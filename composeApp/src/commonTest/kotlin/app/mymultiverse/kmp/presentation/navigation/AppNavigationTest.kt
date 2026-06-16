package app.mymultiverse.kmp.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Documents the in-memory navigation contract used by [app.mymultiverse.kmp.presentation.App].
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
            NutritionSection.Members,
        )

        assertEquals(sections.size, sections.distinct().size)
    }

    @Test
    fun membersRoute_requiresSpaceContext() {
        val space = NutritionSpaceContext(
            id = "space-1",
            name = "Family",
            ownerId = "owner-1",
            features = setOf(app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature.Grocery),
        )
        val members = AppRoute.Nutrition(space = space, section = NutritionSection.Members)

        assertEquals(NutritionSection.Members, members.section)
        assertEquals("space-1", members.space?.id)
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
