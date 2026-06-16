package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.sharing.HOUSEHOLD_RECOMMENDED_MIN_MEMBERS
import app.mymultiverse.kmp.domain.sharing.householdMemberCount
import app.mymultiverse.kmp.domain.sharing.isHouseholdReadyForCollaboration
import app.mymultiverse.kmp.presentation.di.FakeSpaceCollaborationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionSpaceMembersScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSpaceCollaborationRepository
    private lateinit var model: NutritionSpaceMembersScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSpaceCollaborationRepository()
        model = NutritionSpaceMembersScreenModel(
            collaborationRepository = repository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitAddPerson_withUnknownEmail_sendsInvite() = runTest(testDispatcher) {
        model.bindSpace("space-1", ownerId = "owner", ownerDisplayName = "Owner")
        model.openAddPersonDialog()
        model.onEmailChange("invite@example.com")
        model.onRoleChange(SpaceMemberRole.Viewer)

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertEquals(SpaceMembersSuccess.InviteSent, model.uiState.value.successMessageKey)
    }

    @Test
    fun submitAddPerson_withKnownEmail_addsMember() = runTest(testDispatcher) {
        model.bindSpace("space-1", ownerId = "owner", ownerDisplayName = "Owner")
        model.openAddPersonDialog()
        model.onEmailChange("member@example.com")

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertEquals(SpaceMembersSuccess.MemberAdded, model.uiState.value.successMessageKey)
        assertFalse(model.uiState.value.showAddPersonDialog)
    }

    @Test
    fun household_withOwnerOnly_isNotReadyUntilSecondMemberJoins() = runTest(testDispatcher) {
        model.bindSpace("household-1", ownerId = "owner", ownerDisplayName = "Owner")
        advanceUntilIdle()

        assertEquals(1, householdMemberCount(model.uiState.value.members))
        assertFalse(isHouseholdReadyForCollaboration(model.uiState.value.members))

        model.openAddPersonDialog()
        model.onEmailChange("partner@example.com")
        model.submitAddPerson("household-1")
        advanceUntilIdle()

        assertEquals(HOUSEHOLD_RECOMMENDED_MIN_MEMBERS, householdMemberCount(model.uiState.value.members))
        assertTrue(isHouseholdReadyForCollaboration(model.uiState.value.members))
    }
}
