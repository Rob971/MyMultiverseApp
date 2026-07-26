package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.presentation.screens.home.HomeTestTags
import app.mymultiverse.ammo.presentation.screens.home.HomeWelcomeContent
import app.mymultiverse.ammo.presentation.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductTourInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeHubTourTarget_matchesDailyPlanningCard() {
        var tourTargetBounds: Rect? = null

        composeRule.setContent {
            AppTheme {
                HomeWelcomeContent(
                    greeting = Greeting("Ready"),
                    userDisplayName = "Roberto",
                    nutritionSummary = null,
                    isRefreshing = false,
                    onRefresh = {},
                    onOpenMealPlan = {},
                    onOpenGrocery = {},
                    onOpenHouseholdMembers = {},
                    tourTargetModifier = Modifier.onGloballyPositioned {
                        tourTargetBounds = it.boundsInRoot()
                    },
                )
            }
        }

        val dailyCardBounds = composeRule
            .onNodeWithTag(HomeTestTags.DAILY_MEAL_PLAN_BLOCK)
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        val density = composeRule.activity.resources.displayMetrics.density
        val targetBounds = tourTargetBounds

        assertNotNull(targetBounds)
        assertEquals(dailyCardBounds.left.value * density, targetBounds!!.left, 1f)
        assertEquals(dailyCardBounds.top.value * density, targetBounds.top, 1f)
        assertEquals(dailyCardBounds.right.value * density, targetBounds.right, 1f)
        assertEquals(dailyCardBounds.bottom.value * density, targetBounds.bottom, 1f)
    }
}
