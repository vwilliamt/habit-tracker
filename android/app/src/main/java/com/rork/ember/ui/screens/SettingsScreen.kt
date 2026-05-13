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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rork.ember.data.CsvExporter
import com.rork.ember.data.JsonExporter
import com.rork.ember.data.PremiumPlan
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.theme.EmberColors
import com.rork.ember.ui.theme.EmberPalettes
import com.rork.ember.ui.viewmodel.HabitsViewModel
import com.rork.ember.ui.viewmodel.PremiumViewModel
import com.rork.ember.ui.viewmodel.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: PremiumViewModel = viewModel(),
    themeVm: ThemeViewModel = viewModel(),
    habitsVm: HabitsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val purchasing by viewModel.purchasing.collectAsStateWithLifecycle()
    val justPurchased by viewModel.justPurchased.collectAsStateWithLifecycle()
    val themeKey by themeVm.current.collectAsStateWithLifecycle()
    val habits by habitsVm.habits.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    val currentPalette = remember(themeKey) { EmberPalettes.byKey(themeKey) }

    var showCancelSheet by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(justPurchased) {
        if (justPurchased) {
            toast = "Purchases restored"
            viewModel.acknowledgePurchase()
        }
    }

    androidx.compose.runtime.LaunchedEffect(toast) {
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
                    CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Back") {
                        navController.popBackStack()
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = EmberColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.size(40.dp))
                }

                Spacer(Modifier.height(20.dp))

                // Membership hero card
                MembershipCard(
                    isPremium = state.isPremium,
                    plan = state.plan,
                    purchasedAt = state.purchasedAt,
                    onUpgrade = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate("paywall")
                    },
                )

                Spacer(Modifier.height(22.dp))

                // Subscription section
                SectionHeader("Subscription")
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.WorkspacePremium,
                        title = if (state.isPremium) "Manage subscription" else "Upgrade to Pro",
                        subtitle = if (state.isPremium) {
                            when (state.plan) {
                                PremiumPlan.YEARLY -> "Yearly plan · $24.99/year"
                                PremiumPlan.LIFETIME -> "Lifetime · paid in full"
                                else -> "Active"
                            }
                        } else "5 habits free · unlock everything",
                        tintAccent = true,
                        onClick = { navController.navigate("paywall") },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Filled.Restore,
                        title = "Restore purchases",
                        subtitle = if (purchasing) "Restoring..." else "Already subscribed? Tap to restore",
                        loading = purchasing,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.restore()
                        },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Filled.Receipt,
                        title = "Billing & receipts",
                        subtitle = "View invoices in the Play Store",
                        trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                        onClick = { toast = "Opens Play Store on real device" },
                    )
                    if (state.isPremium && state.plan == PremiumPlan.YEARLY) {
                        Divider()
                        SettingsRow(
                            icon = Icons.Filled.Cancel,
                            title = "Cancel subscription",
                            subtitle = "Keep access until your renewal date",
                            danger = true,
                            onClick = { showCancelSheet = true },
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                // Premium features section
                SectionHeader("Pro features")
                SettingsGroup {
                    ProFeatureRow(Icons.Filled.Bolt, "Unlimited habits", state.isPremium)
                    Divider()
                    ProFeatureRow(Icons.Filled.CloudSync, "Cloud sync", state.isPremium)
                    Divider()
                    ProFeatureRow(Icons.Filled.Palette, "Premium themes", state.isPremium)
                    Divider()
                    ProFeatureRow(Icons.Filled.SaveAlt, "Backup & export", state.isPremium)
                }

                Spacer(Modifier.height(22.dp))

                // Personalization
                SectionHeader("Personalization")
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Palette,
                        title = "Theme",
                        subtitle = currentPalette.displayName + " · " + currentPalette.tagline,
                        tintAccent = true,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate("themes")
                        },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Filled.SaveAlt,
                        title = "Export to CSV",
                        subtitle = if (state.isPremium) {
                            "" + habits.size + " habits · spreadsheet-ready"
                        } else "Pro · backup your full history",
                        onClick = {
                            when {
                                !state.isPremium -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate("paywall")
                                }
                                habits.isEmpty() -> {
                                    toast = "No habits to export yet"
                                }
                                else -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    runCatching {
                                        val intent = CsvExporter.export(context, habits)
                                        context.startActivity(intent)
                                        toast = "CSV ready"
                                    }.onFailure {
                                        toast = "Export failed"
                                    }
                                }
                            }
                        },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Filled.DataObject,
                        title = "Export to JSON",
                        subtitle = if (state.isPremium) {
                            "Full backup · re-import friendly"
                        } else "Pro · developer-friendly backup",
                        onClick = {
                            when {
                                !state.isPremium -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate("paywall")
                                }
                                habits.isEmpty() -> {
                                    toast = "No habits to export yet"
                                }
                                else -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    runCatching {
                                        val intent = JsonExporter.export(context, habits)
                                        context.startActivity(intent)
                                        toast = "JSON ready"
                                    }.onFailure {
                                        toast = "Export failed"
                                    }
                                }
                            }
                        },
                    )
                }

                Spacer(Modifier.height(22.dp))

                // Preferences
                SectionHeader("Preferences")
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Notifications,
                        title = "Reminders",
                        subtitle = "Set per-habit on the habit screen",
                        onClick = { toast = "Open a habit to set reminders" },
                    )
                }

                Spacer(Modifier.height(22.dp))

                // About
                SectionHeader("About")
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Filled.Star,
                        title = "Rate Ember",
                        subtitle = "Help others discover the app",
                        onClick = { toast = "Opens Play Store rating" },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Filled.Email,
                        title = "Contact support",
                        subtitle = "hello@ember.app",
                        onClick = { toast = "Opens email" },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = "Help & FAQ",
                        onClick = { toast = "Opens help center" },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Filled.PrivacyTip,
                        title = "Privacy",
                        subtitle = "How Ember handles your data",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            navController.navigate("privacy")
                        },
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Filled.Description,
                        title = "Terms of service",
                        onClick = { toast = "Opens terms" },
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    "Ember · v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberColors.TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Made with fire.",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberColors.TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            // Cancel sheet
            AnimatedVisibility(
                visible = showCancelSheet,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(180)),
                modifier = Modifier.fillMaxSize(),
            ) {
                CancelSheet(
                    onDismiss = { showCancelSheet = false },
                    onConfirm = {
                        viewModel.cancelSubscription()
                        showCancelSheet = false
                        toast = "Subscription canceled"
                    },
                )
            }

            // Toast
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
private fun MembershipCard(
    isPremium: Boolean,
    plan: PremiumPlan,
    purchasedAt: Long,
    onUpgrade: () -> Unit,
) {
    val bg = if (isPremium) Brush.linearGradient(
        listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
    ) else Brush.linearGradient(
        listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
    )
    val onColor = if (isPremium) Color(0xFF1A0E07) else EmberColors.TextPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .border(
                1.dp,
                if (isPremium) Color(0x66FFFFFF) else Color(0x22FFFFFF),
                RoundedCornerShape(28.dp),
            )
            .clickable { onUpgrade() }
            .padding(20.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPremium) Brush.linearGradient(
                                listOf(Color(0x33FFFFFF), Color(0x22FFFFFF))
                            ) else Brush.linearGradient(
                                listOf(EmberColors.Accent, EmberColors.Primary)
                            )
                        )
                        .border(1.dp, if (isPremium) Color(0x66FFFFFF) else Color(0x55FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = if (isPremium) Color(0xFF1A0E07) else Color(0xFF1A0E07),
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isPremium) "Ember Pro" else "Free plan",
                        style = MaterialTheme.typography.titleLarge,
                        color = onColor,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        if (isPremium) when (plan) {
                            PremiumPlan.YEARLY -> "Yearly · renews automatically"
                            PremiumPlan.LIFETIME -> "Lifetime · yours forever"
                            else -> "Active"
                        } else "5 habits · basic stats",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPremium) Color(0xFF1A0E07).copy(alpha = 0.8f) else EmberColors.TextSecondary,
                    )
                }
            }

            if (isPremium && purchasedAt > 0L) {
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoChip(
                        label = "PURCHASED",
                        value = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            .format(Date(purchasedAt)),
                        onPremium = true,
                    )
                    Spacer(Modifier.width(10.dp))
                    if (plan == PremiumPlan.YEARLY) {
                        InfoChip(
                            label = "RENEWS",
                            value = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                                .format(Date(purchasedAt + 365L * 24 * 60 * 60 * 1000)),
                            onPremium = true,
                        )
                    } else {
                        InfoChip(label = "PLAN", value = "Lifetime", onPremium = true)
                    }
                }
            } else if (!isPremium) {
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Text(
                        "Upgrade — from $24.99/yr",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1A0E07),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, onPremium: Boolean) {
    val bg = if (onPremium) Color(0x33FFFFFF) else EmberColors.SurfaceHigh
    val labelColor = if (onPremium) Color(0xFF1A0E07).copy(alpha = 0.75f) else EmberColors.TextMuted
    val valueColor = if (onPremium) Color(0xFF1A0E07) else EmberColors.TextPrimary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.labelLarge, color = valueColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelSmall,
        color = EmberColors.Primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                )
            )
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(22.dp)),
    ) {
        Column { content() }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(Color(0x14FFFFFF)),
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tintAccent: Boolean = false,
    danger: Boolean = false,
    loading: Boolean = false,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val iconBg = when {
        danger -> Brush.linearGradient(listOf(EmberColors.Danger.copy(alpha = 0.35f), EmberColors.Danger.copy(alpha = 0.15f)))
        tintAccent -> Brush.linearGradient(listOf(EmberColors.Accent.copy(alpha = 0.4f), EmberColors.Primary.copy(alpha = 0.2f)))
        else -> Brush.linearGradient(listOf(EmberColors.SurfaceHigh, EmberColors.SurfaceElevated))
    }
    val iconTint = when {
        danger -> EmberColors.Danger
        tintAccent -> EmberColors.Accent
        else -> EmberColors.TextSecondary
    }
    val titleColor = if (danger) EmberColors.Danger else EmberColors.TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !loading) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg)
                .border(
                    1.dp,
                    if (tintAccent) EmberColors.Accent.copy(alpha = 0.35f)
                    else if (danger) EmberColors.Danger.copy(alpha = 0.35f)
                    else Color(0x14FFFFFF),
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                fontWeight = FontWeight.SemiBold,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmberColors.TextSecondary,
                )
            }
        }
        if (loading) {
            androidx.compose.material3.CircularProgressIndicator(
                color = EmberColors.Accent,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp),
            )
        } else if (trailingIcon != null) {
            Icon(
                trailingIcon,
                contentDescription = null,
                tint = EmberColors.TextMuted,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Text(
                "›",
                style = MaterialTheme.typography.titleLarge,
                color = EmberColors.TextMuted,
            )
        }
    }
}

