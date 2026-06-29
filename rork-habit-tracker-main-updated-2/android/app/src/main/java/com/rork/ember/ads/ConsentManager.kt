package com.rork.ember.ads

import android.app.Activity
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

private const val TAG = "ConsentManager"

/**
 * Wraps Google's User Messaging Platform (UMP) SDK so we ask EEA/UK users for
 * ad-personalization consent before showing personalized ads, as required by
 * Play's EU User Consent Policy / GDPR. Outside those regions the consent
 * form is a no-op (Google determines this server-side based on the device's
 * location) and ads load immediately.
 *
 * Call [requestConsent] once, early (e.g. from MainActivity.onCreate),
 * before loading any banner ads.
 */
object ConsentManager {

    /**
     * Requests/updates consent info and shows the consent form if one is
     * required for this user. [onReady] is called once it's safe to load
     * ads — either consent was already obtained, wasn't required, or the
     * user just finished interacting with the form.
     */
    fun requestConsent(activity: Activity, onReady: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            // To test the EEA consent form on a device outside the EEA, build
            // a ConsentDebugSettings with DebugGeography.DEBUG_GEOGRAPHY_EEA
            // and your test device's hashed ID, then call
            // .setConsentDebugSettings(...) here. Remove before shipping.
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadError ->
                    if (loadError != null) {
                        Log.w(TAG, "Consent form error: ${loadError.message}")
                    }
                    onReady()
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.message}")
                // Fail open — still allow ads to load (non-personalized at worst);
                // don't block the app on a consent-network hiccup.
                onReady()
            },
        )
    }

    /** Whether ads can legally be requested right now per the latest consent state. */
    fun canRequestAds(activity: Activity): Boolean =
        UserMessagingPlatform.getConsentInformation(activity).canRequestAds()

    /**
     * Builds an [AdRequest], tagging it as non-personalized when the user has
     * not consented to personalized ads (EEA/UK "Limited Ads" choice). For
     * most regions this returns a normal personalized-eligible request.
     */
    fun buildAdRequest(personalizedAdsConsented: Boolean): AdRequest =
        if (personalizedAdsConsented) {
            AdRequest.Builder().build()
        } else {
            val extras = android.os.Bundle().apply { putString("npa", "1") }
            AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
        }
}
