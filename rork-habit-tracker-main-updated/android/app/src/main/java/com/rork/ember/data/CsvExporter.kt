package com.rork.ember.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CsvExporter {
    /**
     * Writes the user's habits + completion log to a CSV in the app cache and returns
     * a share Intent. Throws on IO failure.
     */
    fun export(context: Context, habits: List<Habit>): Intent {
        val today = LocalDate.now()
        val sb = StringBuilder()

        // Summary section
        sb.append("# Ember habits export\n")
        sb.append("# Exported: ").append(today.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n\n")
        sb.append("habit_id,name,emoji,target_per_week,created_date,reminder_minutes,total_completions,current_streak,longest_streak,completion_rate\n")
        for (h in habits) {
            val created = LocalDate.ofEpochDay(h.createdEpochDay).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val rate = "%.3f".format(h.completionRate(today))
            sb.append(escape(h.id)).append(',')
                .append(escape(h.name)).append(',')
                .append(escape(h.emoji)).append(',')
                .append(h.targetDaysPerWeek).append(',')
                .append(created).append(',')
                .append(h.reminderMinutes?.toString() ?: "").append(',')
                .append(h.completions.size).append(',')
                .append(h.currentStreak(today)).append(',')
                .append(h.longestStreak()).append(',')
                .append(rate).append('\n')
        }

        // Long-format daily log
        sb.append('\n')
        sb.append("habit_id,name,date\n")
        for (h in habits) {
            h.completions.sorted().forEach { day ->
                val date = LocalDate.ofEpochDay(day).format(DateTimeFormatter.ISO_LOCAL_DATE)
                sb.append(escape(h.id)).append(',')
                    .append(escape(h.name)).append(',')
                    .append(date).append('\n')
            }
        }

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "ember-habits-$today.csv")
        file.writeText(sb.toString())

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ember habits export")
            putExtra(Intent.EXTRA_TEXT, "Your habit history from Ember.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, "Export habits").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun escape(s: String): String {
        val needs = s.contains(',') || s.contains('"') || s.contains('\n') || s.contains('\r')
        if (!needs) return s
        return "\"" + s.replace("\"", "\"\"") + "\""
    }
}
