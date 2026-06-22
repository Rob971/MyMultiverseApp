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
    
    // Backgrounds & Surfaces
    val ParchmentWarm = Color(0xFFFFF9F2)
    val SunDrenchedWhite = Color(0xFFFFFBFA)
    val ParchmentWarmDark = Color(0xFF1A1816)
    val SunDrenchedWhiteDark = Color(0xFF242120)

    // Accents & Ink
    val InkDeep = Color(0xFF1C1C1C)
    val InkSecondary = Color(0xFF4F4F4F)
    val InkMuted = Color(0xFF5C5C5C)
    val InkOnDark = Color(0xFFF5F0EB)
    val InkMutedOnDark = Color(0xFFD0C8C0)
    val InkSecondaryOnDark = Color(0xFFE8E2DC)
    val SageSoft = Color(0xFF8DAA91)

    /** Brighter teal for outlines, nav, and links on dark surfaces (WCAG-friendly on #1A1816). */
    val MediterraneanTealOnDark = Color(0xFF6ECFD6)
    val CardElevatedDark = Color(0xFF2C2C2C)
    val NavBarDark = Color(0xFF1E1E1E)

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
    onSecondary = Color.White,
    tertiary = SharedJourneyColors.LemonZestYellow,
    onTertiary = SharedJourneyColors.InkOnDark,
    background = SharedJourneyColors.ParchmentWarmDark,
    surface = SharedJourneyColors.CardElevatedDark,
    onBackground = SharedJourneyColors.InkOnDark,
    onSurface = SharedJourneyColors.InkOnDark,
)
