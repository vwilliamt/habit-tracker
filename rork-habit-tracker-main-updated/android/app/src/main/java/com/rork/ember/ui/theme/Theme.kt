package com.rork.ember.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.ember.data.ThemeRepository

/**
 * Mutable runtime palette. Composables that read these properties recompose when
 * the values change, allowing in-app theme switching without refactoring call sites.
 */
object EmberColors {
    var Background by mutableStateOf(Color(0xFF0E0A09))
    var Surface by mutableStateOf(Color(0xFF1A1311))
    var SurfaceElevated by mutableStateOf(Color(0xFF221917))
    var SurfaceHigh by mutableStateOf(Color(0xFF2C201D))
    var Outline by mutableStateOf(Color(0xFF3A2A26))

    var Primary by mutableStateOf(Color(0xFFFF8A3D))
    var PrimaryDeep by mutableStateOf(Color(0xFFE0571B))
    var Accent by mutableStateOf(Color(0xFFFFC36B))
    var AccentSoft by mutableStateOf(Color(0xFFFFD89A))
    var Coral by mutableStateOf(Color(0xFFFF6B6B))
    var Mint by mutableStateOf(Color(0xFF7BE0B5))
    var Sky by mutableStateOf(Color(0xFF6FB8FF))
    var Lilac by mutableStateOf(Color(0xFFB28CFF))
    var Rose by mutableStateOf(Color(0xFFFF8EC2))

    var TextPrimary by mutableStateOf(Color(0xFFFBF1E6))
    var TextSecondary by mutableStateOf(Color(0xFFB8A89C))
    var TextMuted by mutableStateOf(Color(0xFF6F5E55))

    var Success by mutableStateOf(Color(0xFF7BE0B5))
    var Danger by mutableStateOf(Color(0xFFFF6B6B))

    var GlowTop by mutableStateOf(Color(0x66FF7A2A))
    var GlowTopMid by mutableStateOf(Color(0x22FF7A2A))
    var GlowBottom by mutableStateOf(Color(0x22B28CFF))

    var OnAccent by mutableStateOf(Color(0xFF1A0E07))
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

/** A complete named color palette. */
data class EmberPalette(
    val key: String,
    val displayName: String,
    val tagline: String,
    val isPremium: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceHigh: Color,
    val outline: Color,
    val primary: Color,
    val primaryDeep: Color,
    val accent: Color,
    val accentSoft: Color,
    val coral: Color,
    val mint: Color,
    val sky: Color,
    val lilac: Color,
    val rose: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val success: Color,
    val danger: Color,
    val glowTop: Color,
    val glowTopMid: Color,
    val glowBottom: Color,
    val onAccent: Color,
)

object EmberPalettes {
    val Ember = EmberPalette(
        key = "ember",
        displayName = "Ember",
        tagline = "Warm cinematic glow",
        isPremium = false,
        background = Color(0xFF0E0A09),
        surface = Color(0xFF1A1311),
        surfaceElevated = Color(0xFF221917),
        surfaceHigh = Color(0xFF2C201D),
        outline = Color(0xFF3A2A26),
        primary = Color(0xFFFF8A3D),
        primaryDeep = Color(0xFFE0571B),
        accent = Color(0xFFFFC36B),
        accentSoft = Color(0xFFFFD89A),
        coral = Color(0xFFFF6B6B),
        mint = Color(0xFF7BE0B5),
        sky = Color(0xFF6FB8FF),
        lilac = Color(0xFFB28CFF),
        rose = Color(0xFFFF8EC2),
        textPrimary = Color(0xFFFBF1E6),
        textSecondary = Color(0xFFB8A89C),
        textMuted = Color(0xFF6F5E55),
        success = Color(0xFF7BE0B5),
        danger = Color(0xFFFF6B6B),
        glowTop = Color(0x66FF7A2A),
        glowTopMid = Color(0x22FF7A2A),
        glowBottom = Color(0x22B28CFF),
        onAccent = Color(0xFF1A0E07),
    )

