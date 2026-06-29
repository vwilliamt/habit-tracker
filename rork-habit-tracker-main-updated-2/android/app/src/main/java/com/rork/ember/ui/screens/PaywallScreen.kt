package com.rork.ember.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rork.ember.billing.PlanOffer
import com.rork.ember.data.PremiumPlan
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.theme.EmberColors
import com.rork.ember.ui.viewmodel.PremiumViewModel
import android.app.Activity
import android.widget.Toast

private data class PerkRow(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

/**
 * Centralized pricing copy for the paywall. Update these to match your
 * Play Console subscription/IAP product prices.
 */
private object PaywallPricing {
    const val WEEKLY_PRICE = "$1.99"
    const val WEEKLY_CADENCE = "/week"
    const val WEEKLY_NOTE = "Billed weekly · cancel anytime"

    const val MONTHLY_PRICE = "$4.99"
    const val MONTHLY_CADENCE = "/month"
    const val MONTHLY_NOTE = "Billed monthly · cancel anytime"

    const val YEARLY_PRICE = "$39.99"
    const val YEARLY_CADENCE = "/year"
    const val YEARLY_NOTE = "Just $3.33/month · billed annually"

    const val LIFETIME_PRICE = "$79.99"
    const val LIFETIME_STRIKETHROUGH = "$129.99"
    const val LIFETIME_CADENCE = "once"
    const val LIFETIME_NOTE = "Pay once · yours forever"
}

private val Perks = listOf(
    PerkRow(Icons.Filled.Block, "No ads", "A completely ad-free experience, everywhere"),
    PerkRow(Icons.Filled.Widgets, "Home screen widgets", "Track streaks from your lock & home screen"),
    PerkRow(Icons.Filled.Bolt, "Unlimited habits", "Build the full ritual stack — no caps"),
    PerkRow(Icons.Filled.CloudSync, "Cloud sync", "Encrypted backup across all your devices"),
    PerkRow(Icons.Filled.AutoAwesome, "AI insights", "Personal coach that learns your patterns"),
    PerkRow(Icons.Filled.Insights, "Advanced analytics", "Heatmaps, trends, and time-of-day stats"),
    PerkRow(Icons.Filled.Palette, "Premium themes", "Cinematic palettes & icon packs"),
    PerkRow(Icons.Filled.SaveAlt, "Backup & export", "CSV exports and one-tap restore"),
)

@Composable
fun PaywallScreen(
    navController: NavController,
    viewModel: PremiumViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val purchasing by viewModel.purchasing.collectAsStateWithLifecycle()
    val justPurchased by viewModel.justPurchased.collectAsStateWithLifecycle()
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf(PremiumPlan.YEARLY) }
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeError()
        }
    }

    LaunchedEffect(justPurchased) {
        if (justPurchased) {
            kotlinx.coroutines.delay(1400)
            viewModel.acknowledgePurchase()
            navController.popBackStack()
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
                    Spacer(Modifier.weight(1f))
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
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = EmberColors.TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Hero crown
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(140.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    GlowOrb()
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
                                )
                            )
                            .border(1.dp, Color(0x66FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFF1A0E07),
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Ember Pro",
                    style = MaterialTheme.typography.displayMedium,
                    color = EmberColors.TextPrimary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Unlock everything. Build the life you want.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EmberColors.TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.height(26.dp))

                // Perks
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                            )
                        )
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Perks.forEach { PerkItem(it) }
                    }
                }

                Spacer(Modifier.height(26.dp))

                // Plans
                PlanCard(
                    title = "Weekly",
                    price = offers[PremiumPlan.WEEKLY]?.formattedPrice ?: PaywallPricing.WEEKLY_PRICE,
                    cadence = PaywallPricing.WEEKLY_CADENCE,
                    note = PaywallPricing.WEEKLY_NOTE,
                    badge = null,
                    selected = selected == PremiumPlan.WEEKLY,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selected = PremiumPlan.WEEKLY
                    },
                )
                Spacer(Modifier.height(12.dp))
                PlanCard(
                    title = "Monthly",
                    price = offers[PremiumPlan.MONTHLY]?.formattedPrice ?: PaywallPricing.MONTHLY_PRICE,
                    cadence = PaywallPricing.MONTHLY_CADENCE,
                    note = PaywallPricing.MONTHLY_NOTE,
                    badge = null,
                    selected = selected == PremiumPlan.MONTHLY,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selected = PremiumPlan.MONTHLY
                    },
                )
                Spacer(Modifier.height(12.dp))
                PlanCard(
                    title = "Yearly",
                    price = offers[PremiumPlan.YEARLY]?.formattedPrice ?: PaywallPricing.YEARLY_PRICE,
                    cadence = PaywallPricing.YEARLY_CADENCE,
                    note = PaywallPricing.YEARLY_NOTE,
                    badge = "MOST POPULAR",
                    selected = selected == PremiumPlan.YEARLY,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selected = PremiumPlan.YEARLY
                    },
                )
                Spacer(Modifier.height(12.dp))
                PlanCard(
                    title = "Lifetime",
                    price = offers[PremiumPlan.LIFETIME]?.formattedPrice ?: PaywallPricing.LIFETIME_PRICE,
                    cadence = PaywallPricing.LIFETIME_CADENCE,
                    note = PaywallPricing.LIFETIME_NOTE,
                    badge = "BEST VALUE",
                    selected = selected == PremiumPlan.LIFETIME,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selected = PremiumPlan.LIFETIME
                    },
                )

                Spacer(Modifier.height(22.dp))

                // CTA
                val selectedPrice = offers[selected]?.formattedPrice
                ContinueButton(
                    label = when {
                        state.isPremium -> "You're a member"
                        selected == PremiumPlan.LIFETIME -> "Unlock forever — ${selectedPrice ?: PaywallPricing.LIFETIME_PRICE}"
                        selected == PremiumPlan.YEARLY -> "Start — ${selectedPrice ?: PaywallPricing.YEARLY_PRICE}/year"
                        selected == PremiumPlan.MONTHLY -> "Start — ${selectedPrice ?: PaywallPricing.MONTHLY_PRICE}/month"
                        selected == PremiumPlan.WEEKLY -> "Start — ${selectedPrice ?: PaywallPricing.WEEKLY_PRICE}/week"
                        else -> "Continue"
                    },
                    enabled = !purchasing && !state.isPremium,
                    loading = purchasing,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val activity = context as? Activity
                        if (activity != null) {
                            viewModel.purchase(activity, selected)
                        }
                    },
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextLink(
                        icon = Icons.Filled.Restore,
                        text = "Restore purchases",
                        onClick = { viewModel.restore() },
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Cancel anytime. No ads. No tracking.",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberColors.TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            // Success overlay
            AnimatedVisibility(
                visible = justPurchased,
                enter = fadeIn(tween(200)) + scaleIn(tween(300), initialScale = 0.85f),
                exit = fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.Center),
            ) {
                SuccessOverlay()
            }
        }
    }
}

