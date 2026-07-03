package app.mymultiverse.ammo.presentation.screens.household

import app.mymultiverse.ammo.domain.model.sharing.AddMemberResult
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.sharing.canManageHouseholdMembers
import app.mymultiverse.ammo.domain.sharing.canAssignAdminRole
import app.mymultiverse.ammo.domain.sharing.canChangeRoleOf
import app.mymultiverse.ammo.domain.sharing.canInviteWithRole
import app.mymultiverse.ammo.domain.sharing.canRemoveMember
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HouseholdMembersError {
    data object Generic : HouseholdMembersError
    data object EmailRequired : HouseholdMembersError
    data object NotConfigured : HouseholdMembersError
    data object CannotAddSelf : HouseholdMembersError
    data object MemberAlreadyExists : HouseholdMembersError
    data object InsufficientRole : HouseholdMembersError
    data object InviteeHouseholdAlreadyActive : HouseholdMembersError
    data object MemberLimitReached : HouseholdMembersError
    data object OwnerMustTransferOrDissolve : HouseholdMembersError
    data object InvalidTransferTarget : HouseholdMembersError
    data object TransferTargetNotMember : HouseholdMembersError
}

enum class HouseholdMembersLeaveAction {
    Leave,
    Dissolve,
}

data class HouseholdInviteSharePayload(
    val householdName: String,
    val inviteToken: String,
)

data class HouseholdMembersUiState(
    val members: List<HouseholdMember> = emptyList(),
    val outboundInvites: List<HouseholdInvite> = emptyList(),
    val canManageMembers: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddPersonDialog: Boolean = false,
    val showInviteChooserDialog: Boolean = false,
    val emailInput: String = "",
    val selectedRole: HouseholdMemberRole = HouseholdMemberRole.Editor,
    val successMessageKey: HouseholdMembersSuccess? = null,
    val invitedEmailForSuccess: String? = null,
    val pendingInviteShare: HouseholdInviteSharePayload? = null,
    val transferredToDisplayName: String? = null,
    val error: HouseholdMembersError? = null,
    val dialogError: HouseholdMembersError? = null,
    val canLeave: Boolean = false,
    val canDissolve: Boolean = false,
    val showOwnerTransferHint: Boolean = false,
    val canTransferOwnership: Boolean = false,
    val transferCandidates: List<HouseholdMember> = emptyList(),
    val showTransferDialog: Boolean = false,
    val showAddDependantDialog: Boolean = false,
    val dependantNameInput: String = "",
    val selectedTransferMemberId: String? = null,
    val isTransferring: Boolean = false,
    val pendingLeaveAction: HouseholdMembersLeaveAction? = null,
    val isLeaving: Boolean = false,
    val showRoleChangeDialog: Boolean = false,
    val roleChangeTarget: HouseholdMember? = null,
    val selectedMemberRole: HouseholdMemberRole = HouseholdMemberRole.Editor,
    val isUpdatingRole: Boolean = false,
    val currentUserRole: HouseholdMemberRole? = null,
    val uploadingAvatarMemberId: String? = null,
)

enum class HouseholdMembersSuccess {
    InviteSent,
    MemberAdded,
    OwnershipTransferred,
    DependantAdded,
    RoleUpdated,
}

