package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MealPlanPresentationTest {

    @Test
    fun isPlanned_whenLunchOrDinnerPresent() {
        assertFalse(MealPlanPresentation.isPlanned(DayMeals()))
        assertTrue(MealPlanPresentation.isPlanned(DayMeals(lunch = "Soup")))
        assertTrue(MealPlanPresentation.isPlanned(DayMeals(dinner = "Fish")))
    }

    @Test
    fun summaryText_joinsMealsOrReturnsFallback() {
        val day = DayMeals(lunch = "Pasta", dinner = "Salad")
        assertEquals("Pasta · Salad", MealPlanPresentation.summaryText(day, "Empty"))

        assertEquals("Empty", MealPlanPresentation.summaryText(DayMeals(), "Empty"))
    }
}
