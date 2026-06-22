package app.mymultiverse.kmp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object JourneySemanticColors {
    @Composable
    fun inkDeep(): Color =
        if (isSystemInDarkTheme()) SharedJourneyColors.InkOnDark else SharedJourneyColors.InkDeep

    @Composable
    fun inkMuted(): Color =
        if (isSystemInDarkTheme()) SharedJourneyColors.InkMutedOnDark else SharedJourneyColors.InkMuted

    @Composable
    fun cardSurface(): Color =
        if (isSystemInDarkTheme()) SharedJourneyColors.GlassDark else SharedJourneyColors.GlassWhite

    @Composable
    fun elevatedSurface(): Color =
        if (isSystemInDarkTheme()) SharedJourneyColors.SunDrenchedWhiteDark else SharedJourneyColors.SunDrenchedWhite

    @Composable
    fun inkSecondary(): Color =
        if (isSystemInDarkTheme()) SharedJourneyColors.InkMutedOnDark else SharedJourneyColors.InkSecondary

    /** Elevated greeting banner — stays light in both themes for brand warmth. */
    @Composable
    fun bannerSurface(): Color = SharedJourneyColors.GlassWhite

    @Composable
    fun onBannerHeadline(): Color = SharedJourneyColors.MediterraneanTeal

    @Composable
    fun onBannerDescription(): Color =
        if (isSystemInDarkTheme()) SharedJourneyColors.InkSecondary else SharedJourneyColors.InkSecondary
}
