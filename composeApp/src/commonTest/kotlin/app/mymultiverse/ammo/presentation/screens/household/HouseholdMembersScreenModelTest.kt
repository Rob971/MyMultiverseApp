package app.mymultiverse.ammo.presentation.screens.household

import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.sharing.AvatarImagePrepareException
import app.mymultiverse.ammo.domain.sharing.AvatarPersistException
import app.mymultiverse.ammo.domain.sharing.AvatarUploadTarget
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
import kotlin.test.assertNull
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
            logger = TestObservability.logger,
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
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "editor-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.canManageMembers)
        assertTrue(model.uiState.value.canWriteHouseholdData)
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
            logger = TestObservability.logger,
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
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        model.bindHousehold(householdId = "household-1", householdName = "Test Household", ownerId = "owner", ownerDisplayName = "Owner", currentUserId = "admin-1")
        advanceUntilIdle()

        model.openAddPersonDialog()
        model.onRoleChange(HouseholdMemberRole.Admin)
        assertEquals(HouseholdMemberRole.Editor, model.uiState.value.selectedRole)
    }

    @Test
    fun admin_cannotRemoveSelf() = runTest(testDispatcher) {
        householdRepository = FakeHouseholdRepository(role = HouseholdMemberRole.Admin)
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "member-admin-1",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Current Admin",
                role = HouseholdMemberRole.Admin,
                referenceId = "admin-1",
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

        val currentAdmin = model.uiState.value.members.single { it.referenceId == "admin-1" }
        model.removeMember(currentAdmin, "household-1")
        advanceUntilIdle()

        assertEquals(0, repository.removeMemberCalls)
        assertTrue(model.uiState.value.members.any { it.referenceId == "admin-1" })
    }

    @Test
    fun removeMember_whenRepositoryRejectsRequest_keepsMemberAndShowsError() = runTest(testDispatcher) {
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
        repository.removeMemberFailure = IllegalStateException(CollaborationErrorCodes.INSUFFICIENT_ROLE)

        val member = model.uiState.value.members.single { it.id == "member-1" }
        model.removeMember(member, "household-1")
        advanceUntilIdle()

        assertEquals(1, repository.removeMemberCalls)
        assertTrue(model.uiState.value.members.any { it.id == "member-1" })
        assertEquals(HouseholdMembersError.InsufficientRole, model.uiState.value.error)
        assertFalse(model.uiState.value.isSaving)
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
    fun uploadHouseholdAvatar_setsLoadingThenShowsSuccessSnackbar() = runTest(testDispatcher) {
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertFalse(model.uiState.value.isUploadingHouseholdAvatar)
        assertNull(model.uiState.value.error)
        assertEquals(HouseholdMembersSuccess.AvatarUploaded, model.uiState.value.successMessageKey)
        assertEquals(1, householdRepository.updateHouseholdAvatarCalls)
    }

    @Test
    fun uploadHouseholdAvatar_onFailure_clearsLoadingAndSetsAvatarUploadFailedError() = runTest(testDispatcher) {
        householdRepository.updateHouseholdAvatarResult = Result.failure(IllegalStateException("upload_failed"))
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertFalse(model.uiState.value.isUploadingHouseholdAvatar)
        assertEquals(HouseholdMembersError.AvatarUploadFailed, model.uiState.value.error)
    }

    @Test
    fun uploadMemberAvatar_onSuccess_showsSuccessSnackbar() = runTest(testDispatcher) {
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "member-1",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Alice",
                role = HouseholdMemberRole.Editor,
                referenceId = "user-alice",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        val member = model.uiState.value.members.single { it.id == "member-1" }
        model.uploadMemberAvatar("household-1", member, ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertNull(model.uiState.value.uploadingAvatarMemberId)
        assertNull(model.uiState.value.error)
        assertEquals(HouseholdMembersSuccess.AvatarUploaded, model.uiState.value.successMessageKey)
    }

    @Test
    fun uploadMemberAvatar_onFailure_setsAvatarUploadFailedError() = runTest(testDispatcher) {
        repository.updateMemberAvatarResult = Result.failure(RuntimeException("network_error"))
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "member-1",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Alice",
                role = HouseholdMemberRole.Editor,
                referenceId = "user-alice",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        val member = model.uiState.value.members.single { it.id == "member-1" }
        model.uploadMemberAvatar("household-1", member, ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertNull(model.uiState.value.uploadingAvatarMemberId)
        assertEquals(HouseholdMembersError.AvatarUploadFailed, model.uiState.value.error)
    }

    @Test
    fun editor_hasCanWriteHouseholdDataButNotCanManageMembers() = runTest(testDispatcher) {
        householdRepository = FakeHouseholdRepository(role = HouseholdMemberRole.Editor)
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "editor-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.canManageMembers)
        assertTrue(model.uiState.value.canWriteHouseholdData)
    }

    @Test
    fun viewer_hasBothCanManageMembersAndCanWriteHouseholdDataFalse() = runTest(testDispatcher) {
        householdRepository = FakeHouseholdRepository(role = HouseholdMemberRole.Viewer)
        model = HouseholdMembersScreenModel(
            collaborationRepository = repository,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "viewer-1")
        advanceUntilIdle()

        assertFalse(model.uiState.value.canManageMembers)
        assertFalse(model.uiState.value.canWriteHouseholdData)
    }

    @Test
    fun uploadHouseholdAvatar_updatesHouseholdAvatarUrlInUiState() = runTest(testDispatcher) {
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertEquals(householdRepository.lastHouseholdAvatarUrl, model.uiState.value.householdAvatarUrl)
    }

    @Test
    fun uploadHouseholdAvatar_emptyBytes_setsAvatarUploadFailedWithoutCallingRepository() = runTest(testDispatcher) {
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(0), "image/jpeg")
        advanceUntilIdle()

        assertEquals(0, householdRepository.updateHouseholdAvatarCalls)
        assertEquals(HouseholdMembersError.AvatarUploadFailed, model.uiState.value.error)
    }

    @Test
    fun uploadMemberAvatar_persistFailure_setsMemberAvatarPersistFailed() = runTest(testDispatcher) {
        repository.updateMemberAvatarResult = Result.failure(
            AvatarPersistException(
                target = AvatarUploadTarget.MemberProfile,
                dbTable = "profiles",
                householdId = "household-1",
                storagePath = "profiles/user-carola/avatar.jpg",
                memberId = "member-1",
            ),
        )
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "member-1",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Carola",
                role = HouseholdMemberRole.Editor,
                referenceId = "user-carola",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "user-carola")
        advanceUntilIdle()

        val member = model.uiState.value.members.single { it.displayName == "Carola" }
        model.uploadMemberAvatar("household-1", member, ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertEquals(HouseholdMembersError.MemberAvatarPersistFailed, model.uiState.value.error)
    }

    @Test
    fun uploadHouseholdAvatar_persistFailure_setsHouseholdAvatarPersistFailed() = runTest(testDispatcher) {
        householdRepository.updateHouseholdAvatarResult =
            Result.failure(
                AvatarPersistException(
                    target = AvatarUploadTarget.Household,
                    dbTable = "households",
                    householdId = "household-1",
                    storagePath = "households/household-1/avatar.jpg",
                ),
            )
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertEquals(HouseholdMembersError.HouseholdAvatarPersistFailed, model.uiState.value.error)
    }

    @Test
    fun uploadMemberAvatar_prepareUnsupported_setsAvatarImageUnsupportedError() = runTest(testDispatcher) {
        repository.updateMemberAvatarResult = Result.failure(
            AvatarImagePrepareException(AvatarImagePrepareException.Reason.UnsupportedFormat),
        )
        repository.seedMember(
            householdId = "household-1",
            member = HouseholdMember(
                id = "member-1",
                householdId = "household-1",
                kind = HouseholdMemberKind.Person,
                displayName = "Alice",
                role = HouseholdMemberRole.Editor,
                referenceId = "user-alice",
            ),
            ownerId = "owner",
            ownerDisplayName = "Owner",
        )
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        val member = model.uiState.value.members.single { it.id == "member-1" }
        model.uploadMemberAvatar("household-1", member, ByteArray(1), "image/gif")
        advanceUntilIdle()

        assertEquals(HouseholdMembersError.AvatarImageUnsupported, model.uiState.value.error)
    }

    @Test
    fun uploadHouseholdAvatar_storageMimeRejected_setsAvatarImageUnsupportedError() = runTest(testDispatcher) {
        householdRepository.updateHouseholdAvatarResult =
            Result.failure(IllegalStateException("invalid_mime_type image/heic is not supported"))
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(1), "image/heic")
        advanceUntilIdle()

        assertEquals(HouseholdMembersError.AvatarImageUnsupported, model.uiState.value.error)
    }

    @Test
    fun uploadHouseholdAvatar_storageTooLarge_setsAvatarImageTooLargeError() = runTest(testDispatcher) {
        householdRepository.updateHouseholdAvatarResult =
            Result.failure(IllegalStateException("Payload too large"))
        model.bindHousehold("household-1", "Test Household", "owner", "Owner", "owner")
        advanceUntilIdle()

        model.uploadHouseholdAvatar("household-1", ByteArray(1), "image/jpeg")
        advanceUntilIdle()

        assertEquals(HouseholdMembersError.AvatarImageTooLarge, model.uiState.value.error)
    }
}