    val Midnight = EmberPalette(
        key = "midnight",
        displayName = "Midnight",
        tagline = "Indigo dusk · electric violet",
        isPremium = true,
        background = Color(0xFF07091A),
        surface = Color(0xFF111430),
        surfaceElevated = Color(0xFF181C3D),
        surfaceHigh = Color(0xFF21264E),
        outline = Color(0xFF2C3160),
        primary = Color(0xFF8B7BFF),
        primaryDeep = Color(0xFF5A47E6),
        accent = Color(0xFF7FE0FF),
        accentSoft = Color(0xFFB3EDFF),
        coral = Color(0xFFFF7AC2),
        mint = Color(0xFF7BE0B5),
        sky = Color(0xFF6FB8FF),
        lilac = Color(0xFFB28CFF),
        rose = Color(0xFFFF8EC2),
        textPrimary = Color(0xFFE9EAFF),
        textSecondary = Color(0xFFA9ACD6),
        textMuted = Color(0xFF5F6293),
        success = Color(0xFF7BE0B5),
        danger = Color(0xFFFF6B6B),
        glowTop = Color(0x668B7BFF),
        glowTopMid = Color(0x227FE0FF),
        glowBottom = Color(0x22FF7AC2),
        onAccent = Color(0xFF07091A),
    )

    val Aurora = EmberPalette(
        key = "aurora",
        displayName = "Aurora",
        tagline = "Northern lights · green & teal",
        isPremium = true,
        background = Color(0xFF06120F),
        surface = Color(0xFF0E1F1A),
        surfaceElevated = Color(0xFF142A23),
        surfaceHigh = Color(0xFF1B362D),
        outline = Color(0xFF234639),
        primary = Color(0xFF4CE0A3),
        primaryDeep = Color(0xFF1FA378),
        accent = Color(0xFF7DD3FC),
        accentSoft = Color(0xFFBFE9FB),
        coral = Color(0xFFFFB07A),
        mint = Color(0xFF8FF0C3),
        sky = Color(0xFF7DD3FC),
        lilac = Color(0xFFC4B5FD),
        rose = Color(0xFFFF9FB8),
        textPrimary = Color(0xFFEFFBF4),
        textSecondary = Color(0xFFA4C2B7),
        textMuted = Color(0xFF5A766C),
        success = Color(0xFF4CE0A3),
        danger = Color(0xFFFF7A7A),
        glowTop = Color(0x664CE0A3),
        glowTopMid = Color(0x227DD3FC),
        glowBottom = Color(0x22C4B5FD),
        onAccent = Color(0xFF06120F),
    )

    val Sunset = EmberPalette(
        key = "sunset",
        displayName = "Sunset",
        tagline = "Pink dusk · magenta horizon",
        isPremium = true,
        background = Color(0xFF1A0A14),
        surface = Color(0xFF2A1023),
        surfaceElevated = Color(0xFF36172F),
        surfaceHigh = Color(0xFF421E3B),
        outline = Color(0xFF562B4C),
        primary = Color(0xFFFF5C8A),
        primaryDeep = Color(0xFFD12C66),
        accent = Color(0xFFFFB347),
        accentSoft = Color(0xFFFFD08A),
        coral = Color(0xFFFF7E7E),
        mint = Color(0xFF7BE0B5),
        sky = Color(0xFF93C5FD),
        lilac = Color(0xFFD0A4FF),
        rose = Color(0xFFFFB1D4),
        textPrimary = Color(0xFFFFF1F6),
        textSecondary = Color(0xFFCBA3B8),
        textMuted = Color(0xFF7E556B),
        success = Color(0xFF7BE0B5),
        danger = Color(0xFFFF5C8A),
        glowTop = Color(0x66FF5C8A),
        glowTopMid = Color(0x22FFB347),
        glowBottom = Color(0x22D0A4FF),
        onAccent = Color(0xFF1A0A14),
    )

