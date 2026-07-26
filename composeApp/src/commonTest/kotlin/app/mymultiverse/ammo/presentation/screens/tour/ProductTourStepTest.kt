package app.mymultiverse.ammo.presentation.screens.tour

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductTourStepTest {

    @Test
    fun productTourSteps_followExpectedSpotlightSequence() {
        val steps = productTourSteps()

        assertEquals(
            listOf("welcome", "home_hub", "meal_plan_tab", "grocery_tab"),
            steps.map(ProductTourStep::id),
        )
        assertNull(steps[0].targetTag)
        assertEquals(ProductTourTestTags.TARGET_HOME_HUB, steps[1].targetTag)
        assertEquals(ProductTourTestTags.TARGET_MEAL_PLAN_TAB, steps[2].targetTag)
        assertEquals(ProductTourTestTags.TARGET_GROCERY_TAB, steps[3].targetTag)
    }
}
