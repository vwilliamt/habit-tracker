package com.rork.ember.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rork.ember.data.Habit
import com.rork.ember.data.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class HabitsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = HabitRepository(app)

    val habits: StateFlow<List<Habit>> = repo.habits
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleToday(habitId: String, day: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repo.toggle(habitId, day, habits.value)
        }
    }

    fun toggleDay(habitId: String, day: LocalDate) {
        viewModelScope.launch {
            repo.toggle(habitId, day, habits.value)
        }
    }

    fun addHabit(name: String, emoji: String, colorHex: Long, targetDaysPerWeek: Int) {
        viewModelScope.launch {
            val h = Habit(
                id = UUID.randomUUID().toString(),
                name = name,
                emoji = emoji,
                colorHex = colorHex,
                createdEpochDay = LocalDate.now().toEpochDay(),
                targetDaysPerWeek = targetDaysPerWeek,
            )
            repo.upsert(h, habits.value)
        }
    }

    fun updateHabit(id: String, name: String, emoji: String, colorHex: Long, targetDaysPerWeek: Int) {
        viewModelScope.launch {
            val existing = habits.value.firstOrNull { it.id == id } ?: return@launch
            repo.upsert(
                existing.copy(name = name, emoji = emoji, colorHex = colorHex, targetDaysPerWeek = targetDaysPerWeek),
                habits.value,
            )
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch { repo.delete(id, habits.value) }
    }
}
