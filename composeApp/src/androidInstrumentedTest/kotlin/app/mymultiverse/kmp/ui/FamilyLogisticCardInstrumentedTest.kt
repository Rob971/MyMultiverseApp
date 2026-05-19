package app.mymultiverse.kmp.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.appcompat.R
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.AppTheme
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FamilyLogisticCardInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComposeTestActivity>()

    @Test
    fun enabledCard_tapInvokesClickHandler_underAppCompatTheme() {
        composeRule.activity.setTheme(R.style.Theme_AppCompat_Light_NoActionBar)
        var clicked = false

        composeRule.setContent {
            AppTheme {
                FamilyLogisticCard(
                    title = "Nutrition",
                    description = "Meals and groceries",
                    accentColor = SharedJourneyColors.SageSoft,
                    icon = AppIcons.Restaurant,
                    onClick = { clicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Nutrition").assertIsDisplayed().performClick()

        assertTrue(clicked)
    }

    @Test
    fun disabledCard_tapDoesNotInvokeClickHandler() {
        composeRule.activity.setTheme(R.style.Theme_AppCompat_Light_NoActionBar)
        var clicked = false

        composeRule.setContent {
            AppTheme {
                FamilyLogisticCard(
                    title = "Adventures",
                    description = "Coming later",
                    accentColor = SharedJourneyColors.TerracottaOrange,
                    icon = AppIcons.Explore,
                    enabled = false,
                    badge = "Coming soon",
                    onClick = { clicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Adventures").performClick()

        assertFalse(clicked)
    }
}
