package com.rork.ember.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Ember palette — warm, cinematic, dark by default
object EmberColors {
    val Background = Color(0xFF0E0A09)
    val Surface = Color(0xFF1A1311)
    val SurfaceElevated = Color(0xFF221917)
    val SurfaceHigh = Color(0xFF2C201D)
    val Outline = Color(0xFF3A2A26)

    val Primary = Color(0xFFFF8A3D)        // ember orange
    val PrimaryDeep = Color(0xFFE0571B)
    val Accent = Color(0xFFFFC36B)         // golden amber
    val AccentSoft = Color(0xFFFFD89A)
    val Coral = Color(0xFFFF6B6B)
    val Mint = Color(0xFF7BE0B5)
    val Sky = Color(0xFF6FB8FF)
    val Lilac = Color(0xFFB28CFF)
    val Rose = Color(0xFFFF8EC2)

    val TextPrimary = Color(0xFFFBF1E6)
    val TextSecondary = Color(0xFFB8A89C)
    val TextMuted = Color(0xFF6F5E55)

    val Success = Color(0xFF7BE0B5)
    val Danger = Color(0xFFFF6B6B)
}

val HabitPalette: List<Long> = listOf(
    0xFFFF8A3DL, // Primary
    0xFFFFC36BL, // Accent
    0xFFFF6B6BL, // Coral
    0xFF7BE0B5L, // Mint
    0xFF6FB8FFL, // Sky
    0xFFB28CFFL, // Lilac
    0xFFFF8EC2L, // Rose
    0xFFFFE066L, // Sun
)

private val DarkColorScheme = darkColorScheme(
    primary = EmberColors.Primary,
    onPrimary = Color(0xFF1A0E07),
    primaryContainer = EmberColors.PrimaryDeep,
    onPrimaryContainer = EmberColors.TextPrimary,
    secondary = EmberColors.Accent,
    onSecondary = Color(0xFF1A0E07),
    background = EmberColors.Background,
    onBackground = EmberColors.TextPrimary,
    surface = EmberColors.Surface,
    onSurface = EmberColors.TextPrimary,
    surfaceVariant = EmberColors.SurfaceElevated,
    onSurfaceVariant = EmberColors.TextSecondary,
    outline = EmberColors.Outline,
    error = EmberColors.Danger,
)

private val EmberTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.8).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.4.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.8.sp,
    ),
)

@Composable
fun AppTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = EmberTypography,
        content = content,
    )
}
