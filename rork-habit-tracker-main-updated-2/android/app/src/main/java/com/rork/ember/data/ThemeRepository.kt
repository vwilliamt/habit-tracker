package com.rork.ember.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "ember_theme")

class ThemeRepository(private val context: Context) {
    private val keyPref = stringPreferencesKey("palette_key_v1")

    val themeKey: Flow<String> = context.themeDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { it[keyPref] ?: "ember" }

    suspend fun set(key: String) {
        context.themeDataStore.edit { it[keyPref] = key }
    }
}
