package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeNutritionStatusLineTest {

    @Test
    fun format_returnsNullWhenSummaryMissing() {
        assertNull(
            HomeNutritionStatusLine.format(
                summary = null,
                groceryProgressLabel = { c, t -> "$c/$t" },
                mealPlanProgressLabel = { p, w -> "$p/$w" },
                emptyLabel = "Start",
            ),
        )
    }

    @Test
    fun format_returnsEmptyLabelWhenNoProgressYet() {
        assertEquals(
            "Start",
            HomeNutritionStatusLine.format(
                summary = HomeNutritionSummary(
                    weekKey = "2026-W25",
                    groceryProgress = null,
                    plannedMealSlots = 0,
                ),
                groceryProgressLabel = { c, t -> "Grocery $c/$t" },
                mealPlanProgressLabel = { p, w -> "Meals $p/$w" },
                emptyLabel = "Start",
            ),
        )
    }

    @Test
    fun format_joinsGroceryAndMealPlanProgress() {
        assertEquals(
            "Grocery 2/5 · Meals 3/14",
            HomeNutritionStatusLine.format(
                summary = HomeNutritionSummary(
                    weekKey = "2026-W25",
                    groceryProgress = NutritionHubSummary.GroceryProgress(checked = 2, total = 5),
                    plannedMealSlots = 3,
                ),
                groceryProgressLabel = { c, t -> "Grocery $c/$t" },
                mealPlanProgressLabel = { p, w -> "Meals $p/$w" },
                emptyLabel = "Start",
            ),
        )
    }
}
