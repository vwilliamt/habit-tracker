package com.rork.ember.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.rork.ember.ui.components.EmberBackground
import com.rork.ember.ui.theme.EmberColors

private const val PRIVACY_URL = "https://ember.app/privacy"
private const val SUPPORT_EMAIL = "support@ember.app"

@Composable
fun PrivacyScreen(navController: NavController) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    EmberBackground {
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
                    "Privacy",
                    style = MaterialTheme.typography.titleLarge,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
                        )
                    )
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(28.dp))
                    .padding(22.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF))
                                .border(1.dp, Color(0x66FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = null,
                                tint = Color(0xFF1A0E07),
                                modifier = Modifier.size(26.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Your habits stay yours",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF1A0E07),
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                "On-device by default. No ads. No tracking.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1A0E07).copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))

            // Highlights
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PrivacyPoint(
                    icon = Icons.Filled.Storage,
                    title = "Stored on your device",
                    body = "Habits, streaks, reminders, and settings live in local app storage. We can't see them.",
                )
                PrivacyPoint(
                    icon = Icons.Filled.Block,
                    title = "No ads, no third-party analytics",
                    body = "Ember has no advertising or tracking SDKs. We never sell your data.",
                )
                PrivacyPoint(
                    icon = Icons.Filled.CloudOff,
                    title = "Optional cloud features",
                    body = "AI insights and cloud sync only send the minimum data required, encrypted over HTTPS, when you opt in.",
                )
                PrivacyPoint(
                    icon = Icons.Filled.Lock,
                    title = "You're in control",
                    body = "Export your data anytime, or delete it from Settings. Account deletion removes the cloud copy too.",
                )
            }

            Spacer(Modifier.height(28.dp))

            // Actions
            ActionButton(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                title = "Read the full privacy policy",
                subtitle = PRIVACY_URL,
                primary = true,
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, PRIVACY_URL.toUri())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }

            Spacer(Modifier.height(12.dp))

            ActionButton(
                icon = Icons.Filled.Mail,
                title = "Contact privacy support",
                subtitle = SUPPORT_EMAIL,
                primary = false,
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                runCatching {
                    val intent = Intent(Intent.ACTION_SENDTO, "mailto:$SUPPORT_EMAIL".toUri())
                        .putExtra(Intent.EXTRA_SUBJECT, "Ember privacy question")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Last updated May 13, 2026",
                style = MaterialTheme.typography.labelSmall,
                color = EmberColors.TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun PrivacyPoint(icon: ImageVector, title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
                )
            )
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                EmberColors.Accent.copy(alpha = 0.35f),
                                EmberColors.Primary.copy(alpha = 0.15f),
                            )
                        )
                    )
                    .border(1.dp, EmberColors.Accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = EmberColors.Accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmberColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (primary) Brush.linearGradient(
        listOf(EmberColors.Accent, EmberColors.Primary, EmberColors.PrimaryDeep)
    ) else Brush.linearGradient(
        listOf(EmberColors.SurfaceElevated, EmberColors.Surface)
    )
    val titleColor = if (primary) Color(0xFF1A0E07) else EmberColors.TextPrimary
    val subColor = if (primary) Color(0xFF1A0E07).copy(alpha = 0.75f) else EmberColors.TextSecondary
    val iconTint = if (primary) Color(0xFF1A0E07) else EmberColors.Accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                1.dp,
                if (primary) Color(0x66FFFFFF) else Color(0x1AFFFFFF),
                RoundedCornerShape(20.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                )
            }
            Text(
                "›",
                style = MaterialTheme.typography.titleLarge,
                color = if (primary) Color(0xFF1A0E07).copy(alpha = 0.7f) else EmberColors.TextMuted,
            )
        }
    }
}
