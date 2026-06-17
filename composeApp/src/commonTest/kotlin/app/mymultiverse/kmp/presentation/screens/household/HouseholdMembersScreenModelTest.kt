package app.mymultiverse.kmp.presentation.screens.household

import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.sharing.HOUSEHOLD_RECOMMENDED_MIN_MEMBERS
import app.mymultiverse.kmp.domain.sharing.householdMemberCount
import app.mymultiverse.kmp.domain.sharing.isHouseholdReadyForCollaboration
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.kmp.presentation.di.FakeSpaceCollaborationRepository
import com.russhwolf.settings.MapSettings
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
    private lateinit var householdRepository: FakeHouseholdRepository
    private lateinit var sessionCoordinator: FakeNutritionSessionCoordinator
    private lateinit var model: HouseholdMembersScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSpaceCollaborationRepository()
        householdRepository = FakeHouseholdRepository()
        sessionCoordinator = FakeNutritionSessionCoordinator(
            initialRepository = app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl(MapSettings()),
        )
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitAddPerson_withUnknownEmail_closesDialogAndTracksOutbound() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("invite@example.com")
        model.onRoleChange(SpaceMemberRole.Viewer)

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.showAddPersonDialog)
        assertEquals(HouseholdMembersSuccess.InviteSent, model.uiState.value.successMessageKey)
        assertEquals(1, model.uiState.value.outboundInvites.size)
        assertEquals("invite@example.com", model.uiState.value.outboundInvites.single().email)
    }

    @Test
    fun submitAddPerson_withBlankEmail_keepsDialogOpenWithInlineError() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddPersonDialog)
        assertEquals(HouseholdMembersError.EmailRequired, model.uiState.value.dialogError)
    }

    @Test
    fun submitAddPerson_whenRepositoryFails_keepsDialogOpenWithError() = runTest(testDispatcher) {
        repository.addMemberFailure = IllegalStateException("insufficient_role")
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("partner@example.com")

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddPersonDialog)
        assertEquals(HouseholdMembersError.InsufficientRole, model.uiState.value.dialogError)
    }

    @Test
    fun submitAddPerson_withKnownEmail_sendsInvite() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("member@example.com")

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertEquals(HouseholdMembersSuccess.InviteSent, model.uiState.value.successMessageKey)
        assertFalse(model.uiState.value.showAddPersonDialog)
        assertEquals(1, model.uiState.value.outboundInvites.size)
    }

    @Test
    fun submitAddPerson_whenInviteeAlreadyInHousehold_showsInlineError() = runTest(testDispatcher) {
        repository.emailsAlreadyInAnotherHousehold = setOf("taken@example.com")
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("taken@example.com")

        model.submitAddPerson("space-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddPersonDialog)
        assertEquals(
            HouseholdMembersError.InviteeHouseholdAlreadyActive,
            model.uiState.value.dialogError,
        )
    }

    @Test
    fun nonOwner_cannotManageMembers() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "editor-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.canManageMembers)
    }

    @Test
    fun confirmLeave_callsHouseholdRepositoryAndDeactivatesSession() = runTest(testDispatcher) {
        model.bindHousehold("space-1", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "editor-1")
        advanceUntilIdle()

        model.requestLeave()
        model.confirmLeaveOrDissolve()
        advanceUntilIdle()

        assertEquals(1, householdRepository.leaveCalls)
        assertEquals(1, sessionCoordinator.deactivateCount)
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

        val inviteId = model.uiState.value.outboundInvites.single().id
        repository.acceptInvite(inviteId)
        repository.refreshMembers("household-1", ownerId = "owner", ownerDisplayName = "Owner")
        advanceUntilIdle()

        assertEquals(HOUSEHOLD_RECOMMENDED_MIN_MEMBERS, householdMemberCount(model.uiState.value.members))
        assertTrue(isHouseholdReadyForCollaboration(model.uiState.value.members))
    }
}
