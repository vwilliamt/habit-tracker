package com.rork.ember.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rork.ember.data.Habit
import com.rork.ember.data.HabitRepository
import com.rork.ember.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class HabitsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = HabitRepository(app)
    private val appCtx = app.applicationContext

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

    fun addHabit(
        name: String,
        emoji: String,
        colorHex: Long,
        targetDaysPerWeek: Int,
        reminderMinutes: Int? = null,
    ) {
        viewModelScope.launch {
            val h = Habit(
                id = UUID.randomUUID().toString(),
                name = name,
                emoji = emoji,
                colorHex = colorHex,
                createdEpochDay = LocalDate.now().toEpochDay(),
                targetDaysPerWeek = targetDaysPerWeek,
                reminderMinutes = reminderMinutes,
            )
            repo.upsert(h, habits.value)
            ReminderScheduler.reschedule(appCtx, h)
        }
    }

    fun updateHabit(
        id: String,
        name: String,
        emoji: String,
        colorHex: Long,
        targetDaysPerWeek: Int,
        reminderMinutes: Int? = null,
    ) {
        viewModelScope.launch {
            val existing = habits.value.firstOrNull { it.id == id } ?: return@launch
            val updated = existing.copy(
                name = name,
                emoji = emoji,
                colorHex = colorHex,
                targetDaysPerWeek = targetDaysPerWeek,
                reminderMinutes = reminderMinutes,
            )
            repo.upsert(updated, habits.value)
            ReminderScheduler.reschedule(appCtx, updated)
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            repo.delete(id, habits.value)
            ReminderScheduler.cancel(appCtx, id)
        }
    }

    fun moveHabit(from: Int, to: Int) {
        if (from == to) return
        viewModelScope.launch {
            val list = habits.value.toMutableList()
            if (from !in list.indices || to !in list.indices) return@launch
            val item = list.removeAt(from)
            list.add(to, item)
            repo.save(list)
        }
    }
}
