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
    fun lightScheme_primaryContainer_isNotMaterialDefaultPurple() {
        val materialDefaultPurple = Color(0xFFEADDFF)
        assertNotEquals(materialDefaultPurple, sharedJourneyLightScheme().primaryContainer)
    }
}