@Composable
private fun GlowOrb() {
    val transition = rememberInfiniteTransition(label = "orb")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbAngle",
    )
    Box(
        modifier = Modifier
            .size(140.dp)
            .rotate(angle)
            .background(
                Brush.sweepGradient(
                    listOf(
                        EmberColors.Primary.copy(alpha = 0.0f),
                        EmberColors.Accent.copy(alpha = 0.45f),
                        EmberColors.Primary.copy(alpha = 0.0f),
                        EmberColors.PrimaryDeep.copy(alpha = 0.35f),
                        EmberColors.Primary.copy(alpha = 0.0f),
                    )
                ),
                shape = CircleShape,
            )
    )
}

@Composable
private fun PerkItem(perk: PerkRow) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(EmberColors.Accent.copy(alpha = 0.35f), EmberColors.Primary.copy(alpha = 0.15f))
                    )
                )
                .border(1.dp, EmberColors.Accent.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(perk.icon, contentDescription = null, tint = EmberColors.Accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                perk.title,
                style = MaterialTheme.typography.titleMedium,
                color = EmberColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                perk.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = EmberColors.TextSecondary,
            )
        }
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = EmberColors.Primary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    cadence: String,
    note: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) EmberColors.Accent else Color(0x22FFFFFF)
    val bg = if (selected) Brush.linearGradient(
        listOf(EmberColors.Accent.copy(alpha = 0.18f), EmberColors.Primary.copy(alpha = 0.08f))
    ) else Brush.linearGradient(
        listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) Brush.linearGradient(
                                listOf(EmberColors.Accent, EmberColors.Primary)
                            ) else Brush.linearGradient(
                                listOf(Color(0x22FFFFFF), Color(0x11FFFFFF))
                            )
                        )
                        .border(
                            1.dp,
                            if (selected) EmberColors.Accent else Color(0x33FFFFFF),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1A0E07),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.linearGradient(
                                    listOf(EmberColors.Accent, EmberColors.Primary)
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1A0E07),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    price,
                    style = MaterialTheme.typography.displayMedium,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    cadence,
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                note,
                style = MaterialTheme.typography.bodyMedium,
                color = EmberColors.TextSecondary,
            )
            if (title == "Lifetime") {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        PaywallPricing.LIFETIME_STRIKETHROUGH,
                        style = MaterialTheme.typography.labelLarge,
                        color = EmberColors.TextMuted,
                        textDecoration = TextDecoration.LineThrough,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save 38% — launch offer",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmberColors.Accent,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueButton(
    label: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) Brush.linearGradient(
                    listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
                ) else Brush.linearGradient(
                    listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                )
            )
            .border(1.dp, if (enabled) Color(0x55FFFFFF) else Color(0x22FFFFFF), RoundedCornerShape(50))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color(0xFF1A0E07),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Text(
                label,
                style = MaterialTheme.typography.titleLarge,
                color = if (enabled) Color(0xFF1A0E07) else EmberColors.TextMuted,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TextLink(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = EmberColors.TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = EmberColors.TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SuccessOverlay() {
    Box(
        modifier = Modifier
            .scale(1f)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
                )
            )
            .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(28.dp))
            .padding(horizontal = 28.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.WorkspacePremium,
                contentDescription = null,
                tint = Color(0xFF1A0E07),
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Welcome to Ember Pro",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF1A0E07),
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "Everything is unlocked.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1A0E07).copy(alpha = 0.8f),
            )
        }
    }
}
