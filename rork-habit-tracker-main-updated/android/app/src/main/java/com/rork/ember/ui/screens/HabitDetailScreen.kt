package com.rork.ember.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.components.MonthGrid
import com.rork.ember.ui.components.ProgressRing
import com.rork.ember.ui.components.WeekStrip
import com.rork.ember.ui.theme.EmberColors
import com.rork.ember.ui.viewmodel.HabitsViewModel
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun HabitDetailScreen(
    navController: NavController,
    habitId: String,
    viewModel: HabitsViewModel = viewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val habit = habits.firstOrNull { it.id == habitId }
    val today = LocalDate.now()
    val haptics = LocalHapticFeedback.current

    EmberBackground {
        if (habit == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Habit not found", color = EmberColors.TextMuted)
            }
            return@EmberBackground
        }
        val accent = Color(habit.colorHex)
        val streak = habit.currentStreak(today)
        val longest = habit.longestStreak()
        val rate = (habit.completionRate(today) * 100f).roundToInt()
        val week = habit.recentDays(today, 7)
        val grid = habit.recentDays(today, 84) // 12 weeks

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 40.dp),
        ) {
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleBtn(Icons.AutoMirrored.Filled.ArrowBack, "Back") { navController.popBackStack() }
                Spacer(Modifier.weight(1f))
                CircleBtn(Icons.Filled.EditNote, "Edit") {
                    navController.navigate("edit/${habit.id}")
                }
            }

            Spacer(Modifier.height(20.dp))

            // Hero
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(accent.copy(alpha = 0.55f), accent.copy(alpha = 0.12f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(accent.copy(alpha = 0.4f), accent.copy(alpha = 0.12f))
                                )
                            )
                            .border(1.dp, accent.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(habit.emoji, style = MaterialTheme.typography.displayMedium)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        habit.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = EmberColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${habit.targetDaysPerWeek}× per week",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmberColors.TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Stat row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    value = "$streak",
                    label = "Current",
                    icon = Icons.Filled.LocalFireDepartment,
                    accent = accent,
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    value = "$longest",
                    label = "Longest",
                    icon = Icons.Filled.TrendingUp,
                    accent = accent,
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    value = "$rate%",
                    label = "Rate",
                    icon = null,
                    accent = accent,
                    ring = (rate / 100f).coerceIn(0f, 1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("This week")
                Spacer(Modifier.weight(1f))
                Text(
                    "Long-press a day to toggle",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberColors.TextMuted,
                )
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(listOf(EmberColors.SurfaceElevated, EmberColors.Surface))
                    )
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(22.dp))
                    .padding(horizontal = 12.dp, vertical = 18.dp),
            ) {
                WeekStrip(
                    today = today,
                    days = week,
                    accent = accent,
                    onLongPressDay = { date ->
                        if (!date.isAfter(today)) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleDay(habit.id, date)
                        }
                    },
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Last 12 weeks")
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(listOf(EmberColors.SurfaceElevated, EmberColors.Surface))
                    )
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(22.dp))
                    .padding(20.dp),
            ) {
                MonthGrid(
                    days = grid,
                    accent = accent,
                    onLongPressDay = { date ->
                        if (!date.isAfter(today)) {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleDay(habit.id, date)
                        }
                    },
                )
            }

            Spacer(Modifier.height(28.dp))

            // Toggle today big button
            val isDone = habit.isDoneOn(today)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isDone) Brush.linearGradient(
                            listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                        ) else Brush.linearGradient(
                            listOf(accent, accent.copy(alpha = 0.7f))
                        )
                    )
                    .border(
                        1.dp,
                        if (isDone) Color(0x33FFFFFF) else accent,
                        RoundedCornerShape(50),
                    )
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleToday(habit.id)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (isDone) "Undo today" else "Mark today done",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isDone) EmberColors.TextSecondary else Color(0xFF1A0E07),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector?,
    accent: Color,
    ring: Float? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(EmberColors.SurfaceElevated, EmberColors.Surface)))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
            .padding(vertical = 18.dp, horizontal = 14.dp),
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            if (ring != null) {
                ProgressRing(progress = ring, color = accent, size = 36.dp, strokeWidth = 4.dp)
            } else if (icon != null) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineLarge,
                color = EmberColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = EmberColors.TextMuted,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = EmberColors.TextPrimary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun CircleBtn(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(EmberColors.SurfaceElevated)
            .border(1.dp, Color(0x22FFFFFF), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = EmberColors.TextPrimary, modifier = Modifier.size(20.dp))
    }
}
