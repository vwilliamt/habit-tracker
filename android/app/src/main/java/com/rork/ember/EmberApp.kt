package com.rork.ember

import android.app.Application
import com.rork.ember.notifications.ReminderScheduler

class EmberApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.ensureChannel(this)
    }
}
