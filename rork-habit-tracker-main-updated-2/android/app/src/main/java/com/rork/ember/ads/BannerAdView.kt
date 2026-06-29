package com.rork.ember.ads

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A banner ad for the free tier. Renders nothing (zero height) if ads can't
 * legally be requested yet (consent not resolved) — callers should generally
 * only place this when `!premium.isPremium`.
 *
 * Place this directly in your screen content; it manages its own AdView
 * lifecycle (load on enter, destroy on leave) via DisposableEffect.
 */
@Composable
fun BannerAdView(adUnitId: String = AdIds.BANNER_HOME, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    if (!ConsentManager.canRequestAds(activity)) return

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        },
        update = { adView ->
            // Only load once per AdView instance — AndroidView's `update` can
            // re-run on recomposition, and re-loading the same banner repeatedly
            // wastes fill and looks broken.
            if (adView.responseInfo == null) {
                adView.loadAd(ConsentManager.buildAdRequest(personalizedAdsConsented = true))
            }
        },
        onRelease = { adView -> adView.destroy() },
    )
}
