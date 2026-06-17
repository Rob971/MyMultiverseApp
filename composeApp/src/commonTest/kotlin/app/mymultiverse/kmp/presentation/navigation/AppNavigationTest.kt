package app.mymultiverse.kmp.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Documents route shapes used by [app.mymultiverse.kmp.presentation.App].
 * Back-stack behaviour is covered by [AppNavigatorTest].
 */
class AppNavigationTest {

    @Test
    fun openNutritionFromHome_targetsHubByDefault() {
        val fromHome = AppRoute.Nutrition()

        assertEquals(NutritionSection.Hub, fromHome.section)
        assertEquals(null, fromHome.space)
    }

    @Test
    fun nutritionSections_areDistinctDestinations() {
        val sections = listOf(
            NutritionSection.Hub,
            NutritionSection.Grocery,
            NutritionSection.MealPlan,
            NutritionSection.AiAdvice,
        )

        assertEquals(sections.size, sections.distinct().size)
    }

    @Test
    fun nutritionRoute_storesMealPlanAndAiSections() {
        assertEquals(NutritionSection.MealPlan, AppRoute.Nutrition(section = NutritionSection.MealPlan).section)
        assertEquals(NutritionSection.AiAdvice, AppRoute.Nutrition(section = NutritionSection.AiAdvice).section)
    }

    @Test
    fun householdMembersRoute_startsWithoutPreselectedHousehold() {
        val route = AppRoute.HouseholdMembers()

        assertEquals(null, route.household)
    }

    @Test
    fun householdMembersRoute_storesHouseholdContext() {
        val household = HouseholdContext(
            id = "space-1",
            name = "Family",
            ownerId = "owner-1",
            ownerDisplayName = "Owner",
        )
        val route = AppRoute.HouseholdMembers(household = household)

        assertEquals("space-1", route.household?.id)
    }

    @Test
    fun backToHub_preservesNutritionContainer() {
        val space = NutritionSpaceContext(
            id = "space-1",
            name = "Home",
            ownerId = "owner-1",
            features = setOf(app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature.Grocery),
        )
        val grocery = AppRoute.Nutrition(space = space, section = NutritionSection.Grocery)
        val hub = AppRoute.Nutrition(space = space, section = NutritionSection.Hub)

        assertIs<AppRoute.Nutrition>(grocery)
        assertIs<AppRoute.Nutrition>(hub)
        assertEquals(NutritionSection.Hub, hub.section)
    }
}
