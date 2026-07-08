package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
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
    fun generateGroceryList_localizesFoodSuggestionsForSelectedLanguage() {
        val items = NutritionAiPlanner.generateGroceryList(
            criteria = "Almuerzos proteicos para la familia",
            languageCode = "es",
        )

        assertTrue("Pechuga de pollo" in items)
        assertTrue("Yogur griego" in items)
        assertTrue("Chicken breast" !in items)
    }

    @Test
    fun generateGroceryForMeal_localizesMealIngredientSuggestions() {
        val items = NutritionAiPlanner.generateGroceryForMeal(
            mealDescription = "Pasta con pollo",
            languageCode = "fr",
        )

        assertTrue("Blanc de poulet" in items)
        assertTrue("Huile d'olive" in items)
        assertTrue("Chicken breast" !in items)
    }

    @Test
    fun generateMealPlan_matchesLocalizedQuickPickCriteria() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Comidas altas en proteína",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
        )

        assertTrue(result.summary.contains("high-protein"))
    }

    @Test
    fun generateMealPlan_quickCriteria_usesQuickProfile() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Quick 20-min lunch",
            scope = MealPlanGenerationScope.SingleDay(0),
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
        )

        assertTrue(result.days[0].lunch.contains("20-min", ignoreCase = true))
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
