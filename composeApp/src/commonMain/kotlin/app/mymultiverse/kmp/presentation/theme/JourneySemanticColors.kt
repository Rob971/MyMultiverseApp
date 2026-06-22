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
}
