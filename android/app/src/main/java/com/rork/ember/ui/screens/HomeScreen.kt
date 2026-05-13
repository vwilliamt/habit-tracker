package com.rork.ember.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rork.ember.data.Habit
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.components.FloatingAdd
import com.rork.ember.ui.components.HabitCard
import com.rork.ember.ui.components.ProgressRing
import com.rork.ember.ui.theme.EmberColors
import com.rork.ember.ui.viewmodel.HabitsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HabitsViewModel = viewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var reorderMode by remember { mutableStateOf(false) }

    // If list empties out, exit reorder mode automatically.
    if (habits.isEmpty() && reorderMode) reorderMode = false

    EmberBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { Header(habits = habits, today = today) }
                item { Spacer(Modifier.height(4.dp)) }
                item {
                    SectionLabel(
                        text = if (reorderMode) "Reorder" else "Today",
                        trailing = if (reorderMode) "Long-press a card to undo • drag arrows"
                        else "${habits.count { it.isDoneOn(today) }} of ${habits.size}",
                        reorderMode = reorderMode,
                        canToggleReorder = habits.size >= 2,
                        onToggleReorder = { reorderMode = !reorderMode },
                    )
                }

                if (habits.isEmpty()) {
                    item { EmptyState(onAdd = { navController.navigate("add") }) }
                } else {
                    items(items = habits, key = { it.id }) { habit ->
                        val idx = habits.indexOf(habit)
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
                            exit = fadeOut(),
                        ) {
                            HabitCard(
                                habit = habit,
                                today = today,
                                onToggle = { viewModel.toggleToday(habit.id) },
                                onOpen = { navController.navigate("habit/${habit.id}") },
                                onLongPress = {
                                    if (!reorderMode && habits.size >= 2) reorderMode = true
                                },
                                reorderMode = reorderMode,
                                canMoveUp = idx > 0,
                                canMoveDown = idx >= 0 && idx < habits.size - 1,
                                onMoveUp = { viewModel.moveHabit(idx, idx - 1) },
                                onMoveDown = { viewModel.moveHabit(idx, idx + 1) },
                            )
                        }
                    }
                }
            }

            if (reorderMode) {
                DoneReorderButton(
                    onClick = { reorderMode = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp),
                )
            } else {
                FloatingAdd(
                    onClick = { navController.navigate("add") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 22.dp, bottom = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun DoneReorderButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
                )
            )
            .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 26.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Done,
                contentDescription = null,
                tint = Color(0xFF1A0E07),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Done reordering",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1A0E07),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun Header(habits: List<Habit>, today: LocalDate) {
    val doneToday = habits.count { it.isDoneOn(today) }
    val total = habits.size.coerceAtLeast(1)
    val progress = doneToday.toFloat() / total
    val animProgress by animateFloatAsState(progress, tween(700), label = "headerProgress")

    val greeting = when (today.dayOfWeek.value) {
        in 1..5 -> "Keep the streak alive"
        6 -> "Weekend warrior"
        else -> "Sunday reset"
    }
    val dateText = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    val longestStreak = habits.maxOfOrNull { it.currentStreak(today) } ?: 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateText.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = EmberColors.Primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = greeting,
                style = MaterialTheme.typography.displayMedium,
                color = EmberColors.TextPrimary,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = EmberColors.Primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (longestStreak > 0) "$longestStreak day streak" else "Light your first ember",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberColors.TextSecondary,
                )
            }
        }
        ProgressRing(
            progress = animProgress,
            color = EmberColors.Primary,
            size = 86.dp,
            strokeWidth = 8.dp,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$doneToday",
                    style = MaterialTheme.typography.headlineLarge,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "/ ${habits.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    trailing: String,
    reorderMode: Boolean,
    canToggleReorder: Boolean,
    onToggleReorder: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = EmberColors.TextPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    if (reorderMode) Brush.linearGradient(
                        listOf(EmberColors.Accent.copy(alpha = 0.3f), EmberColors.Primary.copy(alpha = 0.15f))
                    ) else Brush.linearGradient(
                        listOf(EmberColors.SurfaceHigh, EmberColors.SurfaceHigh)
                    )
                )
                .border(
                    1.dp,
                    if (reorderMode) EmberColors.Accent.copy(alpha = 0.6f) else Color(0x22FFFFFF),
                    RoundedCornerShape(50),
                )
                .then(
                    if (canToggleReorder) Modifier.clickable { onToggleReorder() }
                    else Modifier
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canToggleReorder) {
                    Icon(
                        Icons.Filled.SwapVert,
                        contentDescription = null,
                        tint = if (reorderMode) EmberColors.Accent else EmberColors.TextSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reorderMode) EmberColors.TextPrimary else EmberColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(EmberColors.SurfaceElevated, EmberColors.Surface),
                )
            )
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(28.dp))
            .clickable { onAdd() }
            .padding(24.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("✨", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(12.dp))
            Text(
                "No habits yet",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberColors.TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Add your first ritual and start your streak.",
                style = MaterialTheme.typography.bodyLarge,
                color = EmberColors.TextSecondary,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(EmberColors.Primary, EmberColors.PrimaryDeep)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF1A0E07), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "New habit",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1A0E07),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
