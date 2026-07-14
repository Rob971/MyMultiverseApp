package app.mymultiverse.ammo.presentation.screens.household

import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.sharing.HOUSEHOLD_RECOMMENDED_MIN_MEMBERS
import app.mymultiverse.ammo.domain.sharing.householdMemberCount
import app.mymultiverse.ammo.domain.sharing.isHouseholdReadyForCollaboration
import app.mymultiverse.ammo.presentation.di.FakeHouseholdRepository
import app.mymultiverse.ammo.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.ammo.presentation.di.FakeHouseholdCollaborationRepository
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
    private lateinit var repository: FakeHouseholdCollaborationRepository
    private lateinit var householdRepository: FakeHouseholdRepository
    private lateinit var sessionCoordinator: FakeNutritionSessionCoordinator
    private lateinit var model: HouseholdMembersScreenModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeHouseholdCollaborationRepository()
        householdRepository = FakeHouseholdRepository()
        sessionCoordinator = FakeNutritionSessionCoordinator(
            initialRepository = app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl(MapSettings()),
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
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("invite@example.com")
        model.onRoleChange(HouseholdMemberRole.Viewer)

        model.submitAddPerson("household-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.showAddPersonDialog)
        assertEquals(HouseholdMembersSuccess.InviteSent, model.uiState.value.successMessageKey)
        assertEquals(1, model.uiState.value.outboundInvites.size)
        assertEquals("invite@example.com", model.uiState.value.outboundInvites.single().email)
        assertEquals(
            HouseholdInviteSharePayload(
                householdName = "Test Household",
                inviteToken = model.uiState.value.outboundInvites.single().inviteToken.orEmpty(),
            ),
            model.uiState.value.pendingInviteShare,
        )
    }

    @Test
    fun submitAddPerson_withBlankEmail_keepsDialogOpenWithInlineError() = runTest(testDispatcher) {
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()

        model.submitAddPerson("household-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddPersonDialog)
        assertEquals(HouseholdMembersError.EmailRequired, model.uiState.value.dialogError)
    }

    @Test
    fun submitAddPerson_whenRepositoryFails_keepsDialogOpenWithError() = runTest(testDispatcher) {
        repository.addMemberFailure = IllegalStateException("insufficient_role")
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("partner@example.com")

        model.submitAddPerson("household-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddPersonDialog)
        assertEquals(HouseholdMembersError.InsufficientRole, model.uiState.value.dialogError)
    }

    @Test
    fun submitAddPerson_withKnownEmail_sendsInvite() = runTest(testDispatcher) {
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("member@example.com")

        model.submitAddPerson("household-1")
        advanceUntilIdle()

        assertEquals(HouseholdMembersSuccess.InviteSent, model.uiState.value.successMessageKey)
        assertFalse(model.uiState.value.showAddPersonDialog)
        assertEquals(1, model.uiState.value.outboundInvites.size)
    }

    @Test
    fun submitAddPerson_whenInviteeAlreadyInHousehold_showsInlineError() = runTest(testDispatcher) {
        repository.emailsAlreadyInAnotherHousehold = setOf("taken@example.com")
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddPersonDialog()
        model.onEmailChange("taken@example.com")

        model.submitAddPerson("household-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddPersonDialog)
        assertEquals(
            HouseholdMembersError.InviteeHouseholdAlreadyActive,
            model.uiState.value.dialogError,
        )
    }

    @Test
    fun nonOwner_cannotManageMembers() = runTest(testDispatcher) {
        householdRepository = FakeHouseholdRepository(role = HouseholdMemberRole.Editor)
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "editor-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.canManageMembers)
        assertEquals(HouseholdMemberRole.Editor, model.uiState.value.currentUserRole)
    }

    @Test
    fun owner_canPromoteMemberToAdmin() = runTest(testDispatcher) {
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "member-1",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Partner",
                role = HouseholdMemberRole.Editor,
                referenceId = "partner-id",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        advanceUntilIdle()

        val refreshCallsAfterBind = repository.refreshMembersCalls
        val member = model.uiState.value.members.single { it.role == HouseholdMemberRole.Editor }
        model.openRoleChangeDialog(member)
        model.onMemberRoleChange(HouseholdMemberRole.Admin)
        model.confirmRoleChange("household-1")
        advanceUntilIdle()

        assertEquals(HouseholdMembersSuccess.RoleUpdated, model.uiState.value.successMessageKey)
        assertEquals(
            HouseholdMemberRole.Admin,
            model.uiState.value.members.single { it.id == "member-1" }.role,
        )
        assertEquals(refreshCallsAfterBind, repository.refreshMembersCalls)
        assertFalse(model.uiState.value.showRoleChangeDialog)
    }

    @Test
    fun confirmRoleChange_updatesMemberRoleWithoutRefreshingMembers() = runTest(testDispatcher) {
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "member-1",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Partner",
                role = HouseholdMemberRole.Editor,
                referenceId = "partner-id",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold(
            householdId = "household-1",
            householdName = "Test Household",
            ownerId = "owner",
            ownerDisplayName = "Owner",
            currentUserId = "owner",
        )
        advanceUntilIdle()

        val refreshCallsAfterBind = repository.refreshMembersCalls
        val member = model.uiState.value.members.single { it.role == HouseholdMemberRole.Editor }
        model.openRoleChangeDialog(member)
        model.onMemberRoleChange(HouseholdMemberRole.Viewer)
        model.confirmRoleChange("household-1")
        advanceUntilIdle()

        assertEquals(HouseholdMemberRole.Viewer, model.uiState.value.members.single { it.id == "member-1" }.role)
        assertEquals(refreshCallsAfterBind, repository.refreshMembersCalls)
    }

    @Test
    fun admin_canDemoteAdminToViewer() = runTest(testDispatcher) {
        householdRepository = FakeHouseholdRepository(role = HouseholdMemberRole.Admin)
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "admin-2",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Other Admin",
                role = HouseholdMemberRole.Admin,
                referenceId = "admin-2-ref",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold(
            householdId = "household-1",
            householdName = "Test Household",
            ownerId = "owner",
            ownerDisplayName = "Owner",
            currentUserId = "admin-1",
        )
        advanceUntilIdle()

        val otherAdmin = model.uiState.value.members.single { it.id == "admin-2" }
        model.openRoleChangeDialog(otherAdmin)
        model.onMemberRoleChange(HouseholdMemberRole.Viewer)
        model.confirmRoleChange("household-1")
        advanceUntilIdle()

        assertEquals(HouseholdMembersSuccess.RoleUpdated, model.uiState.value.successMessageKey)
        assertEquals(
            HouseholdMemberRole.Viewer,
            model.uiState.value.members.single { it.id == "admin-2" }.role,
        )
    }

    @Test
    fun admin_cannotInviteAsAdmin() = runTest(testDispatcher) {
        householdRepository = FakeHouseholdRepository(role = HouseholdMemberRole.Admin)
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "admin-1")
        advanceUntilIdle()

        model.openAddPersonDialog()
        model.onRoleChange(HouseholdMemberRole.Admin)
        assertEquals(HouseholdMemberRole.Editor, model.uiState.value.selectedRole)
    }

    @Test
    fun confirmLeave_callsHouseholdRepositoryAndDeactivatesSession() = runTest(testDispatcher) {
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "editor-1")
        advanceUntilIdle()

        model.requestLeave()
        model.confirmLeaveOrDissolve()
        advanceUntilIdle()

        assertEquals(1, householdRepository.leaveCalls)
        assertEquals(1, sessionCoordinator.deactivateCount)
    }

    @Test
    fun confirmTransferOwnership_updatesHouseholdAndRefreshesMembers() = runTest(testDispatcher) {
        repository.seedMember(
            householdId = "household-1",
            member = app.mymultiverse.ammo.domain.model.sharing.HouseholdMember(
                id = "member-1",
                householdId = "household-1",
                kind = app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind.Person,
                displayName = "Partner",
                role = HouseholdMemberRole.Editor,
                referenceId = "partner-id",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        advanceUntilIdle()

        model.openTransferDialog()
        model.selectTransferMember("partner-id")
        model.confirmTransferOwnership("household-1")
        advanceUntilIdle()

        assertEquals(1, householdRepository.transferCalls)
        assertEquals("partner-id", householdRepository.lastTransferTargetId)
        assertEquals(HouseholdMembersSuccess.OwnershipTransferred, model.uiState.value.successMessageKey)
        assertFalse(model.uiState.value.canManageMembers)
    }

    @Test
    fun household_withOwnerOnly_isNotReadyUntilSecondMemberJoins() = runTest(testDispatcher) {
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
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

    @Test
    fun submitAddDependant_withValidName_closesDialogAndShowsSuccess() = runTest(testDispatcher) {
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddDependantDialog()
        model.onDependantNameChange("Mia")

        model.submitAddDependant("household-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.showAddDependantDialog)
        assertEquals(HouseholdMembersSuccess.DependantAdded, model.uiState.value.successMessageKey)
        assertEquals(2, model.uiState.value.members.size)
        assertTrue(
            model.uiState.value.members.any {
                it.kind == HouseholdMemberKind.Dependant && it.displayName == "Mia"
            },
        )
    }

    @Test
    fun submitAddDependant_withBlankName_keepsDialogOpenWithInlineError() = runTest(testDispatcher) {
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddDependantDialog()

        model.submitAddDependant("household-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddDependantDialog)
        assertEquals(HouseholdMembersError.Generic, model.uiState.value.dialogError)
    }

    @Test
    fun submitAddDependant_whenRepositoryFails_keepsDialogOpenWithError() = runTest(testDispatcher) {
        repository.addDependantFailure = IllegalStateException(CollaborationErrorCodes.INSUFFICIENT_ROLE)
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddDependantDialog()
        model.onDependantNameChange("Mia")

        model.submitAddDependant("household-1")
        advanceUntilIdle()

        assertTrue(model.uiState.value.showAddDependantDialog)
        assertEquals(HouseholdMembersError.InsufficientRole, model.uiState.value.dialogError)
    }

    @Test
    fun removeDependant_removesMemberFromList() = runTest(testDispatcher) {
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "owner")
        model.openAddDependantDialog()
        model.onDependantNameChange("Mia")
        model.submitAddDependant("household-1")
        advanceUntilIdle()

        val dependant = model.uiState.value.members.single { it.kind == HouseholdMemberKind.Dependant }
        model.removeMember(dependant, "household-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.members.any { it.kind == HouseholdMemberKind.Dependant })
        assertEquals(1, model.uiState.value.members.size)
        assertEquals(HouseholdMemberRole.Owner, model.uiState.value.members.single().role)
    }

    @Test
    fun uploadHouseholdAvatar_setsLoadingThenClearsOnSuccess() = runTest(testDispatcher) {
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(0), "image/jpeg")
        advanceUntilIdle()

        assertFalse(model.uiState.value.isUploadingHouseholdAvatar)
        assertEquals(1, householdRepository.updateHouseholdAvatarCalls)
    }

    @Test
    fun uploadHouseholdAvatar_onFailure_clearsLoadingAndSetsError() = runTest(testDispatcher) {
        householdRepository.updateHouseholdAvatarResult = Result.failure(IllegalStateException("upload_failed"))
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(0), "image/jpeg")
        advanceUntilIdle()

        assertFalse(model.uiState.value.isUploadingHouseholdAvatar)
        assertEquals(HouseholdMembersError.Generic, model.uiState.value.error)
    }
}
