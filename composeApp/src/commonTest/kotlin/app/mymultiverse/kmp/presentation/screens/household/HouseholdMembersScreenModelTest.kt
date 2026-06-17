package app.mymultiverse.kmp.presentation.screens.household

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
class HouseholdMembersScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSpaceCollaborationRepository
    private lateinit var model: HouseholdMembersScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSpaceCollaborationRepository()
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitAddPerson_withUnknownEmail_sendsInviteAndTracksOutbound() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("invite@example.com")
        model.onRoleChange(SpaceMemberRole.Viewer)

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertEquals(HouseholdMembersSuccess.InviteSent, model.uiState.value.successMessageKey)
        assertEquals(1, model.uiState.value.outboundInvites.size)
        assertEquals("invite@example.com", model.uiState.value.outboundInvites.single().email)
    }

    @Test
    fun submitAddPerson_withKnownEmail_addsMember() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("member@example.com")

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertEquals(HouseholdMembersSuccess.MemberAdded, model.uiState.value.successMessageKey)
        assertFalse(model.uiState.value.showAddPersonDialog)
    }

    @Test
    fun nonOwner_cannotManageMembers() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "editor-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.canManageMembers)
    }

    @Test
    fun household_withOwnerOnly_isNotReadyUntilSecondMemberJoins() = runTest(testDispatcher) {
        model.bindHousehold("household-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
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
