package com.rork.ember.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Serializable
data class Habit(
    val id: String,
    val name: String,
    val emoji: String,
    val colorHex: Long,
    val createdEpochDay: Long,
    val targetDaysPerWeek: Int = 7,
    val completions: Set<Long> = emptySet(),
) {
    fun isDoneOn(day: LocalDate): Boolean = completions.contains(day.toEpochDay())

    /** Consecutive day streak ending today (or yesterday if today not done). */
    fun currentStreak(today: LocalDate): Int {
        if (completions.isEmpty()) return 0
        var count = 0
        var cursor = today
        // If today not done, start from yesterday so streak is not lost mid-day.
        if (!completions.contains(cursor.toEpochDay())) {
            cursor = cursor.minusDays(1)
        }
        while (completions.contains(cursor.toEpochDay())) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    fun longestStreak(): Int {
        if (completions.isEmpty()) return 0
        val sorted = completions.sorted()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            run = if (sorted[i] == sorted[i - 1] + 1) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    fun completionRate(today: LocalDate): Float {
        val days = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(createdEpochDay), today) + 1
        if (days <= 0) return 0f
        val expected = days * targetDaysPerWeek / 7f
        if (expected <= 0f) return 0f
        return (completions.size / expected).coerceIn(0f, 1f)
    }

    /** Returns last `days` epoch days from oldest to today and whether each was done. */
    fun recentDays(today: LocalDate, days: Int): List<Pair<LocalDate, Boolean>> {
        return (days - 1 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            d to completions.contains(d.toEpochDay())
        }
    }
}
