package com.rork.ember.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.rork.ember.data.PremiumPlan
import com.rork.ember.data.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * All the product/base-plan IDs this app expects to exist in Play Console.
 *
 * Setup in Play Console (Monetize > Products):
 * 1. Create ONE subscription with product ID [SUBSCRIPTION_PRODUCT_ID].
 *    Inside it, add THREE base plans with these exact IDs:
 *      - [BASE_PLAN_WEEKLY]   — recurring, 1 week billing period
 *      - [BASE_PLAN_MONTHLY]  — recurring, 1 month billing period
 *      - [BASE_PLAN_YEARLY]   — recurring, 1 year billing period
 *    Each base plan needs its own price + an auto-renewing offer enabled.
 * 2. Create ONE one-time product (Monetize > Products > One-time products)
 *    with product ID [LIFETIME_PRODUCT_ID] for the Lifetime unlock.
 *
 * If you rename anything here, the IDs in Play Console must match exactly.
 */
object BillingIds {
    const val SUBSCRIPTION_PRODUCT_ID = "ember_pro"
    const val BASE_PLAN_WEEKLY = "weekly-plan"
    const val BASE_PLAN_MONTHLY = "monthly-plan"
    const val BASE_PLAN_YEARLY = "yearly-plan"
    const val LIFETIME_PRODUCT_ID = "ember_lifetime"

    fun planForBasePlanId(basePlanId: String?): PremiumPlan = when (basePlanId) {
        BASE_PLAN_WEEKLY -> PremiumPlan.WEEKLY
        BASE_PLAN_MONTHLY -> PremiumPlan.MONTHLY
        BASE_PLAN_YEARLY -> PremiumPlan.YEARLY
        else -> PremiumPlan.NONE
    }
}

enum class BillingConnectionState { CONNECTING, CONNECTED, DISCONNECTED, UNAVAILABLE }

/** A purchasable offer, resolved live from Play, ready to hand to launchBillingFlow. */
data class PlanOffer(
    val plan: PremiumPlan,
    val formattedPrice: String,
    val productDetails: ProductDetails,
    /** Required for subscription base plans, null for the one-time Lifetime product. */
    val offerToken: String?,
)

sealed class BillingEvent {
    data class Purchased(val plan: PremiumPlan) : BillingEvent()
    data class Failed(val message: String) : BillingEvent()
    data object UserCancelled : BillingEvent()
    data object Pending : BillingEvent()
}

private const val TAG = "BillingManager"

/**
 * Thin wrapper around [BillingClient] that:
 *  - connects to Play and fetches live ProductDetails for all 4 plans
 *  - launches the purchase flow for a chosen plan
 *  - acknowledges purchases and syncs entitlement into [PremiumRepository]
 *  - restores existing purchases on startup / on demand
 */
