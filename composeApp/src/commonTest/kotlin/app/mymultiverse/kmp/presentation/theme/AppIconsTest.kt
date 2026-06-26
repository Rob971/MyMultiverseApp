package app.mymultiverse.kmp.presentation.theme

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AppIconsTest {

    private val allIcons: List<ImageVector> = listOf(
        AppIcons.Person,
        AppIcons.ShoppingCart,
        AppIcons.Refresh,
        AppIcons.ChevronLeft,
        AppIcons.ChevronRight,
        AppIcons.ArrowBack,
        AppIcons.Add,
        AppIcons.Delete,
        AppIcons.Edit,
        AppIcons.CheckCircle,
        AppIcons.RadioButtonUnchecked,
        AppIcons.Notifications,
        AppIcons.Sparkles,
        AppIcons.Restaurant,
        AppIcons.Explore,
        AppIcons.AccountBalance,
        AppIcons.Check,
        AppIcons.MoreVert,
        AppIcons.DateRange,
        AppIcons.KeyboardArrowDown,
        AppIcons.KeyboardArrowUp,
        AppIcons.Home,
        AppIcons.Close,
        AppIcons.Lightbulb,
        AppIcons.Language,
        AppIcons.DragHandle,
        AppIcons.MealPlan,
        AppIcons.GroceryList,
        AppIcons.Household,
        AppIcons.PersonAdd,
    )

    private fun VectorGroup.hasDrawablePath(): Boolean = any { node: VectorNode ->
        when (node) {
            is VectorPath -> node.fill != null || node.stroke != null
            is VectorGroup -> node.hasDrawablePath()
        }
    }

    /**
     * Regression guard: a `path { }` contour with no [fill] (or [stroke]) brush draws nothing, so
     * the tinted [androidx.compose.material3.Icon] is invisible. Every icon must declare at least
     * one fillable or strokable contour.
     */
    @Test
    fun everyIcon_hasFillOrStroke() {
        allIcons.forEach { icon ->
            assertTrue(
                icon.root.hasDrawablePath(),
                "Icon '${icon.name}' has no fill/stroke and would render invisible.",
            )
        }
    }

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
        assertNotEquals(AppIcons.Household.name, AppIcons.Person.name)
        assertNotEquals(AppIcons.PersonAdd.name, AppIcons.Person.name)
        assertNotEquals(AppIcons.PersonAdd.name, AppIcons.Household.name)
        assertNotEquals(AppIcons.GroceryList.name, AppIcons.Check.name)
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
