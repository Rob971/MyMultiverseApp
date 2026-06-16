package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SpaceMembersError {
    data object Generic : SpaceMembersError
    data object EmailRequired : SpaceMembersError
    data object EmailNotFound : SpaceMembersError
    data object NotConfigured : SpaceMembersError
}

data class SpaceMembersUiState(
    val members: List<SpaceMember> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddPersonDialog: Boolean = false,
    val emailInput: String = "",
    val selectedRole: SpaceMemberRole = SpaceMemberRole.Editor,
    val successMessageKey: SpaceMembersSuccess? = null,
    val error: SpaceMembersError? = null,
)

enum class SpaceMembersSuccess {
    InviteSent,
    MemberAdded,
}

class NutritionSpaceMembersScreenModel(
    private val collaborationRepository: SpaceCollaborationRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(SpaceMembersUiState())
    val uiState: StateFlow<SpaceMembersUiState> = _uiState.asStateFlow()

    private var activeSpaceId: String? = null
    private var activeOwnerId: String = ""
    private var activeOwnerDisplayName: String = ""

    fun bindSpace(spaceId: String, ownerId: String, ownerDisplayName: String) {
        if (activeSpaceId == spaceId &&
            activeOwnerId == ownerId &&
            activeOwnerDisplayName == ownerDisplayName
        ) {
            return
        }
        activeSpaceId = spaceId
        activeOwnerId = ownerId
        activeOwnerDisplayName = ownerDisplayName

        scope.launch {
            collaborationRepository.observeMembers(spaceId).collect { members ->
                _uiState.update { it.copy(members = members) }
            }
        }
        refresh(spaceId)
    }

    fun refresh(spaceId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
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
                successMessageKey = null,
            )
        }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                showAddPersonDialog = false,
                error = null,
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(emailInput = value, error = null) }
    }

    fun onRoleChange(role: SpaceMemberRole) {
        _uiState.update { it.copy(selectedRole = role, error = null) }
    }

    fun submitAddPerson(spaceId: String) {
        val email = _uiState.value.emailInput
        if (email.isBlank()) {
            _uiState.update { it.copy(error = SpaceMembersError.EmailRequired) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = collaborationRepository.addMemberByEmail(
                spaceId = spaceId,
                email = email,
                role = _uiState.value.selectedRole,
            )
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    showAddPersonDialog = result.isFailure,
                    successMessageKey = when {
                        result.isFailure -> null
                        result.getOrNull() == AddMemberResult.InviteSent -> SpaceMembersSuccess.InviteSent
                        else -> SpaceMembersSuccess.MemberAdded
                    },
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
            if (result.isSuccess) {
                collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
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

    private fun mapFailure(throwable: Throwable): SpaceMembersError =
        when (throwable.message) {
            "member_email_required" -> SpaceMembersError.EmailRequired
            "member_email_not_found" -> SpaceMembersError.EmailNotFound
            "supabase_not_configured" -> SpaceMembersError.NotConfigured
            else -> SpaceMembersError.Generic
        }
}
