package app.mymultiverse.kmp.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object JourneySemanticColors {
    @Composable
    private fun isDark(): Boolean = isAppInDarkTheme()

    @Composable
    fun inkDeep(): Color =
        if (isDark()) SharedJourneyColors.InkOnDark else SharedJourneyColors.InkDeep

    @Composable
    fun inkMuted(): Color =
        if (isDark()) SharedJourneyColors.InkMutedOnDark else SharedJourneyColors.InkMuted

    @Composable
    fun inkSecondary(): Color =
        if (isDark()) SharedJourneyColors.InkSecondaryOnDark else SharedJourneyColors.InkSecondary

    @Composable
    fun cardSurface(): Color =
        if (isDark()) SharedJourneyColors.CardElevatedDark else SharedJourneyColors.GlassWhite

    @Composable
    fun elevatedSurface(): Color =
        if (isDark()) SharedJourneyColors.NavBarDark else SharedJourneyColors.SunDrenchedWhite

    /** Primary brand teal — dark surfaces need a lighter variant for contrast. */
    @Composable
    fun brandTeal(): Color =
        if (isDark()) SharedJourneyColors.MediterraneanTealOnDark else SharedJourneyColors.MediterraneanTeal

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
}
