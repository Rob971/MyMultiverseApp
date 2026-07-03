package app.mymultiverse.ammo.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionFeatureArtTest {

    @Test
    fun nutritionFeatureKinds_mapToLightAndDarkAssetPaths() {
        assertEquals(
            NutritionIconAssetPaths.MEAL_PLAN_LIGHT to NutritionIconAssetPaths.MEAL_PLAN_DARK,
            NutritionFeatureKind.MealPlan.assetPaths(),
        )
        assertEquals(
            NutritionIconAssetPaths.GROCERY_LIGHT to NutritionIconAssetPaths.GROCERY_DARK,
            NutritionFeatureKind.Grocery.assetPaths(),
        )
    }
}
