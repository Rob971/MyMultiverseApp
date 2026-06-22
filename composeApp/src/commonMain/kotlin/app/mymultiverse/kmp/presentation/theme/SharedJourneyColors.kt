package app.mymultiverse.kmp.presentation.theme

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
    
    // Accents & Ink
    val InkDeep = Color(0xFF1C1C1C)
    val InkSecondary = Color(0xFF4F4F4F)
    val InkMuted = Color(0xFF5C5C5C)
    val SageSoft = Color(0xFF8DAA91)
    
    // Glassmorphism Bases
    val GlassWhite = Color(0xCCFFFFFF)
    val GlassTerracotta = Color(0x33E2725B)
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
