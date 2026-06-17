package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.AppTopic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HouseholdNavigationContractTest {

    @Test
    fun allLogisticsTopics_existForSingleHouseholdFutureUse() {
        val topics = AppTopic.entries

        assertEquals(3, topics.size)
        assertTrue(topics.contains(AppTopic.Nutrition))
        assertTrue(topics.contains(AppTopic.Adventures))
        assertTrue(topics.contains(AppTopic.Budget))
    }

    @Test
    fun openNutritionFromHome_targetsHubWithoutPreselectedSpace() {
        val route = AppRoute.Nutrition()

        assertEquals(NutritionSection.Hub, route.section)
        assertEquals(null, route.space)
    }

    @Test
    fun householdContext_isSharedAcrossNutritionSections() {
        val space = NutritionSpaceContext(
            id = "household-1",
            name = "Our household",
            ownerId = "owner-1",
            features = setOf(app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature.Grocery),
        )

        val grocery = AppRoute.Nutrition(space = space, section = NutritionSection.Grocery)
        val householdMembers = AppRoute.HouseholdMembers(
            household = HouseholdContext(
                id = space.id,
                name = space.name,
                ownerId = space.ownerId,
                ownerDisplayName = space.ownerDisplayName,
            ),
        )

        assertEquals("household-1", grocery.space?.id)
        assertEquals("household-1", householdMembers.household?.id)
    }
}
