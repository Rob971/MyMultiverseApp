package app.mymultiverse.ammo.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemedNutritionIconPainterTest {

    @Test
    fun mainTabIconKinds_mapToDistinctLightAndDarkAssetPaths() {
        val today = MainTabIconKind.Today.assetPaths()
        val mealPlan = MainTabIconKind.MealPlan.assetPaths()
        val grocery = MainTabIconKind.Grocery.assetPaths()

        assertEquals(NutritionIconAssetPaths.NAV_TODAY_LIGHT, today.first)
        assertEquals(NutritionIconAssetPaths.NAV_TODAY_DARK, today.second)
        assertEquals(NutritionIconAssetPaths.MEAL_PLAN_LIGHT, mealPlan.first)
        assertEquals(NutritionIconAssetPaths.MEAL_PLAN_DARK, mealPlan.second)
        assertEquals(NutritionIconAssetPaths.GROCERY_LIGHT, grocery.first)
        assertEquals(NutritionIconAssetPaths.GROCERY_DARK, grocery.second)
    }

    @Test
    fun nutritionFeatureKinds_reuseMatchingMainTabAssetPaths() {
        assertEquals(
            MainTabIconKind.MealPlan.assetPaths(),
            NutritionFeatureKind.MealPlan.assetPaths(),
        )
        assertEquals(
            MainTabIconKind.Grocery.assetPaths(),
            NutritionFeatureKind.Grocery.assetPaths(),
        )
    }
}
