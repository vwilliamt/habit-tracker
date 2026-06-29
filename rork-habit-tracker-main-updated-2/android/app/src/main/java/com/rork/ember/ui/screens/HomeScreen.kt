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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.rork.ember.ads.BannerAdView
import com.rork.ember.data.FreeTier
import com.rork.ember.data.Habit
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.components.FloatingAdd
import com.rork.ember.ui.components.HabitCard
import com.rork.ember.ui.components.ProgressRing
import com.rork.ember.ui.theme.EmberColors
import com.rork.ember.ui.viewmodel.HabitsViewModel
import com.rork.ember.ui.viewmodel.PremiumViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HabitsViewModel = viewModel(),
    premiumViewModel: PremiumViewModel = viewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val premium by premiumViewModel.state.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var reorderMode by remember { mutableStateOf(false) }
    val atFreeLimit = !premium.isPremium && habits.size >= FreeTier.MAX_HABITS

    // If list empties out, exit reorder mode automatically.
    if (habits.isEmpty() && reorderMode) reorderMode = false

    EmberBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Header(
                        habits = habits,
                        today = today,
                        isPremium = premium.isPremium,
                        onProTap = { navController.navigate("paywall") },
                        onSettingsTap = { navController.navigate("settings") },
                        onThemeTap = { navController.navigate("themes") },
                    )
                }
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
                    item {
                        EmptyState(onAdd = {
                            if (atFreeLimit) navController.navigate("paywall")
                            else navController.navigate("add")
                        })
                    }
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

                if (!premium.isPremium && habits.isNotEmpty()) {
                    item { Spacer(Modifier.height(6.dp)) }
                    item {
                        UpgradeCard(
                            habitCount = habits.size,
                            limit = FreeTier.MAX_HABITS,
                            onTap = { navController.navigate("paywall") },
                        )
                    }
                }

                if (!premium.isPremium) {
                    item { Spacer(Modifier.height(10.dp)) }
                    item { BannerAdView() }
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
                    onClick = {
                        if (atFreeLimit) navController.navigate("paywall")
                        else navController.navigate("add")
                    },
                    locked = atFreeLimit,
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
private fun Header(
    habits: List<Habit>,
    today: LocalDate,
    isPremium: Boolean,
    onProTap: () -> Unit,
    onSettingsTap: () -> Unit,
    onThemeTap: () -> Unit,
) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dateText.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberColors.Primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(10.dp))
                ProPill(isPremium = isPremium, onClick = onProTap)
                Spacer(Modifier.weight(1f))
                ThemeSwatchChip(onClick = onThemeTap)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(EmberColors.SurfaceElevated)
                        .border(1.dp, Color(0x22FFFFFF), CircleShape)
                        .clickable { onSettingsTap() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = EmberColors.TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
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
private fun ThemeSwatchChip(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(EmberColors.SurfaceElevated)
            .border(1.dp, Color(0x22FFFFFF), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(EmberColors.Primary, EmberColors.Accent)
                    )
                )
                .border(1.dp, Color(0x55FFFFFF), CircleShape),
        )
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
private fun ProPill(isPremium: Boolean, onClick: () -> Unit) {
    val bg = if (isPremium) Brush.linearGradient(
        listOf(EmberColors.Accent, EmberColors.Primary)
    ) else Brush.linearGradient(
        listOf(EmberColors.SurfaceHigh, EmberColors.SurfaceElevated)
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(
                1.dp,
                if (isPremium) Color(0x66FFFFFF) else Color(0x22FFFFFF),
                RoundedCornerShape(50),
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = if (isPremium) Color(0xFF1A0E07) else EmberColors.Accent,
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                if (isPremium) "PRO" else "GET PRO",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPremium) Color(0xFF1A0E07) else EmberColors.Accent,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun UpgradeCard(habitCount: Int, limit: Int, onTap: () -> Unit) {
    val remaining = (limit - habitCount).coerceAtLeast(0)
    val atLimit = habitCount >= limit
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        EmberColors.Accent.copy(alpha = 0.22f),
                        EmberColors.Primary.copy(alpha = 0.12f),
                        EmberColors.SurfaceElevated,
                    )
                )
            )
            .border(1.dp, EmberColors.Accent.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable { onTap() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(EmberColors.Accent, EmberColors.Primary)
                        )
                    )
                    .border(1.dp, Color(0x55FFFFFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFF1A0E07),
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (atLimit) "Free limit reached" else "Unlock Ember Pro",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (atLimit) "Upgrade for unlimited habits, no ads, widgets & more."
                    else "$remaining of $limit free habits left · ad-free, widgets, sync",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmberColors.TextSecondary,
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(EmberColors.Accent, EmberColors.Primary)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    "Upgrade",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1A0E07),
                    fontWeight = FontWeight.Bold,
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
