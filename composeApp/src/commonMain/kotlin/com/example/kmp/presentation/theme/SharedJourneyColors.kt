package com.example.kmp.presentation.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Option 2 — "Shared Journey Dashboard": grounded, organic, parchment warmth.
 * Terracotta primary, sage secondary, warm parchment surfaces.
 */
object SharedJourneyColors {
    val Parchment = Color(0xFFFFF8F0)
    val ParchmentSurface = Color(0xFFFFFBF7)
    val Terracotta = Color(0xFFE2725B)
    val Sage = Color(0xFF4F7942)
    val WarmBeige = Color(0xFFE8DCC8)
    val InkBrown = Color(0xFF3D2914)
    val InkMuted = Color(0xFF5C4D3F)
    val OutlineWarm = Color(0xFFD7C4B5)
    val SageMuted = Color(0xFF6B9A5E)
    val TerracottaDeep = Color(0xFFB85A47)
}

fun sharedJourneyLightScheme() = lightColorScheme(
    primary = SharedJourneyColors.Terracotta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF5C1A0E),
    secondary = SharedJourneyColors.Sage,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4E8CC),
    onSecondaryContainer = Color(0xFF1A3315),
    tertiary = Color(0xFFC9A66B),
    onTertiary = Color(0xFF2B1F08),
    background = SharedJourneyColors.Parchment,
    onBackground = SharedJourneyColors.InkBrown,
    surface = SharedJourneyColors.ParchmentSurface,
    onSurface = SharedJourneyColors.InkBrown,
    surfaceVariant = Color(0xFFF2EBE3),
    onSurfaceVariant = SharedJourneyColors.InkMuted,
    outline = SharedJourneyColors.OutlineWarm,
    outlineVariant = Color(0xFFE5D9CE),
)
