package com.rork.ember.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
private data class HabitDto(
    val id: String,
    val name: String,
    val emoji: String,
    val colorHex: Long,
    val createdDate: String,
    val targetDaysPerWeek: Int,
    val reminderMinutes: Int?,
    val completions: List<String>,
    val totalCompletions: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float,
)

@Serializable
private data class ExportDto(
    val app: String,
    val version: Int,
    val exportedDate: String,
    val habitsCount: Int,
    val habits: List<HabitDto>,
)

object JsonExporter {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Serializes habits and the full completion log to a pretty JSON file in the
     * app cache and returns a share Intent. Throws on IO failure.
     */
    fun export(context: Context, habits: List<Habit>): Intent {
        val today = LocalDate.now()
        val iso = DateTimeFormatter.ISO_LOCAL_DATE

        val dto = ExportDto(
            app = "Ember",
            version = 1,
            exportedDate = today.format(iso),
            habitsCount = habits.size,
            habits = habits.map { h ->
                HabitDto(
                    id = h.id,
                    name = h.name,
                    emoji = h.emoji,
                    colorHex = h.colorHex,
                    createdDate = LocalDate.ofEpochDay(h.createdEpochDay).format(iso),
                    targetDaysPerWeek = h.targetDaysPerWeek,
                    reminderMinutes = h.reminderMinutes,
                    completions = h.completions.sorted().map { LocalDate.ofEpochDay(it).format(iso) },
                    totalCompletions = h.completions.size,
                    currentStreak = h.currentStreak(today),
                    longestStreak = h.longestStreak(),
                    completionRate = h.completionRate(today),
                )
            },
        )

        val payload = json.encodeToString(ExportDto.serializer(), dto)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "ember-habits-$today.json")
        file.writeText(payload)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ember habits export")
            putExtra(Intent.EXTRA_TEXT, "Your habit history from Ember (JSON).")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(send, "Export habits").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
