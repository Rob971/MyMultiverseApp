package app.mymultiverse.ammo.presentation.theme

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AppIconsTest {

    @Test
    fun semanticIcons_doNotAliasSparklesOrWrongChevrons() {
        assertNotEquals(AppIcons.Sparkles.name, AppIcons.MoreVert.name)
        assertNotEquals(AppIcons.Sparkles.name, AppIcons.Refresh.name)
        assertNotEquals(AppIcons.Notifications.name, AppIcons.DateRange.name)
        assertNotEquals(AppIcons.ChevronRight.name, AppIcons.KeyboardArrowDown.name)
        assertNotEquals(AppIcons.ChevronLeft.name, AppIcons.KeyboardArrowUp.name)
        assertNotEquals(AppIcons.CheckCircle.name, AppIcons.Check.name)
        assertNotEquals(AppIcons.Person.name, AppIcons.CheckCircle.name)
        assertNotEquals(AppIcons.ShoppingCart.name, AppIcons.Restaurant.name)
        assertNotEquals(AppIcons.Lightbulb.name, AppIcons.Sparkles.name)
        assertNotEquals(AppIcons.DragHandle.name, AppIcons.MoreVert.name)
        assertNotEquals(AppIcons.GroceryList.name, AppIcons.ShoppingCart.name)
        assertNotEquals(AppIcons.MealPlan.name, AppIcons.DateRange.name)
        assertNotEquals(AppIcons.MealPlan.name, AppIcons.GroceryList.name)
        assertNotEquals(AppIcons.PlanLunchPlaceSetting.name, AppIcons.MealPlan.name)
        assertNotEquals(AppIcons.FreshGroceries.name, AppIcons.GroceryList.name)
        assertNotEquals(AppIcons.PlanLunchPlaceSetting.name, AppIcons.FreshGroceries.name)
        assertNotEquals(AppIcons.Household.name, AppIcons.Person.name)
        assertNotEquals(AppIcons.PersonAdd.name, AppIcons.Person.name)
        assertNotEquals(AppIcons.PersonAdd.name, AppIcons.Household.name)
        assertNotEquals(AppIcons.GroceryList.name, AppIcons.Check.name)
        assertNotEquals(AppIcons.KeepScreenOn.name, AppIcons.Lightbulb.name)
        assertNotEquals(AppIcons.SyncPending.name, AppIcons.Refresh.name)
        assertNotEquals(AppIcons.SyncOffline.name, AppIcons.SyncPending.name)
        assertNotEquals(AppIcons.PantryHave.name, AppIcons.CheckCircle.name)
        assertNotEquals(AppIcons.SyncSynced.name, AppIcons.CheckCircle.name)
        assertNotEquals(AppIcons.SyncSynced.name, AppIcons.SyncPending.name)
        assertNotEquals(AppIcons.Google.name, AppIcons.Apple.name)
        assertNotEquals(AppIcons.Adventures.name, AppIcons.Explore.name)
        assertNotEquals(AppIcons.BudgetWallet.name, AppIcons.AccountBalance.name)
        assertNotEquals(AppIcons.Adventures.name, AppIcons.BudgetWallet.name)
    }

    @Test
    fun navTabs_useApprovedHeroIcons() {
        assertEquals(AppIcons.PlanLunchPlaceSetting.name, AppIconRole.NavMealPlan.imageVector().name)
        assertEquals(AppIcons.FreshGroceries.name, AppIconRole.NavGrocery.imageVector().name)
    }

    @Test
    fun heroIcons_haveValidViewport() {
        listOf(AppIcons.PlanLunchPlaceSetting, AppIcons.FreshGroceries, AppIcons.Home).forEach { icon ->
            assertTrue(icon.viewportWidth > 0f)
            assertTrue(icon.viewportHeight > 0f)
            assertTrue(icon.name.isNotBlank())
        }
    }

    @Test
    fun heroIcons_haveStandardDefaultSize() {
        listOf(AppIcons.PlanLunchPlaceSetting, AppIcons.FreshGroceries).forEach { icon ->
            assertEquals(24.dp, icon.defaultWidth)
            assertEquals(24.dp, icon.defaultHeight)
        }
    }

    @Test
    fun heroIcons_doNotAliasLegacyNutritionVectors() {
        assertNotEquals(AppIcons.MealPlan.name, AppIcons.PlanLunchPlaceSetting.name)
        assertNotEquals(AppIcons.GroceryList.name, AppIcons.FreshGroceries.name)
        assertNotEquals(AppIcons.ShoppingCart.name, AppIcons.FreshGroceries.name)
        assertNotEquals(AppIcons.Restaurant.name, AppIcons.PlanLunchPlaceSetting.name)
    }

    @Test
    fun heroIcons_haveDrawablePathNodes() {
        ImageVectorDrawableAssertions.assertHasDrawablePaths(AppIcons.PlanLunchPlaceSetting, minPaths = 5)
        ImageVectorDrawableAssertions.assertHasDrawablePaths(AppIcons.FreshGroceries, minPaths = 7)
    }

    @Test
    fun navIcons_haveDrawablePathNodes() {
        ImageVectorDrawableAssertions.assertHasDrawablePaths(AppIcons.Home, minPaths = 1)
        ImageVectorDrawableAssertions.assertHasDrawablePaths(AppIconRole.NavMealPlan.imageVector(), minPaths = 5)
        ImageVectorDrawableAssertions.assertHasDrawablePaths(AppIconRole.NavGrocery.imageVector(), minPaths = 7)
    }

    @Test
    fun featureRoles_mapToDistinctVectors() {
        assertNotEquals(
            AppIconRole.NavGrocery.imageVector().name,
            AppIconRole.NavMealPlan.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.NavMealPlan.imageVector().name,
            AppIconRole.NavHome.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.Language.imageVector().name,
            AppIconRole.ChromeBack.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.KeepScreenOn.imageVector().name,
            AppIconRole.Hint.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.SyncPending.imageVector().name,
            AppIconRole.SyncOffline.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.SyncSuccess.imageVector().name,
            AppIconRole.GroceryChecked.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.PantryHave.imageVector().name,
            AppIconRole.GroceryChecked.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.SsoGoogle.imageVector().name,
            AppIconRole.SsoApple.imageVector().name,
        )
        assertNotEquals(
            AppIconRole.ComingSoonExplore.imageVector().name,
            AppIconRole.ComingSoonBudget.imageVector().name,
        )
    }
}
