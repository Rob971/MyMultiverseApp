package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AppRouteTest {

    @Test
    fun nutritionRoute_defaultsToHubSection() {
        val route = AppRoute.Nutrition()

        assertEquals(NutritionSection.Hub, route.section)
        assertEquals(null, route.household)
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
    fun nutritionRoute_storesHouseholdContextWithNutritionFeatures() {
        val household = HouseholdContext(
            id = "household-1",
            name = "Family",
            ownerId = "owner-1",
            nutritionFeatures = setOf(NutritionSharingFeature.Grocery, NutritionSharingFeature.MealPlan),
        )
        val route = AppRoute.Nutrition(household = household, section = NutritionSection.Grocery)

        assertEquals(household, route.household)
        assertEquals(NutritionSection.Grocery, route.section)
    }

    @Test
    fun homeRoute_isSingleton() {
        assertIs<AppRoute.Home>(AppRoute.Home)
    }
}
