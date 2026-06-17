package app.mymultiverse.kmp.presentation.screens.household

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import app.mymultiverse.kmp.domain.sharing.CollaborationErrorCodes
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

data class HouseholdMembersUiState(
    val members: List<SpaceMember> = emptyList(),
    val outboundInvites: List<SpaceInvite> = emptyList(),
    val canManageMembers: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddPersonDialog: Boolean = false,
    val emailInput: String = "",
    val selectedRole: SpaceMemberRole = SpaceMemberRole.Editor,
    val successMessageKey: HouseholdMembersSuccess? = null,
    val invitedEmailForSuccess: String? = null,
    val transferredToDisplayName: String? = null,
    val error: HouseholdMembersError? = null,
    val dialogError: HouseholdMembersError? = null,
    val canLeave: Boolean = false,
    val canDissolve: Boolean = false,
    val showOwnerTransferHint: Boolean = false,
    val canTransferOwnership: Boolean = false,
    val transferCandidates: List<SpaceMember> = emptyList(),
    val showTransferDialog: Boolean = false,
    val selectedTransferMemberId: String? = null,
    val isTransferring: Boolean = false,
    val pendingLeaveAction: HouseholdMembersLeaveAction? = null,
    val isLeaving: Boolean = false,
)

enum class HouseholdMembersSuccess {
    InviteSent,
    MemberAdded,
    OwnershipTransferred,
}

class HouseholdMembersScreenModel(
    private val collaborationRepository: SpaceCollaborationRepository,
    private val householdRepository: HouseholdRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(HouseholdMembersUiState())
    val uiState: StateFlow<HouseholdMembersUiState> = _uiState.asStateFlow()

    private var activeSpaceId: String? = null
    private var activeOwnerId: String = ""
    private var activeOwnerDisplayName: String = ""
    private var activeUserIsOwner: Boolean = false
    private var observeJob: Job? = null

    fun bindHousehold(
        spaceId: String,
        ownerId: String,
        ownerDisplayName: String,
        currentUserId: String?,
    ) {
        val canManage = currentUserId != null && currentUserId == ownerId
        activeUserIsOwner = canManage
        if (activeSpaceId == spaceId &&
            activeOwnerId == ownerId &&
            activeOwnerDisplayName == ownerDisplayName &&
            _uiState.value.canManageMembers == canManage
        ) {
            return
        }
        activeSpaceId = spaceId
        activeOwnerId = ownerId
        activeOwnerDisplayName = ownerDisplayName
        _uiState.update {
            it.copy(
                canManageMembers = canManage,
                canLeave = currentUserId != null && !canManage,
            )
        }

        observeJob?.cancel()
        observeJob = scope.launch {
            launch {
                collaborationRepository.observeMembers(spaceId).collect { members ->
                    _uiState.update { state ->
                        val otherMembers = members.count { it.role != SpaceMemberRole.Owner }
                        val transferCandidates = members.filter {
                            it.role != SpaceMemberRole.Owner && it.kind == SpaceMemberKind.Person
                        }
                        state.copy(
                            members = members,
                            canDissolve = activeUserIsOwner && otherMembers == 0,
                            showOwnerTransferHint = activeUserIsOwner && otherMembers > 0,
                            canTransferOwnership = activeUserIsOwner && transferCandidates.isNotEmpty(),
                            transferCandidates = transferCandidates,
                            canLeave = !activeUserIsOwner,
                        )
                    }
                }
            }
            launch {
                collaborationRepository.observeOutboundInvites(spaceId).collect { invites ->
                    _uiState.update { it.copy(outboundInvites = invites) }
                }
            }
        }
        refresh(spaceId)
    }

    fun refresh(spaceId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
                collaborationRepository.refreshOutboundInvites(spaceId)
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(isLoading = false, error = mapFailure(throwable))
                }
            }.onSuccess {
                _uiState.update { state -> state.copy(isLoading = false) }
            }
        }
    }

    fun openAddPersonDialog() {
        _uiState.update {
            it.copy(
                showAddPersonDialog = true,
                emailInput = "",
                selectedRole = SpaceMemberRole.Editor,
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

    fun onRoleChange(role: SpaceMemberRole) {
        _uiState.update { it.copy(selectedRole = role, dialogError = null) }
    }

    fun submitAddPerson(spaceId: String) {
        val email = _uiState.value.emailInput.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(dialogError = HouseholdMembersError.EmailRequired) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isSaving = true, dialogError = null, error = null) }
            val result = collaborationRepository.addMemberByEmail(
                spaceId = spaceId,
                email = email,
                role = _uiState.value.selectedRole,
            )
            result
                .onSuccess { addResult ->
                    collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
                    collaborationRepository.refreshOutboundInvites(spaceId)
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            showAddPersonDialog = false,
                            emailInput = "",
                            dialogError = null,
                            invitedEmailForSuccess = when (addResult) {
                                AddMemberResult.InviteSent -> email
                                AddMemberResult.Added -> null
                            },
                            successMessageKey = when (addResult) {
                                AddMemberResult.InviteSent -> HouseholdMembersSuccess.InviteSent
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

    fun removeMember(memberId: String, spaceId: String) {
        if (memberId.startsWith("owner-")) return
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = collaborationRepository.removeMember(memberId)
            if (result.isSuccess) {
                collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
            }
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
        }
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

    fun confirmTransferOwnership(spaceId: String) {
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
                            activeUserIsOwner = status.membership.role == SpaceMemberRole.Owner
                            collaborationRepository.refreshMembers(
                                spaceId,
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
