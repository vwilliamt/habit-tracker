package com.rork.ember.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rork.ember.data.ThemeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ThemeRepository(app)

    val current: StateFlow<String> = repo.themeKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, "ember")

    fun select(key: String) {
        viewModelScope.launch { repo.set(key) }
    }
}
