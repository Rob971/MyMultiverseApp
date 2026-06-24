package app.mymultiverse.kmp.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object JourneySemanticColors {
    @Composable
    private fun isDark(): Boolean = isAppInDarkTheme()

    @Composable
    fun inkDeep(): Color =
        if (isDark()) SharedJourneyColors.DarkTextPrimary else SharedJourneyColors.InkDeep

    @Composable
    fun inkMuted(): Color =
        if (isDark()) SharedJourneyColors.DarkTextMuted else SharedJourneyColors.InkMuted

    @Composable
    fun inkSecondary(): Color =
        if (isDark()) SharedJourneyColors.DarkTextSecondary else SharedJourneyColors.InkSecondary

    @Composable
    fun cardSurface(): Color =
        if (isDark()) SharedJourneyColors.DarkSurfaceCard else SharedJourneyColors.GlassWhite

    @Composable
    fun elevatedSurface(): Color =
        if (isDark()) SharedJourneyColors.DarkSurfaceElevated else SharedJourneyColors.SunDrenchedWhite

    /** Primary brand teal — mint accent on dark slate surfaces. */
    @Composable
    fun brandTeal(): Color =
        if (isDark()) SharedJourneyColors.AccentMintTeal else SharedJourneyColors.MediterraneanTeal

    /** Secondary accent — warm gold rings on dark; teal outline in light. */
    @Composable
    fun brandSecondaryAccent(): Color =
        if (isDark()) SharedJourneyColors.AccentWarmGold else SharedJourneyColors.MediterraneanTeal

    @Composable
    fun brandTerracotta(): Color = SharedJourneyColors.TerracottaOrange

    @Composable
    fun navIndicator(): Color = brandTeal().copy(alpha = 0.22f)

    @Composable
    fun subtleBorder(): Color = inkMuted().copy(alpha = if (isDark()) 0.35f else 0.2f)

    @Composable
    fun brandTealContainer(): Color = brandTeal().copy(alpha = if (isDark()) 0.18f else 0.08f)

    /** Elevated greeting banner — stays light in both themes for brand warmth. */
    @Composable
    fun bannerSurface(): Color = SharedJourneyColors.GlassWhite

    @Composable
    fun onBannerHeadline(): Color = SharedJourneyColors.MediterraneanTeal

    @Composable
    fun onBannerDescription(): Color = SharedJourneyColors.InkSecondary

    /** Icons and labels on filled accent buttons (teal / gold circles). */
    @Composable
    fun onAccentButton(): Color = SharedJourneyColors.SunDrenchedWhite

    /** Grocery checked state and sync success — readable on light and dark surfaces. */
    @Composable
    fun successAccent(): Color =
        if (isDark()) SharedJourneyColors.AccentMintTeal else SharedJourneyColors.SageSoft
}
