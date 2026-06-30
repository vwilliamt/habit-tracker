package com.rork.ember.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rork.ember.ui.theme.EmberColors
import java.time.LocalDate

@Composable
fun WeekStrip(
    today: LocalDate,
    days: List<Pair<LocalDate, Boolean>>,
    accent: Color,
    modifier: Modifier = Modifier,
    onLongPressDay: ((LocalDate) -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { (date, done) ->
            DayPill(
                date = date,
                isToday = date == today,
                isDone = done,
                accent = accent,
                onLongPress = if (onLongPressDay != null) {
                    { onLongPressDay(date) }
                } else null,
            )
        }
    }
}

@Composable
private fun DayPill(
    date: LocalDate,
    isToday: Boolean,
    isDone: Boolean,
    accent: Color,
    onLongPress: (() -> Unit)?,
) {
    val letter = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.getDefault())
    val haptics = LocalHapticFeedback.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            letter,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) EmberColors.TextPrimary else EmberColors.TextMuted,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isDone) Brush.radialGradient(
                        colors = listOf(accent, accent.copy(alpha = 0.6f)),
                    ) else Brush.linearGradient(
                        colors = listOf(EmberColors.SurfaceHigh, EmberColors.Surface),
                    )
                )
                .border(
                    width = if (isToday) 1.5.dp else 1.dp,
                    color = when {
                        isToday && isDone -> Color.White.copy(alpha = 0.8f)
                        isToday -> accent.copy(alpha = 0.8f)
                        else -> Color(0x22FFFFFF)
                    },
                    shape = CircleShape,
                )
                .then(
                    if (onLongPress != null) Modifier.pointerInput(date) {
                        detectTapGestures(
                            onLongPress = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongPress()
                            },
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = if (isDone) Color(0xFF1A0E07) else EmberColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun MonthGrid(
    days: List<Pair<LocalDate, Boolean>>,
    accent: Color,
    modifier: Modifier = Modifier,
    onLongPressDay: ((LocalDate) -> Unit)? = null,
) {
    val rows = days.chunked(7)
    val haptics = LocalHapticFeedback.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (date, done) ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (done) accent.copy(alpha = 0.9f)
                                else EmberColors.SurfaceHigh
                            )
                            .border(
                                width = 1.dp,
                                color = if (done) accent else Color(0x14FFFFFF),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .then(
                                if (onLongPressDay != null) Modifier.pointerInput(date) {
                                    detectTapGestures(
                                        onLongPress = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onLongPressDay(date)
                                        },
                                    )
                                } else Modifier
                            ),
                    )
                }
                repeat(7 - row.size) {
                    Box(modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
