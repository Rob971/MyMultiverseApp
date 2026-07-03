package app.mymultiverse.ammo.presentation.components

import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_grocery_list
import ammo.composeapp.generated.resources.nutrition_meal_plan
import app.mymultiverse.ammo.presentation.theme.AppIcons
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionFeatureArtTest {

    @Test
    fun featureKinds_mapToDistinctDrawables() {
        assertEquals(
            Res.drawable.nutrition_meal_plan,
            NutritionFeatureKind.MealPlan.drawable(),
        )
        assertEquals(
            Res.drawable.nutrition_grocery_list,
            NutritionFeatureKind.Grocery.drawable(),
        )
    }

    @Test
    fun featureKinds_mapToVectorIcons() {
        assertEquals(AppIcons.MealPlan.name, NutritionFeatureKind.MealPlan.icon().name)
        assertEquals(AppIcons.GroceryList.name, NutritionFeatureKind.Grocery.icon().name)
    }
}
