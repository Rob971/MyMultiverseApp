package app.mymultiverse.ammo.presentation.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SharedJourneyLightSchemeTest {

    @Test
    fun lightScheme_usesNapolitanBrandAndContainers() {
        val scheme = sharedJourneyLightScheme()

        assertEquals(SharedJourneyColors.TerracottaOrange, scheme.primary)
        assertEquals(SharedJourneyColors.MediterraneanTeal, scheme.secondary)
        assertEquals(SharedJourneyColors.LightMintContainer, scheme.primaryContainer)
        assertEquals(SharedJourneyColors.LightGoldContainer, scheme.secondaryContainer)
        assertEquals(SharedJourneyColors.MediterraneanTeal, scheme.onPrimaryContainer)
        assertEquals(SharedJourneyColors.MediterraneanTeal, scheme.onSecondaryContainer)
        assertEquals(SharedJourneyColors.ParchmentWarm, scheme.background)
        assertEquals(SharedJourneyColors.SunDrenchedWhite, scheme.surface)
    }

    @Test
    fun lightScheme_onContainerTints_contrastWithHeroContainers() {
        val scheme = sharedJourneyLightScheme()

        assertNotEquals(scheme.primaryContainer, scheme.onPrimaryContainer)
        assertNotEquals(scheme.secondaryContainer, scheme.onSecondaryContainer)
        assertEquals(SharedJourneyColors.MediterraneanTeal, scheme.onPrimaryContainer)
        assertEquals(SharedJourneyColors.MediterraneanTeal, scheme.onSecondaryContainer)
        assertNotEquals(scheme.primary, scheme.onPrimaryContainer)
        assertNotEquals(scheme.secondary, scheme.secondaryContainer)
    }

    @Test
    fun lightScheme_meetsAaContrastForPrimaryTextAndContainers() {
        val scheme = sharedJourneyLightScheme()

        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onBackground,
            background = scheme.background,
            label = "light onBackground/background",
        )
        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onSurface,
            background = scheme.surface,
            label = "light onSurface/surface",
        )
        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onPrimaryContainer,
            background = scheme.primaryContainer,
            label = "light onPrimaryContainer/primaryContainer",
        )
        ColorContrastAssertions.assertAaTextContrast(
            foreground = scheme.onSecondaryContainer,
            background = scheme.secondaryContainer,
            label = "light onSecondaryContainer/secondaryContainer",
        )
    }

    @Test
    fun lightTokens_keepBottomNavIconsAboveNonTextContrastThreshold() {
        ColorContrastAssertions.assertAaNonTextContrast(
            foreground = SharedJourneyColors.MediterraneanTeal,
            background = SharedJourneyColors.SunDrenchedWhite,
            label = "light nav selected icon",
        )
        ColorContrastAssertions.assertAaNonTextContrast(
            foreground = SharedJourneyColors.InkMuted,
            background = SharedJourneyColors.SunDrenchedWhite,
            label = "light nav unselected icon",
        )
    }
}
