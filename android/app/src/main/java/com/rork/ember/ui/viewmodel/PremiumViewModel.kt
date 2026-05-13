package com.rork.ember.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    val state: StateFlow<PremiumState> = repo.state
        .stateIn(viewModelScope, SharingStarted.Eagerly, PremiumState())

    private val _purchasing = MutableStateFlow(false)
    val purchasing: StateFlow<Boolean> = _purchasing.asStateFlow()

    private val _justPurchased = MutableStateFlow(false)
    val justPurchased: StateFlow<Boolean> = _justPurchased.asStateFlow()

    fun purchase(plan: PremiumPlan) {
        if (_purchasing.value) return
        viewModelScope.launch {
            _purchasing.value = true
            // Simulated purchase flow.
            delay(1100)
            repo.purchase(plan)
            _purchasing.value = false
            _justPurchased.value = true
        }
    }

    fun restore() {
        viewModelScope.launch {
            _purchasing.value = true
            delay(700)
            repo.restore()
            _purchasing.value = false
            _justPurchased.value = true
        }
    }

    fun acknowledgePurchase() { _justPurchased.value = false }
}
