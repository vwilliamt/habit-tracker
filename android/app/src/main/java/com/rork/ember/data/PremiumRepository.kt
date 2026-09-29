package com.rork.ember.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
    private val sourceKey = stringPreferencesKey("source_v1")

    val state: Flow<PremiumState> = context.premiumDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            val plan = runCatching {
                prefs[planKey]?.let { PremiumPlan.valueOf(it) }
            }.getOrNull() ?: PremiumPlan.NONE
            PremiumState(
                plan = plan,
                purchasedAt = prefs[purchasedAtKey] ?: 0L,
            )
        }

    /**
     * Records a Play-verified entitlement. Only call this with a purchase
     * confirmed by [BillingRepository] — never from UI code.
     */
    suspend fun grant(plan: PremiumPlan, purchasedAt: Long = System.currentTimeMillis()) {
        context.premiumDataStore.edit { prefs ->
            prefs[planKey] = plan.name
            prefs[purchasedAtKey] = purchasedAt
            prefs[sourceKey] = SOURCE_PLAY
        }
    }

    /**
     * Removes the entitlement only if it was granted by Play billing.
     * Legacy (locally simulated) entitlements are preserved — Play is
     * authoritative only over purchases it actually made.
     */
    suspend fun clearIfPlayGranted() {
        context.premiumDataStore.edit { prefs ->
            if (prefs[sourceKey] == SOURCE_PLAY) {
                prefs.remove(planKey)
                prefs.remove(purchasedAtKey)
                prefs.remove(sourceKey)
            }
        }
    }

    suspend fun clear() {
        context.premiumDataStore.edit { prefs ->
            prefs.remove(planKey)
            prefs.remove(purchasedAtKey)
            prefs.remove(sourceKey)
        }
    }

    private companion object {
        const val SOURCE_PLAY = "play"
    }
}

object FreeTier {
    const val MAX_HABITS = 5
}
