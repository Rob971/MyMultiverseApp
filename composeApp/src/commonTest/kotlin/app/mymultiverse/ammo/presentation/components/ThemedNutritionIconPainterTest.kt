package app.mymultiverse.ammo.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun darkAssetPaths_areMappedToDrawableDarkDirectory() {
        MainTabIconKind.entries.forEach { iconKind ->
            val (lightPath, darkPath) = iconKind.assetPaths()
            assertTrue(lightPath.contains("/drawable/"), "$iconKind light icon path should be drawable")
            assertTrue(darkPath.contains("/drawable-dark/"), "$iconKind dark icon path should be drawable-dark")
            assertTrue(lightPath != darkPath, "$iconKind should not reuse same light/dark path")
            assertEquals(
                lightPath.substringAfterLast('/'),
                darkPath.substringAfterLast('/'),
                "$iconKind light and dark variants should keep matching file names",
            )
        }
    }
}
