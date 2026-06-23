package app.mymultiverse.kmp.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 'Napolitan Heart' Visual Language: Calore & Appartenenza.
 */
object SharedJourneyColors {
    // Primary Palette
    val TerracottaOrange = Color(0xFFE2725B)
    val MediterraneanTeal = Color(0xFF005F6B)
    val LemonZestYellow = Color(0xFFF4D03F)
    
    // Backgrounds & Surfaces (light)
    val ParchmentWarm = Color(0xFFFFF9F2)
    val SunDrenchedWhite = Color(0xFFFFFBFA)

    // Dark mode tokens (WCAG AA: ≥4.5:1 body text on surfaces)
    /** Primary app background in dark theme (#121212). */
    val DarkBackground = Color(0xFF121212)
    /** Elevated shell / nav chrome (#1A1A1A). */
    val DarkSurfaceElevated = Color(0xFF1A1A1A)
    /** Card and list row surfaces (#1E1E1E). */
    val DarkSurfaceCard = Color(0xFF1E1E1E)
    /** Primary readable text on dark surfaces (#F5F5F5). */
    val DarkTextPrimary = Color(0xFFF5F5F5)
    /** Secondary labels — owner names, metadata (#B3B3B3, ~5.8:1 on #1E1E1E). */
    val DarkTextSecondary = Color(0xFFB3B3B3)
    /** Muted captions and role chips (#9E9E9E, ~4.6:1 on #1E1E1E). */
    val DarkTextMuted = Color(0xFF9E9E9E)
    /** Subtle borders and dividers on dark surfaces. */
    val DarkOutline = Color(0xFF5C5C5C)

    @Deprecated("Use DarkBackground", ReplaceWith("DarkBackground"))
    val ParchmentWarmDark = DarkBackground
    @Deprecated("Use DarkSurfaceElevated", ReplaceWith("DarkSurfaceElevated"))
    val SunDrenchedWhiteDark = DarkSurfaceElevated

    // Accents & Ink (light theme)
    val InkDeep = Color(0xFF1C1C1C)
    val InkSecondary = Color(0xFF4F4F4F)
    val InkMuted = Color(0xFF5C5C5C)

    @Deprecated("Use DarkTextPrimary", ReplaceWith("DarkTextPrimary"))
    val InkOnDark = DarkTextPrimary
    @Deprecated("Use DarkTextMuted", ReplaceWith("DarkTextMuted"))
    val InkMutedOnDark = DarkTextMuted
    @Deprecated("Use DarkTextSecondary", ReplaceWith("DarkTextSecondary"))
    val InkSecondaryOnDark = DarkTextSecondary
    val SageSoft = Color(0xFF8DAA91)

    /** Brighter teal for outlines, nav, and links on dark surfaces (WCAG-friendly on #121212). */
    val MediterraneanTealOnDark = Color(0xFF6ECFD6)
    @Deprecated("Use DarkSurfaceCard", ReplaceWith("DarkSurfaceCard"))
    val CardElevatedDark = DarkSurfaceCard
    @Deprecated("Use DarkSurfaceElevated", ReplaceWith("DarkSurfaceElevated"))
    val NavBarDark = DarkSurfaceElevated

    // Glassmorphism Bases
    val GlassWhite = Color(0xCCFFFFFF)
    val GlassDark = Color(0xD92A2826)
    val GlassTerracotta = Color(0x33E2725B)
    val AiReadOnlyAccent = TerracottaOrange
}

fun sharedJourneyLightScheme() = lightColorScheme(
    primary = SharedJourneyColors.TerracottaOrange,
    onPrimary = Color.White,
    secondary = SharedJourneyColors.MediterraneanTeal,
    onSecondary = Color.White,
    tertiary = SharedJourneyColors.LemonZestYellow,
    onTertiary = SharedJourneyColors.InkDeep,
    background = SharedJourneyColors.ParchmentWarm,
    surface = SharedJourneyColors.SunDrenchedWhite,
    onBackground = SharedJourneyColors.InkDeep,
    onSurface = SharedJourneyColors.InkDeep,
)

fun sharedJourneyDarkScheme() = darkColorScheme(
    primary = SharedJourneyColors.TerracottaOrange,
    onPrimary = Color.White,
    secondary = SharedJourneyColors.MediterraneanTealOnDark,
    onSecondary = SharedJourneyColors.DarkBackground,
    tertiary = SharedJourneyColors.LemonZestYellow,
    onTertiary = SharedJourneyColors.DarkBackground,
    background = SharedJourneyColors.DarkBackground,
    onBackground = SharedJourneyColors.DarkTextPrimary,
    surface = SharedJourneyColors.DarkSurfaceCard,
    onSurface = SharedJourneyColors.DarkTextPrimary,
    surfaceContainer = SharedJourneyColors.DarkSurfaceElevated,
    onSurfaceVariant = SharedJourneyColors.DarkTextMuted,
    outline = SharedJourneyColors.DarkOutline,
    error = SharedJourneyColors.TerracottaOrange,
    onError = Color.White,
)
