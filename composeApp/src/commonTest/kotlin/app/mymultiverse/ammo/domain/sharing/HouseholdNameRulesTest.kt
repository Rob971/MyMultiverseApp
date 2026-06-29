package app.mymultiverse.ammo.domain.sharing

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseholdNameRulesTest {

    @Test
    fun normalizeForUniqueness_collapsesWhitespaceAndLowercases() {
        assertTrue(
            HouseholdNameRules.normalizeForUniqueness("  Garcia   Family  ") ==
                HouseholdNameRules.normalizeForUniqueness("garcia family"),
        )
    }

    @Test
    fun validationError_rejectsBlankAndTooShortNames() {
        assertTrue(HouseholdNameRules.validationError(" ") != null)
        assertTrue(HouseholdNameRules.validationError("a") != null)
    }

    @Test
    fun validationError_acceptsValidNames() {
        assertTrue(HouseholdNameRules.validationError("Garcia Family") == null)
    }
}
