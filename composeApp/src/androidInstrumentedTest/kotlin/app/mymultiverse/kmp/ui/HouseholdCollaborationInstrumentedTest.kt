package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.kmp.presentation.components.HouseholdViewerReadOnlyTestTags
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
import app.mymultiverse.kmp.presentation.screens.household.HouseholdMembersScreen
import app.mymultiverse.kmp.presentation.screens.household.HouseholdMembersScreenModel
import app.mymultiverse.kmp.presentation.screens.household.HouseholdMembersTestTags
import app.mymultiverse.kmp.presentation.screens.nutrition.GroceryShoppingScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.kmp.presentation.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HouseholdCollaborationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun viewerRole_hidesGroceryInputAndShowsReadOnlyBanner() {
        val weekKey = WeekCalendar.currentWeekKey()
        val repository = InstrumentedNutritionRepository(weekKey)
        val screenModel = NutritionScreenModel(
            session = InstrumentedNutritionSessionCoordinator(repository),
            householdRepository = InstrumentedHouseholdRepository(
                role = HouseholdMemberRole.Viewer,
            ),
            aiAssistant = InstrumentedNutritionAdviceService(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )

        composeRule.setContent {
            AppTheme {
                GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
            }
        }

        composeRule.onNodeWithTag(HouseholdViewerReadOnlyTestTags.BANNER).assertIsDisplayed()
        assertTrue(
            composeRule.onAllNodesWithTag(GroceryInputBarTestTags.INPUT_FIELD)
                .fetchSemanticsNodes().isEmpty(),
        )
        assertTrue(
            composeRule.onAllNodesWithTag(GroceryInputBarTestTags.ADD_BUTTON)
                .fetchSemanticsNodes().isEmpty(),
        )
    }

    @Test
    fun ownerWithEditorMember_opensTransferOwnershipDialog() {
        val collaboration = InstrumentedHouseholdCollaborationRepository()
        val ownerId = "owner-user"
        val editor = HouseholdMember(
            id = "editor-1",
            householdId = "household-1",
            kind = HouseholdMemberKind.Person,
            displayName = "Editor User",
            role = HouseholdMemberRole.Editor,
            referenceId = "editor-user",
        )
        collaboration.seedMembers(
            householdId = "household-1",
            ownerId = ownerId,
            ownerDisplayName = "Owner User",
            members = listOf(editor),
        )

        val screenModel = HouseholdMembersScreenModel(
            collaborationRepository = collaboration,
            householdRepository = InstrumentedHouseholdRepository(
                household = app.mymultiverse.kmp.domain.model.sharing.Household(
                    id = "household-1",
                    name = "Our household",
                    ownerId = ownerId,
                    ownerDisplayName = "Owner User",
                    nutritionFeatures = emptySet(),
                ),
            ),
            sessionCoordinator = InstrumentedNutritionSessionCoordinator(
                InstrumentedNutritionRepository(WeekCalendar.currentWeekKey()),
            ),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )

        val household = HouseholdContext(
            id = "household-1",
            name = "Our household",
            ownerId = ownerId,
            ownerDisplayName = "Owner User",
        )
        val authRepository = InstrumentedFakeAuthRepository(
            AuthState.Authenticated(
                AuthUser(
                    id = ownerId,
                    email = "owner@test.com",
                    displayName = "Owner User",
                ),
            ),
        )

        composeRule.setContent {
            AppTheme {
                HouseholdMembersScreen(
                    household = household,
                    onBack = {},
                    screenModel = screenModel,
                    authRepository = authRepository,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            screenModel.uiState.value.canTransferOwnership &&
                composeRule.onAllNodesWithTag(HouseholdMembersTestTags.TRANSFER_OWNERSHIP_BUTTON)
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(HouseholdMembersTestTags.TRANSFER_OWNERSHIP_BUTTON).performClick()
        composeRule.onNodeWithTag(HouseholdMembersTestTags.TRANSFER_DIALOG).assertIsDisplayed()
    }
}
