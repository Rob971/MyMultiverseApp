package app.mymultiverse.kmp.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AppRouteTest {

    @Test
    fun nutritionRoute_defaultsToSpacesSection() {
        val route = AppRoute.Nutrition()

        assertEquals(NutritionSection.Spaces, route.section)
        assertEquals(null, route.space)
    }

    @Test
    fun nutritionRoute_storesRequestedSection() {
        val route = AppRoute.Nutrition(section = NutritionSection.MealPlan)

        assertEquals(NutritionSection.MealPlan, route.section)
    }

    @Test
    fun homeRoute_isSingleton() {
        assertIs<AppRoute.Home>(AppRoute.Home)
    }
}
