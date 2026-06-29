package com.rork.ember.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.theme.EmberColors
import com.rork.ember.ui.theme.EmberPalette
import com.rork.ember.ui.theme.EmberPalettes
import com.rork.ember.ui.viewmodel.PremiumViewModel
import com.rork.ember.ui.viewmodel.ThemeViewModel

@Composable
fun ThemePickerScreen(
    navController: NavController,
    themeVm: ThemeViewModel = viewModel(),
    premiumVm: PremiumViewModel = viewModel(),
) {
    val currentKey by themeVm.current.collectAsStateWithLifecycle()
    val premium by premiumVm.state.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            kotlinx.coroutines.delay(1800)
            toast = null
        }
    }

    EmberBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 40.dp),
            ) {
                // Top bar
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmberColors.SurfaceElevated)
                            .border(1.dp, Color(0x22FFFFFF), CircleShape)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = EmberColors.TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Themes",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmberColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.size(40.dp))
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    "Pick the mood",
                    style = MaterialTheme.typography.displayMedium,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Your selection applies instantly across the entire app.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EmberColors.TextSecondary,
                )

                Spacer(Modifier.height(24.dp))

                EmberPalettes.all.forEach { palette ->
                    val locked = palette.isPremium && !premium.isPremium
                    val selected = palette.key == currentKey
                    PaletteCard(
                        palette = palette,
                        selected = selected,
                        locked = locked,
                        onClick = {
                            if (locked) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate("paywall")
                            } else {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                themeVm.select(palette.key)
                                if (!selected) toast = "${palette.displayName} applied"
                            }
                        },
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }

            AnimatedVisibility(
                visible = toast != null,
                enter = fadeIn(tween(160)),
                exit = fadeOut(tween(160)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(EmberColors.SurfaceHigh)
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmberColors.Accent,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            toast ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            color = EmberColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteCard(
    palette: EmberPalette,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = when {
        selected -> EmberColors.Accent
        else -> Color(0x1AFFFFFF)
    }
    val borderWidth = if (selected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(palette.background)
            .border(borderWidth, borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(18.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color preview stack
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(palette.accent, palette.primary, palette.primaryDeep)
                            )
                        )
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp)),
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            palette.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        if (palette.isPremium) {
                            Spacer(Modifier.width(8.dp))
                            ProBadge()
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        palette.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
                if (selected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = palette.accent,
                        modifier = Modifier.size(24.dp),
                    )
                } else if (locked) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0x22000000))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = palette.textSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Swatch row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Swatch(palette.surface, modifier = Modifier.weight(1f))
                Swatch(palette.surfaceElevated, modifier = Modifier.weight(1f))
                Swatch(palette.primary, modifier = Modifier.weight(1f))
                Swatch(palette.accent, modifier = Modifier.weight(1f))
                Swatch(palette.coral, modifier = Modifier.weight(1f))
                Swatch(palette.mint, modifier = Modifier.weight(1f))
                Swatch(palette.sky, modifier = Modifier.weight(1f))
                Swatch(palette.lilac, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Swatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(6.dp)),
    )
}

@Composable
private fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    listOf(EmberColors.Accent, EmberColors.Primary)
                )
            )
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            "PRO",
            style = MaterialTheme.typography.labelSmall,
            color = EmberColors.OnAccent,
            fontWeight = FontWeight.Black,
        )
    }
}
