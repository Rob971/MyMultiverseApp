package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.presentation.components.HomeHouseholdButtonTestTags
import app.mymultiverse.ammo.presentation.screens.home.HomeAccountSheet
import app.mymultiverse.ammo.presentation.screens.home.HomeAccountSheetTestTags
import app.mymultiverse.ammo.presentation.screens.home.HomeOnboardingContent
import app.mymultiverse.ammo.presentation.screens.home.HomeOnboardingUiState
import app.mymultiverse.ammo.presentation.screens.home.HomeTestTags
import app.mymultiverse.ammo.presentation.screens.home.HouseholdNameAvailability
import app.mymultiverse.ammo.presentation.theme.AppTheme
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
                    suggestedQuickCreateName = null,
                    onNameChange = {},
                    onQuickCreate = {},
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
                    suggestedQuickCreateName = null,
                    onNameChange = {},
                    onQuickCreate = {},
                    onCreate = {},
                    onAcceptInvite = {},
                    onDeclineInvite = {},
                    onRefreshInvites = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_CREATE_BUTTON)
            .performScrollTo()
            .assertIsNotEnabled()
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
                    suggestedQuickCreateName = null,
                    onNameChange = {},
                    onQuickCreate = {},
                    onCreate = { createClicked = true },
                    onAcceptInvite = {},
                    onDeclineInvite = {},
                    onRefreshInvites = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_CREATE_BUTTON)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertTrue(createClicked)
    }

    @Test
    fun onboarding_createButtonDisabledWhenNameBlank() {
        composeRule.setContent {
            AppTheme {
                HomeOnboardingContent(
                    onboardingUiState = HomeOnboardingUiState(
                        householdNameInput = "",
                        nameAvailability = HouseholdNameAvailability.Unknown,
                    ),
                    pendingInvites = emptyList(),
                    sessionEmail = "tester@example.com",
                    suggestedQuickCreateName = null,
                    onNameChange = {},
                    onQuickCreate = {},
                    onCreate = {},
                    onAcceptInvite = {},
                    onDeclineInvite = {},
                    onRefreshInvites = {},
                )
            }
        }

        composeRule.onNodeWithTag(HomeTestTags.ONBOARDING_CREATE_BUTTON)
            .performScrollTo()
            .assertIsNotEnabled()
    }

    @Test
    fun accountSheet_familyHub_opensMembers() {
        var membersOpened = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeAccountSheet(
                        visible = true,
                        householdName = "Our Household",
                        canRenameHousehold = true,
                        onDismiss = {},
                        onOpenHouseholdMembers = { membersOpened = true },
                        onRenameHousehold = {},
                        onSignOut = {},
                        onExportPersonalData = {},
                        onDeleteAccount = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeAccountSheetTestTags.FAMILY_HUB).performClick()
        assertTrue(membersOpened)
    }

    @Test
    fun accountSheet_owner_editRenamesHousehold() {
        var renameOpened = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeAccountSheet(
                        visible = true,
                        householdName = "Our Household",
                        canRenameHousehold = true,
                        onDismiss = {},
                        onOpenHouseholdMembers = {},
                        onRenameHousehold = { renameOpened = true },
                        onSignOut = {},
                        onExportPersonalData = {},
                        onDeleteAccount = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeHouseholdButtonTestTags.EDIT).performClick()
        assertTrue(renameOpened)
    }

    @Test
    fun accountSheet_viewer_hidesRenameEditAction() {
        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    HomeAccountSheet(
                        visible = true,
                        householdName = "Shared Home",
                        canRenameHousehold = false,
                        onDismiss = {},
                        onOpenHouseholdMembers = {},
                        onRenameHousehold = {},
                        onSignOut = {},
                        onExportPersonalData = {},
                        onDeleteAccount = {},
                    )
                }
            }
        }

        composeRule.onNodeWithTag(HomeAccountSheetTestTags.FAMILY_HUB).assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithTag(HomeHouseholdButtonTestTags.EDIT)
                .fetchSemanticsNodes().isEmpty(),
        )
    }

}
