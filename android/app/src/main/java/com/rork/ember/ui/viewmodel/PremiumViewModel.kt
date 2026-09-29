package com.rork.ember.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rork.ember.data.BillingConnection
import com.rork.ember.data.BillingProducts
import com.rork.ember.data.BillingRepository
import com.rork.ember.data.PremiumPlan
import com.rork.ember.data.PremiumRepository
import com.rork.ember.data.PremiumState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PremiumViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = PremiumRepository(app)
    private val billing = BillingRepository(app, repo)

    val state: StateFlow<PremiumState> = repo.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, PremiumState())

    val connection: StateFlow<BillingConnection> = billing.connection
    val yearlyPrice: StateFlow<String?> = billing.yearlyPrice
    val lifetimePrice: StateFlow<String?> = billing.lifetimePrice
    val message: StateFlow<String?> = billing.message

    private val _purchasing = MutableStateFlow(false)
    val purchasing: StateFlow<Boolean> = _purchasing.asStateFlow()

    private val _justPurchased = MutableStateFlow(false)
    val justPurchased: StateFlow<Boolean> = _justPurchased.asStateFlow()

    init {
        viewModelScope.launch {
            if (billing.connect()) billing.refresh()
        }
        viewModelScope.launch {
            billing.purchaseGranted.collect { _justPurchased.value = true }
        }
    }

    /** Launches Google Play's real purchase sheet for the selected plan. */
    fun purchase(plan: PremiumPlan, activity: Activity?) {
        if (activity == null) return
        billing.launchPurchase(activity, plan)
    }

    /** Re-queries Google Play for existing purchases and applies them. */
    fun restore() {
        if (_purchasing.value) return
        viewModelScope.launch {
            _purchasing.value = true
            billing.restore()
            _purchasing.value = false
        }
    }

    fun acknowledgePurchase() { _justPurchased.value = false }

    fun clearMessage() { billing.clearMessage() }

    /** Cancellation and plan changes live in Google Play, not in the app. */
    fun manageSubscription() {
        val context = getApplication<Application>()
        val uri = (
            "https://play.google.com/store/account/subscriptions" +
                "?sku=${BillingProducts.YEARLY}&package=${context.packageName}"
            ).toUri()
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun onCleared() {
        billing.shutdown()
    }
}
