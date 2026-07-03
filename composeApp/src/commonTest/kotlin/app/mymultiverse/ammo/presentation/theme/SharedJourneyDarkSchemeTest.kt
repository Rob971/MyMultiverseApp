package app.mymultiverse.ammo.presentation.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedJourneyDarkSchemeTest {

    @Test
    fun darkScheme_usesDarkNapolitanSurfaceTokens() {
        val scheme = sharedJourneyDarkScheme()

        assertEquals(SharedJourneyColors.DarkBackground, scheme.background)
        assertEquals(SharedJourneyColors.DarkSurfaceCard, scheme.surface)
        assertEquals(SharedJourneyColors.DarkTextPrimary, scheme.onBackground)
        assertEquals(SharedJourneyColors.DarkTextPrimary, scheme.onSurface)
        assertEquals(SharedJourneyColors.AccentMintContainer, scheme.primaryContainer)
        assertEquals(SharedJourneyColors.AccentWarmGoldContainer, scheme.secondaryContainer)
    }

    @Test
    fun darkScheme_meetsAaContrastForCoreReadablePairs() {
        val scheme = sharedJourneyDarkScheme()

        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onBackground,
            background = scheme.background,
            label = "dark onBackground/background",
        )
        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onSurface,
            background = scheme.surface,
            label = "dark onSurface/surface",
        )
        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onPrimaryContainer,
            background = scheme.primaryContainer,
            label = "dark onPrimaryContainer/primaryContainer",
        )
        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onSecondaryContainer,
            background = scheme.secondaryContainer,
            label = "dark onSecondaryContainer/secondaryContainer",
        )
    }

    @Test
    fun darkTokens_keepBottomNavTextAndIconContrastAccessible() {
        ColorContrastAssertions.assertAaTextContrast(
            foreground = SharedJourneyColors.DarkTextMuted,
            background = SharedJourneyColors.DarkSurfaceElevated,
            label = "dark nav unselected text",
        )
        ColorContrastAssertions.assertAaNonTextContrast(
            foreground = SharedJourneyColors.AccentMintTeal,
            background = SharedJourneyColors.DarkSurfaceElevated,
            label = "dark nav selected icon",
        )
        ColorContrastAssertions.assertAaNonTextContrast(
            foreground = SharedJourneyColors.DarkTextMuted,
            background = SharedJourneyColors.DarkSurfaceElevated,
            label = "dark nav unselected icon",
        )
    }
}
