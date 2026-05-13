package com.rork.ember.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.foundation.LocalIndication
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rork.ember.data.Habit
import com.rork.ember.ui.theme.EmberColors
import java.time.LocalDate

@Composable
fun HabitCard(
    habit: Habit,
    today: LocalDate,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = Color(habit.colorHex)
    val isDone = habit.isDoneOn(today)
    val streak = habit.currentStreak(today)
    val weeklyDone = habit.recentDays(today, 7).count { it.second }
    val weeklyProgress = (weeklyDone.toFloat() / habit.targetDaysPerWeek.coerceAtLeast(1)).coerceIn(0f, 1f)

    var bumpKey by remember { mutableStateOf(0) }
    val scale by animateFloatAsState(
        targetValue = if (bumpKey % 2 == 1) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale",
    )
    LaunchedEffect(isDone) {
        if (isDone) bumpKey++
    }

    val gradient = if (isDone) {
        Brush.linearGradient(
            colors = listOf(
                accent.copy(alpha = 0.22f),
                accent.copy(alpha = 0.06f),
                EmberColors.SurfaceElevated,
            ),
            start = Offset(0f, 0f),
            end = Offset(900f, 600f),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(EmberColors.SurfaceElevated, EmberColors.Surface),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(26.dp))
            .background(gradient)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isDone)
                        listOf(accent.copy(alpha = 0.6f), accent.copy(alpha = 0.05f))
                    else
                        listOf(Color(0x22FFFFFF), Color(0x06FFFFFF)),
                ),
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onOpen,
            )
            .padding(16.dp)
            .animateContentSize(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Emoji puck
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.35f),
                                accent.copy(alpha = 0.12f),
                            ),
                        )
                    )
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(habit.emoji, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (streak > 0) EmberColors.Primary else EmberColors.TextMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (streak > 0) "$streak day streak" else "Start your streak",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (streak > 0) EmberColors.TextSecondary else EmberColors.TextMuted,
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(EmberColors.TextMuted.copy(alpha = 0.6f)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "$weeklyDone / ${habit.targetDaysPerWeek} this week",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmberColors.TextSecondary,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            CheckPuck(
                isDone = isDone,
                accent = accent,
                onClick = onToggle,
            )
        }

        Spacer(Modifier.height(14.dp))
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun CheckPuck(
    isDone: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "puckScale",
    )
    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isDone) Brush.radialGradient(
                    colors = listOf(accent, accent.copy(alpha = 0.7f)),
                ) else Brush.linearGradient(
                    colors = listOf(EmberColors.SurfaceHigh, EmberColors.Surface),
                )
            )
            .border(
                width = 1.5.dp,
                color = if (isDone) accent else Color(0x33FFFFFF),
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
            ) {
                haptics.performHapticFeedback(
                    if (isDone) HapticFeedbackType.TextHandleMove else HapticFeedbackType.LongPress
                )
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isDone) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Check,
                contentDescription = "Done",
                tint = Color(0xFF1A0E07),
                modifier = Modifier.size(28.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(EmberColors.TextMuted.copy(alpha = 0.5f)),
            )
        }
    }
}