@Composable
private fun ProFeatureRow(icon: ImageVector, title: String, unlocked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (unlocked) Brush.linearGradient(
                        listOf(EmberColors.Accent.copy(alpha = 0.4f), EmberColors.Primary.copy(alpha = 0.2f))
                    ) else Brush.linearGradient(
                        listOf(EmberColors.SurfaceHigh, EmberColors.SurfaceElevated)
                    )
                )
                .border(
                    1.dp,
                    if (unlocked) EmberColors.Accent.copy(alpha = 0.4f) else Color(0x14FFFFFF),
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (unlocked) EmberColors.Accent else EmberColors.TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = if (unlocked) EmberColors.TextPrimary else EmberColors.TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        if (unlocked) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(EmberColors.Accent, EmberColors.Primary)
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    "UNLOCKED",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1A0E07),
                    fontWeight = FontWeight.Black,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(EmberColors.SurfaceHigh)
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    "LOCKED",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(EmberColors.SurfaceElevated)
            .border(1.dp, Color(0x22FFFFFF), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = EmberColors.TextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun CancelSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(EmberColors.Surface)
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                ) {}
                .padding(24.dp),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x33FFFFFF)),
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    "Cancel subscription?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "You'll keep Pro access until the end of your billing period. After that, you'll lose unlimited habits, cloud sync, AI insights, themes, and widgets.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EmberColors.TextSecondary,
                )
                Spacer(Modifier.height(22.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(50))
                        .background(EmberColors.Danger.copy(alpha = 0.15f))
                        .border(1.dp, EmberColors.Danger.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .clickable { onConfirm() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Cancel subscription",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmberColors.Danger,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(50))
                        .background(EmberColors.SurfaceHigh)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(50))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Keep my subscription",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmberColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
