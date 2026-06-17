package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import androidx.compose.runtime.mutableStateListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AppNavigatorTest {

    private val householdContext = HouseholdContext(
        id = "household-1",
        name = "Our household",
        ownerId = "owner-1",
        nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
    )

    private val household = HouseholdContext(
        id = "household-1",
        name = "Our household",
        ownerId = "owner-1",
        ownerDisplayName = "Owner",
    )

    @Test
    fun navigateBack_fromChild_returnsToHome() {
        val navigator = navigatorAt(AppRoute.Home)

        navigator.navigateTo(AppRoute.Nutrition())
        navigator.navigateBack()

        assertIs<AppRoute.Home>(navigator.current)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun replaceCurrent_afterNutritionGate_doesNotLeaveEntryRouteOnStack() {
        val navigator = navigatorAt(AppRoute.Home)
        navigator.navigateTo(AppRoute.Nutrition())

        navigator.replaceCurrent(
            AppRoute.Nutrition(household = householdContext, section = NutritionSection.Hub),
        )

        assertIs<AppRoute.Nutrition>(navigator.current)
        assertEquals(householdContext, (navigator.current as AppRoute.Nutrition).household)

        navigator.navigateBack()

        assertIs<AppRoute.Home>(navigator.current)
    }

    @Test
    fun replaceCurrent_afterHouseholdGate_doesNotLeaveEntryRouteOnStack() {
        val navigator = navigatorAt(AppRoute.Home)
        navigator.navigateTo(AppRoute.HouseholdMembers())

        navigator.replaceCurrent(AppRoute.HouseholdMembers(household = household))

        assertIs<AppRoute.HouseholdMembers>(navigator.current)
        assertEquals(household, (navigator.current as AppRoute.HouseholdMembers).household)

        navigator.navigateBack()

        assertIs<AppRoute.Home>(navigator.current)
    }

    @Test
    fun navigateBack_fromNutritionSection_returnsToHub() {
        val navigator = navigatorAt(AppRoute.Home)
        navigator.navigateTo(AppRoute.Nutrition(household = householdContext, section = NutritionSection.Hub))
        navigator.navigateTo(AppRoute.Nutrition(household = householdContext, section = NutritionSection.Grocery))

        navigator.navigateBack()

        val current = navigator.current
        assertIs<AppRoute.Nutrition>(current)
        assertEquals(NutritionSection.Hub, current.section)
        assertTrue(navigator.canGoBack)
    }

    @Test
    fun navigateBack_fromNutritionHub_returnsToHome() {
        val navigator = navigatorAt(AppRoute.Home)
        navigator.navigateTo(AppRoute.Nutrition(household = householdContext, section = NutritionSection.Hub))

        navigator.navigateBack()

        assertIs<AppRoute.Home>(navigator.current)
    }

    @Test
    fun navigateTo_sameRoute_isNoOp() {
        val navigator = navigatorAt(AppRoute.Home)
        val route = AppRoute.Nutrition(household = householdContext, section = NutritionSection.Hub)
        navigator.navigateTo(route)

        navigator.navigateTo(route)

        assertEquals(route, navigator.current)
        navigator.navigateBack()
        assertIs<AppRoute.Home>(navigator.current)
    }

    @Test
    fun replaceCurrent_sameRoute_isNoOp() {
        val navigator = navigatorAt(AppRoute.Home)
        val route = AppRoute.Nutrition(household = householdContext, section = NutritionSection.Hub)
        navigator.navigateTo(route)

        navigator.replaceCurrent(route)

        assertEquals(route, navigator.current)
        navigator.navigateBack()
        assertIs<AppRoute.Home>(navigator.current)
    }

    private fun navigatorAt(start: AppRoute): AppNavigator {
        val backStack = mutableStateListOf(start)
        return AppNavigator(backStack)
    }
}