class HouseholdMembersScreenModel(
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val householdRepository: HouseholdRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(HouseholdMembersUiState())
    val uiState: StateFlow<HouseholdMembersUiState> = _uiState.asStateFlow()

    private var activeHouseholdId: String? = null
    private var activeHouseholdName: String = ""
    private var activeOwnerId: String = ""
    private var activeOwnerDisplayName: String = ""
    private var activeUserIsOwner: Boolean = false
    private var activeUserRole: HouseholdMemberRole? = null
    private var observeJob: Job? = null

    fun bindHousehold(
        householdId: String,
        householdName: String,
        ownerId: String,
        ownerDisplayName: String,
        currentUserId: String?,
    ) {
        val identityChanged = activeHouseholdId != householdId ||
            activeOwnerId != ownerId ||
            activeOwnerDisplayName != ownerDisplayName
        val metadataChanged = identityChanged || activeHouseholdName != householdName
        if (!metadataChanged) {
            return
        }
        activeHouseholdId = householdId
        activeHouseholdName = householdName
        activeOwnerId = ownerId
        activeOwnerDisplayName = ownerDisplayName

        if (identityChanged) {
            startObserving(householdId)
            refresh(householdId)
        }
    }

    private fun startObserving(householdId: String) {
        observeJob?.cancel()
        observeJob = scope.launch {
            launch {
                collaborationRepository.observeMembers(householdId).collect { members ->
                    _uiState.update { state ->
                        val otherMembers = members.count { it.role != HouseholdMemberRole.Owner }
                        val transferCandidates = members.filter {
                            it.role != HouseholdMemberRole.Owner && it.kind == HouseholdMemberKind.Person
                        }
                        val isOwner = activeUserRole == HouseholdMemberRole.Owner
                        state.copy(
                            members = members,
                            canDissolve = isOwner && otherMembers == 0,
                            showOwnerTransferHint = isOwner && otherMembers > 0,
                            canTransferOwnership = isOwner && transferCandidates.isNotEmpty(),
                            transferCandidates = transferCandidates,
                            canLeave = activeUserRole != null && activeUserRole != HouseholdMemberRole.Owner,
                        )
                    }
                }
            }
            launch {
                collaborationRepository.observeOutboundInvites(householdId).collect { invites ->
                    _uiState.update { it.copy(outboundInvites = invites) }
                }
            }
        }
    }

    private fun applyMembershipRole(role: HouseholdMemberRole?) {
        activeUserRole = role
        activeUserIsOwner = role == HouseholdMemberRole.Owner
        val canManage = role?.canManageHouseholdMembers() == true
        _uiState.update {
            it.copy(
                currentUserRole = role,
                canManageMembers = canManage,
                canLeave = role != null && role != HouseholdMemberRole.Owner,
            )
        }
    }

    fun refresh(householdId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            householdRepository.refreshMembership()
                .onSuccess { status ->
                    if (status is HouseholdMembershipStatus.Active) {
                        applyMembershipRole(status.role)
                    }
                }
            runCatching {
                collaborationRepository.refreshMembers(householdId, activeOwnerId, activeOwnerDisplayName)
                collaborationRepository.refreshOutboundInvites(householdId)
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(isLoading = false, error = mapFailure(throwable))
                }
            }.onSuccess {
                _uiState.update { state -> state.copy(isLoading = false) }
            }
        }
    }

    fun openInviteChooser() {
        _uiState.update {
            it.copy(
                showInviteChooserDialog = true,
                error = null,
                dialogError = null,
            )
        }
    }

    fun dismissInviteChooser() {
        _uiState.update { it.copy(showInviteChooserDialog = false) }
    }

    fun openAddPersonDialog() {
        _uiState.update {
            it.copy(
                showInviteChooserDialog = false,
                showAddPersonDialog = true,
                emailInput = "",
                selectedRole = HouseholdMemberRole.Editor,
                error = null,
                dialogError = null,
                successMessageKey = null,
            )
        }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                showAddPersonDialog = false,
                dialogError = null,
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(emailInput = value, dialogError = null) }
    }

    fun onRoleChange(role: HouseholdMemberRole) {
        val actorRole = _uiState.value.currentUserRole
        if (actorRole != null && !actorRole.canInviteWithRole(role)) return
        _uiState.update { it.copy(selectedRole = role, dialogError = null) }
    }

    fun openRoleChangeDialog(member: HouseholdMember) {
        if (member.kind != HouseholdMemberKind.Person) return
        val actorRole = _uiState.value.currentUserRole ?: return
        if (!actorRole.canChangeRoleOf(member.role)) return
        _uiState.update {
            it.copy(
                showRoleChangeDialog = true,
                roleChangeTarget = member,
                selectedMemberRole = member.role,
                dialogError = null,
            )
        }
    }

    fun dismissRoleChangeDialog() {
        _uiState.update {
            it.copy(
                showRoleChangeDialog = false,
                roleChangeTarget = null,
                isUpdatingRole = false,
                dialogError = null,
            )
        }
    }

    fun onMemberRoleChange(role: HouseholdMemberRole) {
        val actorRole = _uiState.value.currentUserRole
        val target = _uiState.value.roleChangeTarget ?: return
        if (actorRole != null && !actorRole.canChangeRoleOf(target.role)) return
        if (actorRole != null && !actorRole.canInviteWithRole(role)) return
        _uiState.update { it.copy(selectedMemberRole = role, dialogError = null) }
    }

    fun confirmRoleChange(householdId: String) {
        val member = _uiState.value.roleChangeTarget ?: return
        val role = _uiState.value.selectedMemberRole
        if (role == member.role) {
            dismissRoleChangeDialog()
            return
        }
        scope.launch {
            val previousMembers = _uiState.value.members
            _uiState.update { state ->
                state.copy(
                    isUpdatingRole = true,
                    dialogError = null,
                    error = null,
                    members = state.members.map { current ->
                        if (current.id == member.id) current.copy(role = role) else current
                    },
                )
            }
            collaborationRepository.updateMemberRole(member.id, role)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isUpdatingRole = false,
                            showRoleChangeDialog = false,
                            roleChangeTarget = null,
                            successMessageKey = HouseholdMembersSuccess.RoleUpdated,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isUpdatingRole = false,
                            members = previousMembers,
                            dialogError = mapFailure(throwable),
                        )
                    }
                }
        }
    }

    fun submitAddPerson(householdId: String) {
        val email = _uiState.value.emailInput.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(dialogError = HouseholdMembersError.EmailRequired) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isSaving = true, dialogError = null, error = null) }
            val result = collaborationRepository.addMemberByEmail(
                householdId = householdId,
                email = email,
                role = _uiState.value.selectedRole,
            )
            result
                .onSuccess { addResult ->
                    collaborationRepository.refreshMembers(householdId, activeOwnerId, activeOwnerDisplayName)
                    collaborationRepository.refreshOutboundInvites(householdId)
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            showAddPersonDialog = false,
                            emailInput = "",
                            dialogError = null,
                            invitedEmailForSuccess = when (addResult) {
                                is AddMemberResult.InviteSent -> email
                                AddMemberResult.Added -> null
                            },
                            pendingInviteShare = when (addResult) {
                                is AddMemberResult.InviteSent -> HouseholdInviteSharePayload(
                                    householdName = activeHouseholdName,
                                    inviteToken = addResult.inviteToken,
                                )
                                AddMemberResult.Added -> null
                            },
                            successMessageKey = when (addResult) {
                                is AddMemberResult.InviteSent -> HouseholdMembersSuccess.InviteSent
                                AddMemberResult.Added -> HouseholdMembersSuccess.MemberAdded
                            },
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            showAddPersonDialog = true,
                            dialogError = mapFailure(throwable),
                        )
                    }
                }
        }
    }

    fun removeMember(member: HouseholdMember, householdId: String) {
        if (member.role == HouseholdMemberRole.Owner) return
        val actorRole = _uiState.value.currentUserRole ?: return
        if (!actorRole.canRemoveMember(member.role)) return
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = when (member.kind) {
                HouseholdMemberKind.Dependant -> collaborationRepository.removeDependant(member.id)
                else -> collaborationRepository.removeMember(member.id)
            }
            if (result.isSuccess) {
                collaborationRepository.refreshMembers(householdId, activeOwnerId, activeOwnerDisplayName)
            }
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
        }
    }

    fun openAddDependantDialog() {
        _uiState.update {
            it.copy(
                showInviteChooserDialog = false,
                showAddDependantDialog = true,
                dependantNameInput = "",
                dialogError = null,
            )
        }
    }

    fun dismissAddDependantDialog() {
        _uiState.update { it.copy(showAddDependantDialog = false, dialogError = null) }
    }

    fun onDependantNameChange(value: String) {
        _uiState.update { it.copy(dependantNameInput = value, dialogError = null) }
    }

    fun submitAddDependant(householdId: String) {
        val name = _uiState.value.dependantNameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(dialogError = HouseholdMembersError.Generic) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isSaving = true, dialogError = null, error = null) }
            collaborationRepository.addDependant(householdId, name)
                .onSuccess {
                    collaborationRepository.refreshMembers(householdId, activeOwnerId, activeOwnerDisplayName)
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            showAddDependantDialog = false,
                            dependantNameInput = "",
                            successMessageKey = HouseholdMembersSuccess.DependantAdded,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            showAddDependantDialog = true,
                            dialogError = mapFailure(throwable),
                        )
                    }
                }
        }
    }

    fun consumePendingInviteShare() {
        _uiState.update { it.copy(pendingInviteShare = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessageKey = null, transferredToDisplayName = null, invitedEmailForSuccess = null) }
    }

    fun requestLeave() {
        _uiState.update { it.copy(pendingLeaveAction = HouseholdMembersLeaveAction.Leave, error = null) }
    }

    fun requestDissolve() {
        _uiState.update { it.copy(pendingLeaveAction = HouseholdMembersLeaveAction.Dissolve, error = null) }
    }

    fun dismissLeaveDissolve() {
        _uiState.update { it.copy(pendingLeaveAction = null) }
    }

    fun openTransferDialog() {
        val candidates = _uiState.value.transferCandidates
        _uiState.update {
            it.copy(
                showTransferDialog = true,
                selectedTransferMemberId = candidates.firstOrNull()?.referenceId,
                error = null,
            )
        }
    }

    fun dismissTransferDialog() {
        _uiState.update {
            it.copy(
                showTransferDialog = false,
                selectedTransferMemberId = null,
            )
        }
    }

    fun selectTransferMember(memberReferenceId: String) {
        _uiState.update { it.copy(selectedTransferMemberId = memberReferenceId, error = null) }
    }

    fun confirmTransferOwnership(householdId: String) {
        val targetId = _uiState.value.selectedTransferMemberId
        if (targetId.isNullOrBlank()) {
            _uiState.update { it.copy(error = HouseholdMembersError.InvalidTransferTarget) }
            return
        }
        scope.launch {
            val targetName = _uiState.value.transferCandidates
                .find { it.referenceId == targetId }
                ?.displayName
                .orEmpty()
            _uiState.update {
                it.copy(isTransferring = true, showTransferDialog = false, error = null)
            }
            householdRepository.transferOwnership(targetId)
                .onSuccess {
                    householdRepository.refreshMembership().onSuccess { status ->
                        if (status is HouseholdMembershipStatus.Active) {
                            activeOwnerId = status.household.ownerId
                            activeOwnerDisplayName = status.household.ownerDisplayName.orEmpty()
                            activeUserIsOwner = status.membership.role == HouseholdMemberRole.Owner
                            collaborationRepository.refreshMembers(
                                householdId,
                                status.household.ownerId,
                                status.household.ownerDisplayName.orEmpty(),
                            )
                        }
                    }
                    _uiState.update { state ->
                        state.copy(
                            isTransferring = false,
                            selectedTransferMemberId = null,
                            successMessageKey = HouseholdMembersSuccess.OwnershipTransferred,
                            transferredToDisplayName = targetName,
                            canManageMembers = false,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isTransferring = false,
                            showTransferDialog = true,
                            error = mapFailure(throwable),
                        )
                    }
                }
        }
    }

    fun uploadMemberAvatar(
        householdId: String,
        member: HouseholdMember,
        imageBytes: ByteArray,
        contentType: String,
    ) {
        scope.launch {
            _uiState.update {
                it.copy(
                    uploadingAvatarMemberId = member.id,
                    error = null,
                )
            }
            collaborationRepository.updateMemberAvatar(
                householdId = householdId,
                member = member,
                imageBytes = imageBytes,
                contentType = contentType,
            )
                .onSuccess {
                    collaborationRepository.refreshMembers(householdId, activeOwnerId, activeOwnerDisplayName)
                    _uiState.update { it.copy(uploadingAvatarMemberId = null) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            uploadingAvatarMemberId = null,
                            error = mapFailure(throwable),
                        )
                    }
                }
        }
    }

    fun confirmLeaveOrDissolve() {
        val action = _uiState.value.pendingLeaveAction ?: return
        scope.launch {
            _uiState.update { it.copy(isLeaving = true, pendingLeaveAction = null, error = null) }
            sessionCoordinator.deactivate()
            val result = when (action) {
                HouseholdMembersLeaveAction.Leave -> householdRepository.leaveHousehold()
                HouseholdMembersLeaveAction.Dissolve -> householdRepository.dissolveHousehold()
            }
            result
                .onSuccess { householdRepository.refreshMembership() }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(error = mapFailure(throwable))
                    }
                }
            _uiState.update { it.copy(isLeaving = false) }
        }
    }

    private fun mapFailure(throwable: Throwable): HouseholdMembersError {
        val message = throwable.message
        return when {
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.MEMBER_EMAIL_REQUIRED, message) ->
                HouseholdMembersError.EmailRequired
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.SUPABASE_NOT_CONFIGURED, message) ->
                HouseholdMembersError.NotConfigured
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.MEMBER_CANNOT_ADD_SELF, message) ->
                HouseholdMembersError.CannotAddSelf
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.MEMBER_ALREADY_EXISTS, message) ->
                HouseholdMembersError.MemberAlreadyExists
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INSUFFICIENT_ROLE, message) ->
                HouseholdMembersError.InsufficientRole
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.INVITEE_HOUSEHOLD_ALREADY_ACTIVE,
                message,
            ) -> HouseholdMembersError.InviteeHouseholdAlreadyActive
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.HOUSEHOLD_MEMBER_LIMIT_REACHED,
                message,
            ) -> HouseholdMembersError.MemberLimitReached
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.OWNER_MUST_TRANSFER_OR_DISSOLVE,
                message,
            ) -> HouseholdMembersError.OwnerMustTransferOrDissolve
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.INVALID_TRANSFER_TARGET,
                message,
            ) -> HouseholdMembersError.InvalidTransferTarget
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.TRANSFER_TARGET_NOT_MEMBER,
                message,
            ) -> HouseholdMembersError.TransferTargetNotMember
            else -> HouseholdMembersError.Generic
        }
    }
}
