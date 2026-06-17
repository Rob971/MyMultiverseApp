package app.mymultiverse.kmp.presentation.screens.household

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.CollaborationErrorCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    val error: HouseholdMembersError? = null,
    val dialogError: HouseholdMembersError? = null,
)

enum class HouseholdMembersSuccess {
    InviteSent,
    MemberAdded,
}

class HouseholdMembersScreenModel(
    private val collaborationRepository: SpaceCollaborationRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(HouseholdMembersUiState())
    val uiState: StateFlow<HouseholdMembersUiState> = _uiState.asStateFlow()

    private var activeSpaceId: String? = null
    private var activeOwnerId: String = ""
    private var activeOwnerDisplayName: String = ""
    private var observeJob: Job? = null

    fun bindHousehold(
        spaceId: String,
        ownerId: String,
        ownerDisplayName: String,
        currentUserId: String?,
    ) {
        val canManage = currentUserId != null && currentUserId == ownerId
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
        _uiState.update { it.copy(canManageMembers = canManage) }

        observeJob?.cancel()
        observeJob = scope.launch {
            launch {
                collaborationRepository.observeMembers(spaceId).collect { members ->
                    _uiState.update { it.copy(members = members) }
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
        _uiState.update { it.copy(successMessageKey = null) }
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
            else -> HouseholdMembersError.Generic
        }
    }
}
