package app.mymultiverse.ammo.presentation.theme

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

    // Dark mode tokens — Option 1 deep slate palette (WCAG AA on slate surfaces)
    /** Primary app background — deep slate charcoal. */
    val DarkBackground = Color(0xFF12161A)
    /** Elevated shell / nav chrome — step above background. */
    val DarkSurfaceElevated = Color(0xFF252B33)
    /** Card and list row surfaces — elevated container slate. */
    val DarkSurfaceCard = Color(0xFF1E232A)
    /** Primary readable text on dark surfaces — soft off-white. */
    val DarkTextPrimary = Color(0xFFE2E8F0)
    /** Secondary labels — owner names, metadata. */
    val DarkTextSecondary = Color(0xFFB8C5D0)
    /** Muted captions and role chips. */
    val DarkTextMuted = Color(0xFF8B9AAB)
    /** Subtle borders, dividers, and muted accent rings on dark surfaces. */
    val DarkOutline = Color(0xFF4A5568)
    /** Mint/teal accent — Plan Lunch primary actions in dark mode. */
    val AccentMintTeal = Color(0xFF76C79C)
    /** Warm gold accent — Shopping List secondary rings in dark mode. */
    val AccentWarmGold = Color(0xFFE5C478)
    /** Muted mint container / ring fill behind primary accents. */
    val AccentMintContainer = Color(0xFF2A3D34)
    /** Muted gold container / ring fill behind secondary accents. */
    val AccentWarmGoldContainer = Color(0xFF3D3528)

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

    /** Brighter teal for outlines, nav, and links on dark surfaces. */
    val MediterraneanTealOnDark = AccentMintTeal
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

fun sharedJourneyDarkScheme() = appDarkColorScheme()
