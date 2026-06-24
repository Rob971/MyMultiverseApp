package app.mymultiverse.kmp.presentation.theme

import kotlin.test.Test
import kotlin.test.assertNotEquals

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
    }
}
