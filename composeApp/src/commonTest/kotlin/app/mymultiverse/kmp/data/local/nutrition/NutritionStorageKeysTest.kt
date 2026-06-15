package app.mymultiverse.kmp.data.local.nutrition

import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionStorageKeysTest {

    @Test
    fun personalKeys_doNotIncludeSpaceId() {
        assertEquals("nutrition_grocery_2026-W24", NutritionStorageKeys.grocery(null, "2026-W24"))
        assertEquals("nutrition_ai_grocery_2026-W24", NutritionStorageKeys.aiGrocery(null, "2026-W24"))
        assertEquals("nutrition_meal_plan_2026-W24", NutritionStorageKeys.mealPlan(null, "2026-W24"))
    }

    @Test
    fun spaceKeys_includeSpaceId() {
        val spaceId = "space-abc"
        assertEquals("nutrition_${spaceId}_grocery_2026-W24", NutritionStorageKeys.grocery(spaceId, "2026-W24"))
        assertEquals("nutrition_${spaceId}_ai_grocery_2026-W24", NutritionStorageKeys.aiGrocery(spaceId, "2026-W24"))
        assertEquals("nutrition_${spaceId}_meal_plan_2026-W24", NutritionStorageKeys.mealPlan(spaceId, "2026-W24"))
    }
}