    val Mono = EmberPalette(
        key = "mono",
        displayName = "Graphite",
        tagline = "Monochrome · paper white",
        isPremium = true,
        background = Color(0xFF0A0A0B),
        surface = Color(0xFF141416),
        surfaceElevated = Color(0xFF1C1C1F),
        surfaceHigh = Color(0xFF26262A),
        outline = Color(0xFF34343A),
        primary = Color(0xFFF2F2F4),
        primaryDeep = Color(0xFFC9C9CE),
        accent = Color(0xFFFFFFFF),
        accentSoft = Color(0xFFDADADE),
        coral = Color(0xFFE57373),
        mint = Color(0xFF9ED1B8),
        sky = Color(0xFFA8C5E0),
        lilac = Color(0xFFC4B5FD),
        rose = Color(0xFFE4A5C0),
        textPrimary = Color(0xFFF6F6F8),
        textSecondary = Color(0xFFA2A2A8),
        textMuted = Color(0xFF55555B),
        success = Color(0xFF9ED1B8),
        danger = Color(0xFFE57373),
        glowTop = Color(0x33FFFFFF),
        glowTopMid = Color(0x11FFFFFF),
        glowBottom = Color(0x11FFFFFF),
        onAccent = Color(0xFF0A0A0B),
    )

    val all: List<EmberPalette> = listOf(Ember, Midnight, Aurora, Sunset, Mono)

    fun byKey(key: String?): EmberPalette = all.firstOrNull { it.key == key } ?: Ember
}

fun applyPalette(p: EmberPalette) {
    EmberColors.Background = p.background
    EmberColors.Surface = p.surface
    EmberColors.SurfaceElevated = p.surfaceElevated
    EmberColors.SurfaceHigh = p.surfaceHigh
    EmberColors.Outline = p.outline
    EmberColors.Primary = p.primary
    EmberColors.PrimaryDeep = p.primaryDeep
    EmberColors.Accent = p.accent
    EmberColors.AccentSoft = p.accentSoft
    EmberColors.Coral = p.coral
    EmberColors.Mint = p.mint
    EmberColors.Sky = p.sky
    EmberColors.Lilac = p.lilac
    EmberColors.Rose = p.rose
    EmberColors.TextPrimary = p.textPrimary
    EmberColors.TextSecondary = p.textSecondary
    EmberColors.TextMuted = p.textMuted
    EmberColors.Success = p.success
    EmberColors.Danger = p.danger
    EmberColors.GlowTop = p.glowTop
    EmberColors.GlowTopMid = p.glowTopMid
    EmberColors.GlowBottom = p.glowBottom
    EmberColors.OnAccent = p.onAccent
}

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
fun AppTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    val ctx = LocalContext.current
    val repo = remember(ctx) { ThemeRepository(ctx.applicationContext) }
    val key by repo.themeKey.collectAsStateWithLifecycle(initialValue = "ember")

    LaunchedEffect(key) {
        applyPalette(EmberPalettes.byKey(key))
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    val colorScheme = darkColorScheme(
        primary = EmberColors.Primary,
        onPrimary = EmberColors.OnAccent,
        primaryContainer = EmberColors.PrimaryDeep,
        onPrimaryContainer = EmberColors.TextPrimary,
        secondary = EmberColors.Accent,
        onSecondary = EmberColors.OnAccent,
        background = EmberColors.Background,
        onBackground = EmberColors.TextPrimary,
        surface = EmberColors.Surface,
        onSurface = EmberColors.TextPrimary,
        surfaceVariant = EmberColors.SurfaceElevated,
        onSurfaceVariant = EmberColors.TextSecondary,
        outline = EmberColors.Outline,
        error = EmberColors.Danger,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EmberTypography,
        content = content,
    )
}
