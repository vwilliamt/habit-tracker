package com.rork.ember.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rork.ember.billing.BillingConnectionState
import com.rork.ember.billing.BillingEvent
import com.rork.ember.billing.BillingManager
import com.rork.ember.billing.PlanOffer
import com.rork.ember.data.PremiumPlan
import com.rork.ember.data.PremiumRepository
import com.rork.ember.data.PremiumState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PremiumViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PremiumRepository(app)
    private val billing = BillingManager(app, repo)

    val state: StateFlow<PremiumState> = repo.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, PremiumState())

    /** Live prices resolved from Play Console. Empty until billing connects. */
    val offers: StateFlow<Map<PremiumPlan, PlanOffer>> = billing.offers
    val connectionState: StateFlow<BillingConnectionState> = billing.connectionState

    private val _purchasing = MutableStateFlow(false)
    val purchasing: StateFlow<Boolean> = _purchasing.asStateFlow()

    private val _justPurchased = MutableStateFlow(false)
    val justPurchased: StateFlow<Boolean> = _justPurchased.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        billing.startConnection()
        viewModelScope.launch {
            billing.events.collect { event ->
                when (event) {
                    is BillingEvent.Purchased -> {
                        _purchasing.value = false
                        _justPurchased.value = true
                        billing.consumeEvent()
                    }
                    is BillingEvent.Failed -> {
                        _purchasing.value = false
                        _errorMessage.value = event.message
                        billing.consumeEvent()
                    }
                    BillingEvent.UserCancelled -> {
                        _purchasing.value = false
                        billing.consumeEvent()
                    }
                    BillingEvent.Pending -> {
                        _purchasing.value = false
                        _errorMessage.value = "Your payment is pending — we'll unlock Pro as soon as it clears."
                        billing.consumeEvent()
                    }
                    null -> Unit
                }
            }
        }
    }

    /** Launches the real Play purchase sheet for [plan]. Requires the hosting Activity. */
    fun purchase(activity: Activity, plan: PremiumPlan) {
        if (_purchasing.value) return
        _purchasing.value = true
        billing.launchPurchaseFlow(activity, plan)
    }

    /** Re-syncs entitlement from Play (active subs + lifetime IAP) without showing the sheet. */
    fun restore() {
        if (_purchasing.value) return
        viewModelScope.launch {
            _purchasing.value = true
            billing.restorePurchases()
            // restorePurchases() is fire-and-forget against PremiumRepository; give it a beat
            // to land before flipping the success UI so state.isPremium has caught up.
            delay(600)
            _purchasing.value = false
            if (state.value.isPremium) {
                _justPurchased.value = true
            } else {
                _errorMessage.value = "No active purchase found for this Google account."
            }
        }
    }

    fun acknowledgePurchase() { _justPurchased.value = false }

    fun consumeError() { _errorMessage.value = null }

    /**
     * Local-only state reset. This does NOT cancel the real Play subscription — Play doesn't
     * allow apps to cancel subscriptions via the Billing Library. Use this only to clear stale
     * local entitlement (e.g. after support intervention); for the actual "Cancel subscription"
     * UI action, deep-link to the Play subscription center instead (see SettingsScreen).
     */
    fun clearLocalEntitlement() {
        viewModelScope.launch {
            repo.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        billing.endConnection()
    }
}
