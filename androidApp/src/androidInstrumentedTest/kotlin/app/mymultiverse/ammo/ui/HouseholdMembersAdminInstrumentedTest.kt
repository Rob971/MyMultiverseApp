package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.ammo.domain.platform.PersonalDataExporter
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersScreen
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersScreenModel
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersTestTags
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.ui.InstrumentedComposeTest.waitFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HouseholdMembersAdminInstrumentedTest {

    private val noopPersonalDataExporter = object : PersonalDataExporter {
        override fun shareJson(filename: String, content: String): Boolean = true

        override fun shareText(chooserTitle: String, message: String): Boolean = true
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val householdId = "household-1"
    private val ownerId = "owner-user"
    private val editorMemberId = "member-editor-1"

    @Test
    fun owner_canPromoteEditorToAdminThroughRoleDialog() {
        val collaborationRepository = InstrumentedHouseholdCollaborationRepository()
        collaborationRepository.seedMembers(
            householdId = householdId,
            ownerId = ownerId,
            ownerDisplayName = "Owner",
            members = listOf(
                HouseholdMember(
                    id = editorMemberId,
                    householdId = householdId,
                    kind = HouseholdMemberKind.Person,
                    displayName = "Editor User",
                    role = HouseholdMemberRole.Editor,
                    referenceId = "editor-user",
                ),
            ),
        )
        val screenModel = HouseholdMembersScreenModel(
            collaborationRepository = collaborationRepository,
            householdRepository = InstrumentedHouseholdRepository(role = HouseholdMemberRole.Owner),
            sessionCoordinator = InstrumentedNutritionSessionCoordinator(
                repository = InstrumentedNutritionRepository(weekKey = "2026-06-16"),
            ),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )
        val authRepository = InstrumentedFakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = ownerId, email = "owner@example.com", displayName = "Owner"),
            ),
        )

        composeRule.setContent {
            AppTheme {
                HouseholdMembersScreen(
                    household = HouseholdContext(
                        id = householdId,
                        name = "Our Household",
                        ownerId = ownerId,
                        ownerDisplayName = "Owner",
                        nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
                    ),
                    onBack = {},
                    screenModel = screenModel,
                    authRepository = authRepository,
                    personalDataExporter = noopPersonalDataExporter,
                )
            }
        }

        composeRule.waitFor(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("${HouseholdMembersTestTags.MEMBER_ROW}_$editorMemberId")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("${HouseholdMembersTestTags.MEMBER_ROW}_$editorMemberId")
            .performScrollTo()
        composeRule.onNodeWithTag("${HouseholdMembersTestTags.MEMBER_ROW_OVERFLOW}_$editorMemberId")
            .performClick()
        composeRule.onNodeWithTag(HouseholdMembersTestTags.MEMBER_CHANGE_ROLE_MENU).performClick()
        composeRule.onNodeWithText("Family admin").performClick()
        composeRule.onNodeWithTag(HouseholdMembersTestTags.ROLE_CHANGE_CONFIRM_BUTTON).performClick()
        composeRule.onNodeWithText("Make family admin?").assertIsDisplayed()
        composeRule.onNodeWithTag(HouseholdMembersTestTags.PROMOTE_ADMIN_CONFIRM_BUTTON).performClick()

        composeRule.waitFor(timeoutMillis = 5_000) {
            collaborationRepository.lastRoleUpdate?.second == HouseholdMemberRole.Admin
        }

        assertEquals(
            editorMemberId to HouseholdMemberRole.Admin,
            collaborationRepository.lastRoleUpdate,
        )
    }

    @Test
    fun viewer_seesReadOnlyMembersListWithoutAddPerson() {
        val collaborationRepository = InstrumentedHouseholdCollaborationRepository()
        collaborationRepository.seedMembers(
            householdId = householdId,
            ownerId = ownerId,
            ownerDisplayName = "Owner",
            members = emptyList(),
        )
        val screenModel = HouseholdMembersScreenModel(
            collaborationRepository = collaborationRepository,
            householdRepository = InstrumentedHouseholdRepository(role = HouseholdMemberRole.Viewer),
            sessionCoordinator = InstrumentedNutritionSessionCoordinator(
                repository = InstrumentedNutritionRepository(weekKey = "2026-06-16"),
            ),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )
        val authRepository = InstrumentedFakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "viewer-user", email = "viewer@example.com", displayName = "Viewer"),
            ),
        )

        composeRule.setContent {
            AppTheme {
                HouseholdMembersScreen(
                    household = HouseholdContext(
                        id = householdId,
                        name = "Our Household",
                        ownerId = ownerId,
                        ownerDisplayName = "Owner",
                        nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
                    ),
                    onBack = {},
                    screenModel = screenModel,
                    authRepository = authRepository,
                    personalDataExporter = noopPersonalDataExporter,
                )
            }
        }

        composeRule.waitFor(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Only the family owner and admins can invite or remove members.")
                .fetchSemanticsNodes().isNotEmpty()
        }

        assertTrue(
            composeRule.onAllNodesWithTag(HouseholdMembersTestTags.INVITE_BUTTON)
                .fetchSemanticsNodes().isEmpty(),
        )
    }
}
