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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.theme.EmberColors
import com.rork.ember.ui.theme.HabitPalette
import com.rork.ember.ui.viewmodel.HabitsViewModel

private val EmojiOptions = listOf(
    "🔥", "🧘", "📖", "🏋️", "💧", "🏃", "🥗", "🧠", "🎨", "🎸", "💻", "🌱",
    "✍️", "☕", "🌅", "🌙", "🧹", "💤", "🚶", "🚴", "🛁", "💊", "🪷", "📝",
)

private val ReminderPresets = listOf(
    420,  // 07:00
    480,  // 08:00
    720,  // 12:00
    1080, // 18:00
    1260, // 21:00
)

@Composable
fun AddHabitScreen(
    navController: NavController,
    editingId: String? = null,
    viewModel: HabitsViewModel = viewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val editing = remember(habits, editingId) { habits.firstOrNull { it.id == editingId } }

    var name by rememberSaveable { mutableStateOf(editing?.name ?: "") }
    var emoji by rememberSaveable { mutableStateOf(editing?.emoji ?: "🔥") }
    var colorHex by rememberSaveable {
        mutableLongStateOf(editing?.colorHex ?: HabitPalette.first())
    }
    var target by rememberSaveable { mutableIntStateOf(editing?.targetDaysPerWeek ?: 7) }
    var reminderMinutes by rememberSaveable {
        mutableStateOf<Int?>(editing?.reminderMinutes)
    }
    var showTimePicker by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(editing?.id) {
        if (editing != null) {
            name = editing.name
            emoji = editing.emoji
            colorHex = editing.colorHex
            target = editing.targetDaysPerWeek
            reminderMinutes = editing.reminderMinutes
        }
    }

    val accent = Color(colorHex)

    EmberBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 40.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleIconButton(
                    icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EmberColors.TextPrimary) },
                    onClick = { navController.popBackStack() },
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (editing != null) "Edit habit" else "New habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                if (editing != null) {
                    CircleIconButton(
                        icon = { Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = EmberColors.Coral) },
                        onClick = {
                            viewModel.deleteHabit(editing.id)
                            navController.popBackStack()
                        },
                    )
                } else {
                    Box(modifier = Modifier.size(44.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.5f), accent.copy(alpha = 0.1f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(accent.copy(alpha = 0.45f), accent.copy(alpha = 0.15f))
                            )
                        )
                        .border(1.dp, accent.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, style = MaterialTheme.typography.displayMedium)
                }
            }

            Spacer(Modifier.height(28.dp))

            FieldLabel("Name")
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(EmberColors.SurfaceElevated)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = EmberColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    ),
                    cursorBrush = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (name.isEmpty()) {
                            Text(
                                "e.g. Morning run",
                                color = EmberColors.TextMuted,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        inner()
                    },
                )
            }

            Spacer(Modifier.height(22.dp))

            FieldLabel("Icon")
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(count = EmojiOptions.size, key = { EmojiOptions[it] }) { idx ->
                    val e = EmojiOptions[idx]
                    val selected = e == emoji
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (selected) Brush.linearGradient(
                                    listOf(accent.copy(alpha = 0.45f), accent.copy(alpha = 0.15f))
                                ) else Brush.linearGradient(
                                    listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                                )
                            )
                            .border(
                                1.dp,
                                if (selected) accent else Color(0x22FFFFFF),
                                RoundedCornerShape(16.dp),
                            )
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                emoji = e
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(e, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            FieldLabel("Color")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HabitPalette.forEach { argb ->
                    val c = Color(argb)
                    val selected = argb == colorHex
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(c, c.copy(alpha = 0.7f))
                                )
                            )
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) Color.White else Color(0x33FFFFFF),
                                shape = CircleShape,
                            )
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                colorHex = argb
                            },
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            FieldLabel("Days per week")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                (1..7).forEach { n ->
                    val selected = n == target
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) Brush.linearGradient(
                                    listOf(accent, accent.copy(alpha = 0.65f))
                                ) else Brush.linearGradient(
                                    listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                                )
                            )
                            .border(
                                1.dp,
                                if (selected) accent else Color(0x22FFFFFF),
                                RoundedCornerShape(14.dp),
                            )
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                target = n
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$n",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected) Color(0xFF1A0E07) else EmberColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Reminder section
            Row(verticalAlignment = Alignment.CenterVertically) {
                FieldLabel("Reminder")
                Spacer(Modifier.weight(1f))
                if (reminderMinutes != null) {
                    Text(
                        text = "Daily at ${formatMinutes(reminderMinutes!!)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                item {
                    ReminderChip(
                        label = "Off",
                        leadingIcon = Icons.Filled.NotificationsOff,
                        selected = reminderMinutes == null,
                        accent = accent,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            reminderMinutes = null
                        },
                    )
                }
                items(count = ReminderPresets.size, key = { ReminderPresets[it] }) { idx ->
                    val m = ReminderPresets[idx]
                    ReminderChip(
                        label = formatMinutes(m),
                        leadingIcon = null,
                        selected = reminderMinutes == m,
                        accent = accent,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            reminderMinutes = m
                        },
                    )
                }
                item {
                    val custom = reminderMinutes != null && reminderMinutes !in ReminderPresets
                    ReminderChip(
                        label = if (custom) "Custom · ${formatMinutes(reminderMinutes!!)}" else "Custom…",
                        leadingIcon = Icons.Filled.NotificationsActive,
                        selected = custom,
                        accent = accent,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showTimePicker = true
                        },
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (name.isNotBlank()) Brush.linearGradient(
                            listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
                        ) else Brush.linearGradient(
                            listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                        )
                    )
                    .clickable(enabled = name.isNotBlank()) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (editing != null) {
                            viewModel.updateHabit(editing.id, name.trim(), emoji, colorHex, target, reminderMinutes)
                        } else {
                            viewModel.addHabit(name.trim(), emoji, colorHex, target, reminderMinutes)
                        }
                        navController.popBackStack()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (name.isNotBlank()) Color(0xFF1A0E07) else EmberColors.TextMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (editing != null) "Save changes" else "Light the habit",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (name.isNotBlank()) Color(0xFF1A0E07) else EmberColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        ReminderTimeDialog(
            initialMinutes = reminderMinutes ?: 480,
            onDismiss = { showTimePicker = false },
            onConfirm = { mins ->
                reminderMinutes = mins
                showTimePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = (initialMinutes / 60).coerceIn(0, 23),
        initialMinute = (initialMinutes % 60).coerceIn(0, 59),
        is24Hour = false,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = EmberColors.SurfaceElevated,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Set reminder",
                    style = MaterialTheme.typography.titleLarge,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(16.dp))
                TimePicker(state = state)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(EmberColors.Surface)
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(50))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmberColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.linearGradient(
                                    listOf(EmberColors.Accent, EmberColors.Primary)
                                )
                            )
                            .clickable {
                                onConfirm(state.hour * 60 + state.minute)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Set",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1A0E07),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderChip(
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector?,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.5f), accent.copy(alpha = 0.18f))
                ) else Brush.linearGradient(
                    listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                )
            )
            .border(
                1.dp,
                if (selected) accent else Color(0x22FFFFFF),
                RoundedCornerShape(50),
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = if (selected) EmberColors.TextPrimary else EmberColors.TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) EmberColors.TextPrimary else EmberColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun formatMinutes(m: Int): String {
    val h = m / 60
    val mm = m % 60
    val hour12 = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    val ampm = if (h < 12) "AM" else "PM"
    val mmStr = if (mm < 10) "0$mm" else "$mm"
    return "$hour12:$mmStr $ampm"
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = EmberColors.TextMuted,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun CircleIconButton(icon: @Composable () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(EmberColors.SurfaceElevated)
            .border(1.dp, Color(0x22FFFFFF), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { icon() }
}
