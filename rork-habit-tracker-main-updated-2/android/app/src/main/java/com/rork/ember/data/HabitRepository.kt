package com.rork.ember.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val Context.habitsDataStore by preferencesDataStore(name = "ember_habits")

class HabitRepository(private val context: Context) {
    private val key = stringPreferencesKey("habits_json_v1")
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val habits: Flow<List<Habit>> = context.habitsDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val raw = prefs[key] ?: return@map seedHabits()
            runCatching { json.decodeFromString<List<Habit>>(raw) }.getOrDefault(emptyList())
        }

    suspend fun save(list: List<Habit>) {
        context.habitsDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(list)
        }
    }

    suspend fun upsert(habit: Habit, current: List<Habit>) {
        val idx = current.indexOfFirst { it.id == habit.id }
        val updated = if (idx >= 0) current.toMutableList().apply { this[idx] = habit }
        else current + habit
        save(updated)
    }

    suspend fun delete(id: String, current: List<Habit>) {
        save(current.filterNot { it.id == id })
    }

    suspend fun toggle(id: String, day: LocalDate, current: List<Habit>) {
        val updated = current.map { h ->
            if (h.id != id) h
            else {
                val epoch = day.toEpochDay()
                val newSet = if (h.completions.contains(epoch)) h.completions - epoch
                else h.completions + epoch
                h.copy(completions = newSet)
            }
        }
        save(updated)
    }

    private fun seedHabits(): List<Habit> {
        val today = LocalDate.now().toEpochDay()
        return listOf(
            Habit(
                id = "seed-1",
                name = "Morning meditation",
                emoji = "🧘",
                colorHex = 0xFFFFC36BL,
                createdEpochDay = today - 10,
                targetDaysPerWeek = 7,
                completions = setOf(today - 1, today - 2, today - 3, today - 5, today - 6),
            ),
            Habit(
                id = "seed-2",
                name = "Read 20 pages",
                emoji = "📖",
                colorHex = 0xFF6FB8FFL,
                createdEpochDay = today - 14,
                targetDaysPerWeek = 5,
                completions = setOf(today - 1, today - 2, today - 4, today - 7),
            ),
            Habit(
                id = "seed-3",
                name = "Workout",
                emoji = "🏋️",
                colorHex = 0xFFFF6B6BL,
                createdEpochDay = today - 21,
                targetDaysPerWeek = 4,
                completions = setOf(today, today - 2, today - 4, today - 6, today - 9),
            ),
            Habit(
                id = "seed-4",
                name = "Drink 2L water",
                emoji = "💧",
                colorHex = 0xFF7BE0B5L,
                createdEpochDay = today - 7,
                targetDaysPerWeek = 7,
                completions = setOf(today, today - 1, today - 2, today - 3),
            ),
        )
    }
}
