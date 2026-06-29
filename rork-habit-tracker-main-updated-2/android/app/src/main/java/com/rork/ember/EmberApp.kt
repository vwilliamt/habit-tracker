package com.rork.ember

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.rork.ember.notifications.ReminderScheduler

class EmberApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.ensureChannel(this)
        // Consent (UMP) is requested from MainActivity since it needs an
        // Activity; MobileAds.initialize() is safe to call here regardless.
        MobileAds.initialize(this)
    }
}
