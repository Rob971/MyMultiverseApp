package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionAiPlannerTest {

    @Test
    fun generateGroceryList_returnsItemsForProteinKeywords() {
        val items = NutritionAiPlanner.generateGroceryList("high protein family")

        assertTrue(items.any { it.contains("Chicken", ignoreCase = true) || it.contains("yogurt", ignoreCase = true) })
        assertTrue(items.size >= 6)
    }

    @Test
    fun generateMealPlan_fullWeek_replacesAllDays() {
        val plan = WeeklyMealPlan(weekKey = "2026-05-18")
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "vegetarian",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = plan,
        )

        assertEquals(7, result.days.size)
        assertTrue(result.days.all { it.lunch.isNotBlank() && it.dinner.isNotBlank() })
    }

    @Test
    fun generateMealPlan_singleDay_updatesOnlyTargetDay() {
        val plan = WeeklyMealPlan(weekKey = "2026-05-18")
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "protein",
            scope = MealPlanGenerationScope.SingleDay(dayIndex = 2),
            currentPlan = plan,
        )

        assertTrue(result.days[2].lunch.isNotBlank())
        assertTrue(result.days[2].dinner.isNotBlank())
        assertEquals("", plan.days[0].lunch)
    }

    @Test
    fun generateGroceryForMeal_returnsItemsForMealDescription() {
        val items = NutritionAiPlanner.generateGroceryForMeal("Grilled chicken salad")

        assertTrue(items.isNotEmpty())
        assertTrue(items.size <= 12)
    }

    @Test
    fun generateGroceryForMeal_blankMeal_returnsEmpty() {
        assertEquals(emptyList(), NutritionAiPlanner.generateGroceryForMeal("   "))
    }
}
