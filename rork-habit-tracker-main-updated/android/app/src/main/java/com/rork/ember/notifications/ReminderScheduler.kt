package com.rork.ember.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.rork.ember.data.Habit
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "ember.habit.reminders"
    const val EXTRA_ID = "habit_id"
    const val EXTRA_NAME = "habit_name"
    const val EXTRA_EMOJI = "habit_emoji"

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Habit reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Gentle nudges to keep your streak alive"
        }
        nm.createNotificationChannel(ch)
    }

    fun reschedule(context: Context, habit: Habit) {
        cancel(context, habit.id)
        val minutes = habit.reminderMinutes ?: return
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = buildPendingIntent(context, habit.id, habit.name, habit.emoji)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutes / 60)
            set(Calendar.MINUTE, minutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        am.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi,
        )
    }

    fun cancel(context: Context, habitId: String) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = buildPendingIntent(context, habitId, name = null, emoji = null, mutableFlag = true)
        am.cancel(pi)
    }

    private fun buildPendingIntent(
        context: Context,
        habitId: String,
        name: String?,
        emoji: String?,
        mutableFlag: Boolean = false,
    ): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            action = "com.rork.ember.REMIND.$habitId"
            putExtra(EXTRA_ID, habitId)
            if (name != null) putExtra(EXTRA_NAME, name)
            if (emoji != null) putExtra(EXTRA_EMOJI, emoji)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            flags,
        )
    }
}
