package app.mymultiverse.ammo.presentation.components

import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nav_today
import ammo.composeapp.generated.resources.nutrition_grocery_list
import ammo.composeapp.generated.resources.nutrition_meal_plan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MainTabIconArtTest {

    @Test
    fun mainTabKinds_mapToDistinctTransparentDrawables() {
        assertEquals(Res.drawable.nav_today, MainTabIconKind.Today.drawable())
        assertEquals(Res.drawable.nutrition_meal_plan, MainTabIconKind.MealPlan.drawable())
        assertEquals(Res.drawable.nutrition_grocery_list, MainTabIconKind.Grocery.drawable())
        assertNotEquals(
            MainTabIconKind.Today.drawable(),
            MainTabIconKind.MealPlan.drawable(),
        )
        assertNotEquals(
            MainTabIconKind.MealPlan.drawable(),
            MainTabIconKind.Grocery.drawable(),
        )
    }
}
