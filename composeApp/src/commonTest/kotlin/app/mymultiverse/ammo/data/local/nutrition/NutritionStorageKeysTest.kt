package app.mymultiverse.ammo.data.local.nutrition

import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionStorageKeysTest {

    @Test
    fun personalKeys_doNotIncludeHouseholdId() {
        assertEquals("nutrition_grocery_2026-W24", NutritionStorageKeys.grocery(null, "2026-W24"))
        assertEquals("nutrition_ai_grocery_2026-W24", NutritionStorageKeys.aiGrocery(null, "2026-W24"))
        assertEquals("nutrition_meal_plan_2026-W24", NutritionStorageKeys.mealPlan(null, "2026-W24"))
    }

    @Test
    fun householdKeys_includeHouseholdId() {
        val householdId = "household-abc"
        assertEquals("nutrition_${householdId}_grocery_2026-W24", NutritionStorageKeys.grocery(householdId, "2026-W24"))
        assertEquals("nutrition_${householdId}_ai_grocery_2026-W24", NutritionStorageKeys.aiGrocery(householdId, "2026-W24"))
        assertEquals("nutrition_${householdId}_meal_plan_2026-W24", NutritionStorageKeys.mealPlan(householdId, "2026-W24"))
    }
}
