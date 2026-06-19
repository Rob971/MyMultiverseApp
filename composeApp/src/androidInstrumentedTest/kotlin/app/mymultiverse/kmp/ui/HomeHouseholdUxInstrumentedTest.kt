package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.presentation.components.HouseholdNameChipTestTags
import app.mymultiverse.kmp.presentation.screens.home.HomeOnboardingContent
import app.mymultiverse.kmp.presentation.screens.home.HomeOnboardingUiState
import app.mymultiverse.kmp.presentation.screens.home.HomeTestTags
import app.mymultiverse.kmp.presentation.screens.home.HomeWelcomeContent
import app.mymultiverse.kmp.presentation.screens.home.HouseholdNameAvailability
import app.mymultiverse.kmp.presentation.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeHouseholdUxInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onboarding_withoutPendingInvites_showsWaitForInviteSection() {
        composeRule.setContent {
            AppTheme {
                HomeOnboardingContent(
                    onboardingUiState = HomeOnboardingUiState(),
                    pendingInvites = emptyList(),
                    sessionEmail = "tester@example.com",
                    onNameChange = {},
                    onCreate = {},
                    onAcceptInvite = {},
                    onDeclineInvite = {},
                    onRefreshInvites = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_WAIT_FOR_INVITE).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_REFRESH_INVITES).assertIsDisplayed()
    }

    @Test
    fun onboarding_createButtonDisabledUntilNameAvailable() {
        composeRule.setContent {
            AppTheme {
                HomeOnboardingContent(
                    onboardingUiState = HomeOnboardingUiState(
                        householdNameInput = "My Household",
                        nameAvailability = HouseholdNameAvailability.Checking,
                    ),
                    pendingInvites = emptyList(),
                    sessionEmail = "tester@example.com",
                    onNameChange = {},
                    onCreate = {},
                    onAcceptInvite = {},
                    onDeclineInvite = {},
                    onRefreshInvites = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_CREATE_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun onboarding_createButtonEnabledWhenNameAvailable() {
        var createClicked = false

        composeRule.setContent {
            AppTheme {
                HomeOnboardingContent(
                    onboardingUiState = HomeOnboardingUiState(
                        householdNameInput = "Unique Name",
                        nameAvailability = HouseholdNameAvailability.Available,
                    ),
                    pendingInvites = emptyList(),
                    sessionEmail = "tester@example.com",
                    onNameChange = {},
                    onCreate = { createClicked = true },
                    onAcceptInvite = {},
                    onDeclineInvite = {},
                    onRefreshInvites = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_CREATE_BUTTON).performClick()
        assertTrue(createClicked)
    }

    @Test
    fun onboarding_typeName_updatesField() {
        composeRule.setContent {
            AppTheme {
                HomeOnboardingContent(
                    onboardingUiState = HomeOnboardingUiState(),
                    pendingInvites = emptyList(),
                    sessionEmail = "tester@example.com",
                    onNameChange = {},
                    onCreate = {},
                    onAcceptInvite = {},
                    onDeclineInvite = {},
                    onRefreshInvites = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_CREATE_NAME_FIELD)
            .performTextInput("Family Hub")
        composeRule.onNodeWithText("Family Hub").assertIsDisplayed()
    }

    @Test
    fun welcome_owner_seesRenameChipAndEditAction() {
        var renameOpened = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = "Roberto",
                        householdName = "Our Household",
                        canRenameHousehold = true,
                        onRenameHousehold = { renameOpened = true },
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                        onExportPersonalData = {},
                        onDeleteAccount = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HouseholdNameChipTestTags.CHIP).assertIsDisplayed()
        composeRule.onNodeWithTag(HouseholdNameChipTestTags.EDIT).performClick()
        assertTrue(renameOpened)
    }

    @Test
    fun welcome_viewer_hidesRenameEditAction() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeWelcomeContent(
                        greeting = Greeting("ready"),
                        userDisplayName = "Guest",
                        householdName = "Shared Home",
                        canRenameHousehold = false,
                        onRenameHousehold = {},
                        isRefreshing = false,
                        pendingInvites = emptyList(),
                        onRefreshClick = {},
                        onOpenNutrition = {},
                        onOpenHouseholdMembers = {},
                        onAcceptInvite = {},
                        onDeclineInvite = {},
                        onExportPersonalData = {},
                        onDeleteAccount = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HouseholdNameChipTestTags.CHIP).assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithTag(HouseholdNameChipTestTags.EDIT)
                .fetchSemanticsNodes().isEmpty(),
        )
    }
}
