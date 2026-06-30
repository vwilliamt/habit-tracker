package com.rork.ember.ads

/**
 * Ad unit IDs. These are GOOGLE'S OFFICIAL TEST IDs — they always serve test
 * creatives and are safe to ship to internal testing, but they will NEVER
 * earn real revenue.
 *
 * Before releasing to production:
 * 1. Create an AdMob account at admob.google.com and add this app
 * 2. Create a Banner ad unit for the home screen
 * 3. Replace BANNER_HOME below with your real ad unit ID
 * 4. Replace the APPLICATION_ID meta-data value in AndroidManifest.xml with
 *    your real AdMob App ID too — both must be real for production traffic
 *
 * Using test IDs in a production release just means $0 revenue, not a
 * crash — but it's an easy thing to forget, so double check before you ship.
 */
object AdIds {
    const val BANNER_HOME = "ca-app-pub-3940256099942544/9214589741"
}
