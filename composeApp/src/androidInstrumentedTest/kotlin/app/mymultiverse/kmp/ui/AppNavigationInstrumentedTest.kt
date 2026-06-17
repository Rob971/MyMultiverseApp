package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.presentation.navigation.NavigationTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionHubScreen
import app.mymultiverse.kmp.presentation.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nutritionHub_backButton_invokesOnBack() {
        var backPressed = false

        composeRule.setContent {
            AppTheme {
                NutritionHubScreen(
                    spaceName = "Our household",
                    enabledFeatures = setOf(
                        NutritionSharingFeature.Grocery,
                        NutritionSharingFeature.MealPlan,
                        NutritionSharingFeature.AiAdvice,
                    ),
                    onBack = { backPressed = true },
                    onOpenSection = {},
                )
            }
        }

        composeRule.onNodeWithTag(NavigationTestTags.BACK_BUTTON).performClick()

        assertTrue(backPressed)
    }
}
