package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
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
    data object GroupNameRequired : SpaceMembersError
    data object NotConfigured : SpaceMembersError
}

data class SpaceMembersUiState(
    val members: List<SpaceMember> = emptyList(),
    val groups: List<ContactGroup> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddPersonDialog: Boolean = false,
    val showCreateGroupDialog: Boolean = false,
    val showAddGroupDialog: Boolean = false,
    val emailInput: String = "",
    val groupNameInput: String = "",
    val groupLifecycle: GroupLifecycle = GroupLifecycle.Persistent,
    val error: SpaceMembersError? = null,
)

class NutritionSpaceMembersScreenModel(
    private val collaborationRepository: SpaceCollaborationRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(SpaceMembersUiState())
    val uiState: StateFlow<SpaceMembersUiState> = _uiState.asStateFlow()

    private var activeSpaceId: String? = null

    fun bindSpace(spaceId: String) {
        if (activeSpaceId == spaceId) return
        activeSpaceId = spaceId
        scope.launch {
            collaborationRepository.observeMembers(spaceId).collect { members ->
                _uiState.update { it.copy(members = members) }
            }
        }
        scope.launch {
            collaborationRepository.observeGroups().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
        refresh(spaceId)
    }

    fun refresh(spaceId: String) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                collaborationRepository.refreshMembers(spaceId)
                collaborationRepository.refreshGroups()
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
        _uiState.update { it.copy(showAddPersonDialog = true, emailInput = "", error = null) }
    }

    fun openCreateGroupDialog() {
        _uiState.update {
            it.copy(
                showCreateGroupDialog = true,
                groupNameInput = "",
                groupLifecycle = GroupLifecycle.Persistent,
                error = null,
            )
        }
    }

    fun openAddGroupDialog() {
        _uiState.update { it.copy(showAddGroupDialog = true, error = null) }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                showAddPersonDialog = false,
                showCreateGroupDialog = false,
                showAddGroupDialog = false,
                error = null,
            )
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(emailInput = value, error = null) }
    }

    fun onGroupNameChange(value: String) {
        _uiState.update { it.copy(groupNameInput = value, error = null) }
    }

    fun onGroupLifecycleChange(lifecycle: GroupLifecycle) {
        _uiState.update { it.copy(groupLifecycle = lifecycle, error = null) }
    }

    fun submitAddPerson(spaceId: String) {
        val email = _uiState.value.emailInput
        if (email.isBlank()) {
            _uiState.update { it.copy(error = SpaceMembersError.EmailRequired) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = collaborationRepository.addMemberByEmail(spaceId, email)
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    showAddPersonDialog = result.isSuccess,
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
        }
    }

    fun submitCreateGroup(spaceId: String) {
        val name = _uiState.value.groupNameInput
        if (name.isBlank()) {
            _uiState.update { it.copy(error = SpaceMembersError.GroupNameRequired) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val created = collaborationRepository.createGroup(
                name = name,
                lifecycle = _uiState.value.groupLifecycle,
            )
            created.onSuccess { group ->
                collaborationRepository.addGroupToSpace(spaceId, group.id)
            }
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    showCreateGroupDialog = created.isSuccess,
                    error = created.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
        }
    }

    fun addExistingGroupToSpace(spaceId: String, groupId: String) {
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = collaborationRepository.addGroupToSpace(spaceId, groupId)
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    showAddGroupDialog = result.isSuccess,
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
        }
    }

    fun removeMember(memberId: String) {
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = collaborationRepository.removeMember(memberId)
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
        }
    }

    private fun mapFailure(throwable: Throwable): SpaceMembersError =
        when (throwable.message) {
            "member_email_required" -> SpaceMembersError.EmailRequired
            "member_email_not_found" -> SpaceMembersError.EmailNotFound
            "group_name_required" -> SpaceMembersError.GroupNameRequired
            "supabase_not_configured" -> SpaceMembersError.NotConfigured
            else -> SpaceMembersError.Generic
        }
}
