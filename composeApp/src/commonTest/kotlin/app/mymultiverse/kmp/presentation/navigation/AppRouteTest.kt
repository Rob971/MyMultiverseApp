package app.mymultiverse.kmp.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AppRouteTest {

    @Test
    fun nutritionRoute_defaultsToHubSection() {
        val route = AppRoute.Nutrition()

        assertEquals(NutritionSection.Hub, route.section)
        assertEquals(null, route.space)
    }

    @Test
    fun nutritionRoute_storesRequestedSection() {
        val route = AppRoute.Nutrition(section = NutritionSection.MealPlan)

        assertEquals(NutritionSection.MealPlan, route.section)
    }

    @Test
    fun householdMembersRoute_defaultsToGateWhenHouseholdMissing() {
        val route = AppRoute.HouseholdMembers()

        assertEquals(null, route.household)
    }

    @Test
    fun homeRoute_isSingleton() {
        assertIs<AppRoute.Home>(AppRoute.Home)
    }
}
