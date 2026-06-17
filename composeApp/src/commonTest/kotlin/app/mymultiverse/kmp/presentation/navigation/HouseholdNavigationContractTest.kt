package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HouseholdNavigationContractTest {

    @Test
    fun householdContext_exposesNutritionFeatureMembership() {
        val household = HouseholdContext(
            id = "household-1",
            name = "Our household",
            ownerId = "owner-1",
            nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
        )

        assertTrue(household.includesNutritionFeature(NutritionSharingFeature.Grocery))
        assertFalse(household.includesNutritionFeature(NutritionSharingFeature.MealPlan))
    }

    @Test
    fun openNutritionFromHome_targetsHubWithoutPreselectedHousehold() {
        val route = AppRoute.Nutrition()

        assertEquals(NutritionSection.Hub, route.section)
        assertEquals(null, route.household)
    }

    @Test
    fun householdContext_isSharedAcrossNutritionSections() {
        val householdContext = HouseholdContext(
            id = "household-1",
            name = "Our household",
            ownerId = "owner-1",
            nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
        )

        val grocery = AppRoute.Nutrition(household = householdContext, section = NutritionSection.Grocery)
        val householdMembers = AppRoute.HouseholdMembers(household = householdContext)

        assertEquals("household-1", grocery.household?.id)
        assertEquals("household-1", householdMembers.household?.id)
    }
}
