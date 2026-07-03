package app.mymultiverse.ammo.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MainTabIconArtTest {

    @Test
    fun nutritionFeatureKind_mapsToMatchingMainTabIconKind() {
        assertEquals(MainTabIconKind.MealPlan, NutritionFeatureKind.MealPlan.toMainTabIconKind())
        assertEquals(MainTabIconKind.Grocery, NutritionFeatureKind.Grocery.toMainTabIconKind())
    }

    @Test
    fun mainTabKinds_mapToDistinctLightAssetPaths() {
        assertEquals(NutritionIconAssetPaths.NAV_TODAY_LIGHT, MainTabIconKind.Today.lightAssetPath())
        assertEquals(NutritionIconAssetPaths.MEAL_PLAN_LIGHT, MainTabIconKind.MealPlan.lightAssetPath())
        assertEquals(NutritionIconAssetPaths.GROCERY_LIGHT, MainTabIconKind.Grocery.lightAssetPath())
        assertNotEquals(
            MainTabIconKind.Today.lightAssetPath(),
            MainTabIconKind.MealPlan.lightAssetPath(),
        )
        assertNotEquals(
            MainTabIconKind.MealPlan.lightAssetPath(),
            MainTabIconKind.Grocery.lightAssetPath(),
        )
    }
}
