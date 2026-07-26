package app.mymultiverse.ammo.presentation.screens.tour

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Contract tests for the canonical tour step list — guards the wire-up that
 * instrumented smoke and [App] both depend on.
 */
class ProductTourCatalogTest {

    @Test
    fun defaultSteps_areFourStepsWithExpectedTargets() {
        val steps = ProductTourCatalog.defaultSteps()
        assertEquals(4, steps.size)
        assertEquals("welcome", steps[0].id)
        assertNull(steps[0].targetTag)
        assertEquals("home_hub", steps[1].id)
        assertEquals(ProductTourTestTags.TARGET_HOME_HUB, steps[1].targetTag)
        assertEquals(ProductTourTestTags.TARGET_MEAL_PLAN_TAB, steps[2].targetTag)
        assertEquals(ProductTourTestTags.TARGET_GROCERY_TAB, steps[3].targetTag)
    }

    @Test
    fun defaultSteps_idsAreUnique() {
        val steps = ProductTourCatalog.defaultSteps()
        assertEquals(steps.size, steps.map { it.id }.toSet().size)
        assertTrue(steps.all { it.id.isNotBlank() })
    }
}