class BillingManager(
    context: Context,
    private val premiumRepository: PremiumRepository,
) : PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Main)

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private val _connectionState = MutableStateFlow(BillingConnectionState.CONNECTING)
    val connectionState: StateFlow<BillingConnectionState> = _connectionState.asStateFlow()

    private val _offers = MutableStateFlow<Map<PremiumPlan, PlanOffer>>(emptyMap())
    val offers: StateFlow<Map<PremiumPlan, PlanOffer>> = _offers.asStateFlow()

    private val _events = MutableStateFlow<BillingEvent?>(null)
    val events: StateFlow<BillingEvent?> = _events.asStateFlow()

    fun consumeEvent() {
        _events.update { null }
    }

    fun startConnection() {
        if (billingClient.isReady) {
            queryOffers()
            restorePurchases()
            return
        }
        _connectionState.value = BillingConnectionState.CONNECTING
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _connectionState.value = BillingConnectionState.CONNECTED
                    queryOffers()
                    restorePurchases()
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.debugMessage}")
                    _connectionState.value = BillingConnectionState.UNAVAILABLE
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = BillingConnectionState.DISCONNECTED
            }
        })
    }

    /** Fetches live prices for the subscription base plans + the lifetime one-time product. */
    private fun queryOffers() {
        scope.launch {
            val subsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(BillingIds.SUBSCRIPTION_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()

            val inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(BillingIds.LIFETIME_PRODUCT_ID)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                )
                .build()

            val subsResult = runCatching { billingClient.queryProductDetails(subsParams) }.getOrNull()
            val inAppResult = runCatching { billingClient.queryProductDetails(inAppParams) }.getOrNull()

            val resolved = mutableMapOf<PremiumPlan, PlanOffer>()

            subsResult?.productDetailsList?.forEach { details ->
                details.subscriptionOfferDetails?.forEach { offer ->
                    val plan = BillingIds.planForBasePlanId(offer.basePlanId)
                    if (plan == PremiumPlan.NONE) return@forEach
                    val price = offer.pricingPhases.pricingPhaseList.firstOrNull()
                        ?.formattedPrice ?: return@forEach
                    // Prefer the first eligible offer for each base plan (e.g. no trial stacking).
                    if (!resolved.containsKey(plan)) {
                        resolved[plan] = PlanOffer(
                            plan = plan,
                            formattedPrice = price,
                            productDetails = details,
                            offerToken = offer.offerToken,
                        )
                    }
                }
            }

            inAppResult?.productDetailsList?.firstOrNull { it.productId == BillingIds.LIFETIME_PRODUCT_ID }
                ?.let { details ->
                    val price = details.oneTimePurchaseOfferDetails?.formattedPrice
                    if (price != null) {
                        resolved[PremiumPlan.LIFETIME] = PlanOffer(
                            plan = PremiumPlan.LIFETIME,
                            formattedPrice = price,
                            productDetails = details,
                            offerToken = null,
                        )
                    }
                }

            if (resolved.isEmpty()) {
                Log.w(TAG, "No products resolved — check Play Console product/base-plan IDs match BillingIds.")
            }
            _offers.value = resolved
        }
    }

    private var pendingPurchasePlan: PremiumPlan = PremiumPlan.NONE

    /** Launches the Play purchase sheet for the given plan. No-op if the offer isn't loaded yet. */
    fun launchPurchaseFlow(activity: Activity, plan: PremiumPlan) {
        val offer = _offers.value[plan]
        if (offer == null) {
            _events.value = BillingEvent.Failed("This plan isn't available right now. Please try again shortly.")
            return
        }

        val productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(offer.productDetails)
        if (offer.offerToken != null) {
            productDetailsParamsBuilder.setOfferToken(offer.offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()

        // Remember what the user actually tapped so we can label the resulting purchase
        // correctly — Play's Purchase object identifies the subscription product but not
        // which base plan (cadence) was bought, so we can't reliably re-derive that later.
        pendingPurchasePlan = plan
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _events.value = BillingEvent.UserCancelled
            }
            else -> {
                _events.value = BillingEvent.Failed(result.debugMessage.ifBlank { "Purchase failed." })
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                scope.launch {
                    val plan = resolvePlan(purchase)
                    if (plan != PremiumPlan.NONE) {
                        premiumRepository.purchase(plan)
                    }
                    if (!purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        runCatching { billingClient.acknowledgePurchase(ackParams) }
                            .onFailure { Log.w(TAG, "Acknowledge failed", it) }
                    }
                    _events.value = if (plan != PremiumPlan.NONE) {
                        BillingEvent.Purchased(plan)
                    } else {
                        BillingEvent.Failed("Purchase succeeded but the plan couldn't be identified.")
                    }
                    pendingPurchasePlan = PremiumPlan.NONE
                }
            }
            Purchase.PurchaseState.PENDING -> {
                // e.g. cash/pending payment methods — entitlement grants once it clears and
                // onPurchasesUpdated / restorePurchases fires again with PURCHASED.
                _events.value = BillingEvent.Pending
            }
            else -> Unit
        }
    }

    /** Maps a completed Purchase back to a [PremiumPlan]. */
    private fun resolvePlan(purchase: Purchase): PremiumPlan {
        val productId = purchase.products.firstOrNull() ?: return PremiumPlan.NONE
        return when (productId) {
            BillingIds.LIFETIME_PRODUCT_ID -> PremiumPlan.LIFETIME
            BillingIds.SUBSCRIPTION_PRODUCT_ID -> {
                // Fresh purchase made in this session — we know exactly what was tapped.
                if (pendingPurchasePlan != PremiumPlan.NONE) pendingPurchasePlan else PremiumPlan.MONTHLY
            }
            else -> PremiumPlan.NONE
        }
    }

    /** Queries Play for purchases the user already owns (active subs + the lifetime IAP). */
    fun restorePurchases() {
        scope.launch {
            val subsPurchases = runCatching {
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
                )
            }.getOrNull()?.purchasesList.orEmpty()

            val inAppPurchases = runCatching {
                billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
                )
            }.getOrNull()?.purchasesList.orEmpty()

            // Lifetime wins if the user owns both — it's a strict superset of the subscription.
            val lifetimePurchase = inAppPurchases.firstOrNull {
                it.products.contains(BillingIds.LIFETIME_PRODUCT_ID) &&
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            val activeSub = subsPurchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }

            when {
                lifetimePurchase != null -> {
                    premiumRepository.purchase(PremiumPlan.LIFETIME)
                    acknowledgeIfNeeded(lifetimePurchase)
                }
                activeSub != null -> {
                    val plan = subscriptionPlanFromPurchase(activeSub)
                    premiumRepository.purchase(plan)
                    acknowledgeIfNeeded(activeSub)
                }
                else -> {
                    // No active entitlement found on Play — clear any stale local state.
                    premiumRepository.clear()
                }
            }
        }
    }

    private suspend fun acknowledgeIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            runCatching { billingClient.acknowledgePurchase(ackParams) }
        }
    }

    /**
     * A restored subscription purchase doesn't directly tell us which base plan it's on via
     * the Purchase object alone — Play associates that with the offer at purchase time. The
     * reliable way to read it back is the receipt's `basePlanId`, exposed through the purchase's
     * account identifiers in some library versions; if your Play Console plan IDs differ, you
     * may need to call the Play Developer API (purchases.subscriptionsv2) server-side for the
     * authoritative base plan. As a practical client-side fallback we keep whatever cadence is
     * already cached locally (set at the moment of purchase) and only default to MONTHLY if we
     * have no prior record at all — e.g. a fresh install where the user restores a purchase made
     * on another device.
     */
    private suspend fun subscriptionPlanFromPurchase(purchase: Purchase): PremiumPlan {
        val cached = premiumRepository.currentPlan()
        return if (cached == PremiumPlan.WEEKLY || cached == PremiumPlan.MONTHLY || cached == PremiumPlan.YEARLY) {
            cached
        } else {
            PremiumPlan.MONTHLY
        }
    }

    fun endConnection() {
        if (billingClient.isReady) billingClient.endConnection()
    }
}
