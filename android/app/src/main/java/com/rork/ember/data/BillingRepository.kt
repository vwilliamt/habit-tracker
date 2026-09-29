package com.rork.ember.data

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Google Play product IDs — these must match the products set up in Play Console. */
object BillingProducts {
    const val YEARLY = "ember_pro_yearly"
    const val LIFETIME = "ember_pro_lifetime"
}

enum class BillingConnection { DISCONNECTED, CONNECTING, CONNECTED }

/**
 * Wraps the Google Play Billing client and keeps [PremiumRepository]
 * entitlements in sync with real purchases.
 */
class BillingRepository(
    context: Context,
    private val premium: PremiumRepository,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connection = MutableStateFlow(BillingConnection.DISCONNECTED)
    val connection: StateFlow<BillingConnection> = _connection.asStateFlow()

    private val _yearlyPrice = MutableStateFlow<String?>(null)
    val yearlyPrice: StateFlow<String?> = _yearlyPrice.asStateFlow()

    private val _lifetimePrice = MutableStateFlow<String?>(null)
    val lifetimePrice: StateFlow<String?> = _lifetimePrice.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _purchaseGranted = MutableSharedFlow<PremiumPlan>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val purchaseGranted: SharedFlow<PremiumPlan> = _purchaseGranted.asSharedFlow()

    private val yearlyDetails = MutableStateFlow<ProductDetails?>(null)
    private val lifetimeDetails = MutableStateFlow<ProductDetails?>(null)

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    /** Connects to Google Play. Returns true when the service is ready. */
    suspend fun connect(): Boolean {
        if (_connection.value == BillingConnection.CONNECTED) return true
        if (_connection.value == BillingConnection.CONNECTING) return false
        _connection.value = BillingConnection.CONNECTING
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        _connection.value = BillingConnection.CONNECTED
                        if (cont.isActive) cont.resume(true)
                    } else {
                        _connection.value = BillingConnection.DISCONNECTED
                        Log.w(TAG, "Billing setup failed: ${result.responseCode} ${result.debugMessage}")
                        if (cont.isActive) cont.resume(false)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    _connection.value = BillingConnection.DISCONNECTED
                }
            })
        }
    }

    /** Fetches live prices and reconciles existing Play purchases with local entitlements. */
    suspend fun refresh() {
        queryProducts()
        reconcilePurchases()
    }

    /** Re-queries Play purchases and applies the resulting entitlement. */
    suspend fun restore(): Boolean {
        val connected = connect()
        if (!connected) {
            _message.value = "Google Play is unavailable right now. Try again later."
            return false
        }
        queryProducts()
        val found = reconcilePurchases()
        if (!found) _message.value = "No previous purchases found on this Google account."
        return found
    }

    /** Launches the Google Play purchase sheet for the given plan. */
    fun launchPurchase(activity: Activity, plan: PremiumPlan) {
        if (_connection.value != BillingConnection.CONNECTED) {
            _message.value = "Connecting to Google Play — try again in a moment."
            scope.launch {
                if (connect()) refresh()
            }
            return
        }
        val details = when (plan) {
            PremiumPlan.YEARLY -> yearlyDetails.value
            PremiumPlan.LIFETIME -> lifetimeDetails.value
            PremiumPlan.NONE -> null
        }
        if (details == null) {
            _message.value = "This product isn't available yet. Please try again later."
            return
        }
        val productParams = when (plan) {
            PremiumPlan.YEARLY -> {
                val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (offerToken == null) {
                    Log.w(TAG, "No subscription offer for ${details.productId}")
                    _message.value = "This subscription isn't available yet. Please try again later."
                    return
                }
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(offerToken)
                    .build()
            }
            else -> BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        _message.value = null
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "launchBillingFlow failed: ${result.responseCode} ${result.debugMessage}")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.forEach { handlePurchase(it) }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch { reconcilePurchases() }
            else -> {
                Log.w(TAG, "Purchase failed: ${result.responseCode} ${result.debugMessage}")
                _message.value = "Purchase didn't go through. Please try again."
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> scope.launch {
                acknowledge(purchase)
                val plan = if (purchase.products.contains(BillingProducts.LIFETIME)) {
                    PremiumPlan.LIFETIME
                } else {
                    PremiumPlan.YEARLY
                }
                premium.grant(plan, purchase.purchaseTime)
                _purchaseGranted.tryEmit(plan)
            }
            Purchase.PurchaseState.PENDING -> _message.value =
                "Payment pending — Pro unlocks as soon as Google Play confirms."
            else -> Unit
        }
    }

    private suspend fun acknowledge(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val result = billingClient.acknowledgePurchase(
            AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "Acknowledge failed: ${result.responseCode} ${result.debugMessage}")
        }
    }

    private suspend fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingProducts.YEARLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingProducts.LIFETIME)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                )
            )
            .build()
        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.w(TAG, "queryProductDetails failed: ${result.billingResult.responseCode}")
            return
        }
        result.productDetailsList?.forEach { details ->
            when (details.productId) {
                BillingProducts.YEARLY -> {
                    yearlyDetails.value = details
                    _yearlyPrice.value = details.subscriptionOfferDetails
                        ?.firstOrNull()?.pricingPhases?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros > 0 }?.formattedPrice
                        ?: _yearlyPrice.value
                }
                BillingProducts.LIFETIME -> {
                    lifetimeDetails.value = details
                    _lifetimePrice.value = details.oneTimePurchaseOfferDetails?.formattedPrice
                        ?: _lifetimePrice.value
                }
            }
        }
    }

    /** Applies Play-reported purchases to local entitlements. Returns true when a purchase exists. */
    private suspend fun reconcilePurchases(): Boolean {
        var hasYearly = false
        var hasLifetime = false
        var latestAt = 0L

        val subs = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        if (subs.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            subs.purchasesList
                .filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        it.products.contains(BillingProducts.YEARLY)
                }
                .forEach { purchase ->
                    hasYearly = true
                    latestAt = maxOf(latestAt, purchase.purchaseTime)
                    acknowledge(purchase)
                }
        }

        val inapp = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        )
        if (inapp.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            inapp.purchasesList
                .filter {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        it.products.contains(BillingProducts.LIFETIME)
                }
                .forEach { purchase ->
                    hasLifetime = true
                    latestAt = maxOf(latestAt, purchase.purchaseTime)
                    acknowledge(purchase)
                }
        }

        return when {
            hasLifetime -> {
                premium.grant(PremiumPlan.LIFETIME, latestAt)
                true
            }
            hasYearly -> {
                premium.grant(PremiumPlan.YEARLY, latestAt)
                true
            }
            else -> {
                premium.clearIfPlayGranted()
                false
            }
        }
    }

    fun clearMessage() { _message.value = null }

    fun shutdown() {
        scope.cancel()
        runCatching { billingClient.endConnection() }
    }

    private companion object {
        const val TAG = "BillingRepository"
    }
}
