package com.rork.ember.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

enum class PremiumPlan { NONE, YEARLY, LIFETIME }

data class PremiumState(
    val plan: PremiumPlan = PremiumPlan.NONE,
    val purchasedAt: Long = 0L,
) {
    val isPremium: Boolean get() = plan != PremiumPlan.NONE
}

private val Context.premiumDataStore by preferencesDataStore(name = "ember_premium")

class PremiumRepository(private val context: Context) {
    private val planKey = stringPreferencesKey("plan_v1")
    private val purchasedAtKey = longPreferencesKey("purchased_at_v1")

    val state: Flow<PremiumState> = context.premiumDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val plan = runCatching {
                prefs[planKey]?.let { PremiumPlan.valueOf(it) }
            }.getOrNull() ?: PremiumPlan.NONE
            PremiumState(
                plan = plan,
                purchasedAt = prefs[purchasedAtKey] ?: 0L,
            )
        }

    suspend fun purchase(plan: PremiumPlan) {
        context.premiumDataStore.edit { prefs ->
            prefs[planKey] = plan.name
            prefs[purchasedAtKey] = System.currentTimeMillis()
        }
    }

    suspend fun restore(plan: PremiumPlan = PremiumPlan.LIFETIME) {
        // Simulate a restore — treat as lifetime by default.
        purchase(plan)
    }

    suspend fun clear() {
        context.premiumDataStore.edit { prefs ->
            prefs.remove(planKey)
            prefs.remove(purchasedAtKey)
        }
    }
}

object FreeTier {
    const val MAX_HABITS = 5
}
