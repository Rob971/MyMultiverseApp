package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NutritionHubSummaryTest {

    @Test
    fun groceryProgress_returnsNullWhenListEmpty() {
        assertNull(NutritionHubSummary.groceryProgress(emptyList()))
    }

    @Test
    fun groceryProgress_countsCheckedItems() {
        val items = listOf(
            GroceryItem("1", "Milk", isChecked = true),
            GroceryItem("2", "Bread", isChecked = false),
            GroceryItem("3", "Eggs", isChecked = true),
        )

        val progress = NutritionHubSummary.groceryProgress(items)

        assertEquals(NutritionHubSummary.GroceryProgress(checked = 2, total = 3), progress)
    }

    @Test
    fun plannedDaysCount_countsDaysWithLunchOrDinner() {
        val days = listOf(
            DayMeals(lunch = "Soup", dinner = ""),
            DayMeals(),
            DayMeals(dinner = "Pasta"),
        ) + List(4) { DayMeals() }

        assertEquals(2, NutritionHubSummary.plannedDaysCount(days))
    }

    @Test
    fun plannedSlotsCount_countsIndividualMeals() {
        val days = listOf(
            DayMeals(lunch = "Soup", dinner = "Fish"),
            DayMeals(lunch = "Salad"),
        ) + List(5) { DayMeals() }

        assertEquals(3, NutritionHubSummary.plannedSlotsCount(days))
        assertEquals(
            NutritionHubSummary.MealPlanProgress(plannedSlots = 3),
            NutritionHubSummary.mealPlanProgress(days),
        )
    }
}
