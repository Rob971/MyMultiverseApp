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
        assertEquals(
            "Pasta · Salad",
            MealPlanPresentation.summaryText(
                day = day,
                notPlannedLabel = "Empty",
                unplannedSlotLabel = "Unplanned",
                lunchLabel = "Lunch",
                dinnerLabel = "Dinner",
            ),
        )

        assertEquals(
            "Empty",
            MealPlanPresentation.summaryText(
                day = DayMeals(),
                notPlannedLabel = "Empty",
                unplannedSlotLabel = "Unplanned",
                lunchLabel = "Lunch",
                dinnerLabel = "Dinner",
            ),
        )
    }

    @Test
    fun summaryText_marksMissingSlotAsUnplanned() {
        val day = DayMeals(lunch = "Soup", dinner = "")

        assertEquals(
            "Soup · Dinner: Unplanned",
            MealPlanPresentation.summaryText(
                day = day,
                notPlannedLabel = "Empty",
                unplannedSlotLabel = "Unplanned",
                lunchLabel = "Lunch",
                dinnerLabel = "Dinner",
            ),
        )
    }

    @Test
    fun plannedMeals_listsNonBlankSlots() {
        val days = listOf(
            DayMeals(lunch = "Soup", dinner = "Fish"),
            DayMeals(),
        ) + List(5) { DayMeals() }

        val planned = MealPlanPresentation.plannedMeals(days)

        assertEquals(2, planned.size)
        assertEquals(MealSlot.Lunch, planned.first().slot)
        assertEquals("Soup", planned.first().text)
    }

    @Test
    fun tomorrowIndex_wrapsWithinWeek() {
        assertEquals(1, MealPlanPresentation.tomorrowIndex(0))
        assertEquals(null, MealPlanPresentation.tomorrowIndex(6))
    }
}
