package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.presentation.components.FamilyLogisticCard
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AppCompat + Material3 tap regression for [FamilyLogisticCard].
 * Source contract is guarded in [app.mymultiverse.ammo.presentation.components.FamilyLogisticCardContractTest].
 */
@RunWith(AndroidJUnit4::class)
class FamilyLogisticCardInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun familyLogisticCard_respectsEnabledState_underAppCompatTheme() {
        var enabledClicked = false
        var disabledClicked = false

        composeRule.setContent {
            AppTheme {
                Column {
                    FamilyLogisticCard(
                        title = "Nutrition",
                        description = "Meals and groceries",
                        accentColor = SharedJourneyColors.SageSoft,
                        icon = AppIcons.Restaurant,
                        onClick = { enabledClicked = true },
                    )
                    FamilyLogisticCard(
                        title = "Adventures",
                        description = "Coming later",
                        accentColor = SharedJourneyColors.TerracottaOrange,
                        icon = AppIcons.Explore,
                        enabled = false,
                        badge = "Coming soon",
                        onClick = { disabledClicked = true },
                    )
                }
            }
        }

        composeRule.onNodeWithText("Nutrition").assertIsDisplayed().performClick()
        assertTrue(enabledClicked)

        composeRule.onNodeWithText("Adventures").performClick()
        assertFalse(disabledClicked)
    }
}
