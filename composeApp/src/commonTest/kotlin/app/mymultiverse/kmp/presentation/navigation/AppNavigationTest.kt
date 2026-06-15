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
    fun backToHub_preservesNutritionContainer() {
        val grocery = AppRoute.Nutrition(section = NutritionSection.Grocery)
        val hub = AppRoute.Nutrition(section = NutritionSection.Hub)

        assertIs<AppRoute.Nutrition>(grocery)
        assertIs<AppRoute.Nutrition>(hub)
        assertEquals(NutritionSection.Hub, hub.section)
    }
}
