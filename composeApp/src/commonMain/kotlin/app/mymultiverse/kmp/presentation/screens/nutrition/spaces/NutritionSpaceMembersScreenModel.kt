package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.ContactGroup
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.GroupMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

sealed interface SpaceMembersError {
    data object Generic : SpaceMembersError
    data object EmailRequired : SpaceMembersError
    data object EmailNotFound : SpaceMembersError
    data object GroupNameRequired : SpaceMembersError
    data object EventExpiresRequired : SpaceMembersError
    data object NotConfigured : SpaceMembersError
}

data class SpaceMembersUiState(
    val members: List<SpaceMember> = emptyList(),
    val groups: List<ContactGroup> = emptyList(),
    val groupMembers: List<GroupMember> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showAddPersonDialog: Boolean = false,
    val showCreateGroupDialog: Boolean = false,
    val showAddGroupDialog: Boolean = false,
    val showManageGroupDialog: Boolean = false,
    val selectedGroupId: String? = null,
    val emailInput: String = "",
    val groupNameInput: String = "",
    val groupEventLabelInput: String = "",
    val groupExpiresInput: String = "",
    val groupMemberEmailInput: String = "",
    val groupLifecycle: GroupLifecycle = GroupLifecycle.Persistent,
    val selectedRole: SpaceMemberRole = SpaceMemberRole.Editor,
    val successMessageKey: SpaceMembersSuccess? = null,
    val error: SpaceMembersError? = null,
)

enum class SpaceMembersSuccess {
    InviteSent,
    MemberAdded,
    GroupMemberAdded,
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
                collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
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

    fun openCreateGroupDialog() {
        _uiState.update {
            it.copy(
                showCreateGroupDialog = true,
                groupNameInput = "",
                groupEventLabelInput = "",
                groupExpiresInput = "",
                groupLifecycle = GroupLifecycle.Persistent,
                error = null,
                successMessageKey = null,
            )
        }
    }

    fun openAddGroupDialog() {
        _uiState.update { it.copy(showAddGroupDialog = true, error = null, successMessageKey = null) }
    }

    fun openManageGroupDialog(groupId: String) {
        _uiState.update {
            it.copy(
                showManageGroupDialog = true,
                selectedGroupId = groupId,
                groupMemberEmailInput = "",
                error = null,
                successMessageKey = null,
            )
        }
        scope.launch {
            collaborationRepository.observeGroupMembers(groupId).collect { members ->
                _uiState.update { state -> state.copy(groupMembers = members) }
            }
        }
        scope.launch {
            runCatching { collaborationRepository.refreshGroupMembers(groupId) }
        }
    }

    fun dismissDialogs() {
        _uiState.update {
            it.copy(
                showAddPersonDialog = false,
                showCreateGroupDialog = false,
                showAddGroupDialog = false,
                showManageGroupDialog = false,
                selectedGroupId = null,
                groupMembers = emptyList(),
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

    fun onGroupEventLabelChange(value: String) {
        _uiState.update { it.copy(groupEventLabelInput = value, error = null) }
    }

    fun onGroupExpiresChange(value: String) {
        _uiState.update { it.copy(groupExpiresInput = value, error = null) }
    }

    fun onGroupMemberEmailChange(value: String) {
        _uiState.update { it.copy(groupMemberEmailInput = value, error = null) }
    }

    fun onGroupLifecycleChange(lifecycle: GroupLifecycle) {
        _uiState.update { it.copy(groupLifecycle = lifecycle, error = null) }
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

    fun submitCreateGroup(spaceId: String) {
        val name = _uiState.value.groupNameInput
        if (name.isBlank()) {
            _uiState.update { it.copy(error = SpaceMembersError.GroupNameRequired) }
            return
        }
        val lifecycle = _uiState.value.groupLifecycle
        val expiresAt = if (lifecycle == GroupLifecycle.Event) {
            parseDateInput(_uiState.value.groupExpiresInput)
                ?: run {
                    _uiState.update { it.copy(error = SpaceMembersError.EventExpiresRequired) }
                    return
                }
        } else {
            null
        }

        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val created = collaborationRepository.createGroup(
                name = name,
                lifecycle = lifecycle,
                eventLabel = _uiState.value.groupEventLabelInput,
                expiresAtEpochMillis = expiresAt,
            )
            created.onSuccess { group ->
                collaborationRepository.addGroupToSpace(spaceId, group.id)
                collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
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
            if (result.isSuccess) {
                collaborationRepository.refreshMembers(spaceId, activeOwnerId, activeOwnerDisplayName)
            }
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    showAddGroupDialog = result.isFailure,
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
        }
    }

    fun submitAddGroupMember() {
        val groupId = _uiState.value.selectedGroupId ?: return
        val email = _uiState.value.groupMemberEmailInput
        if (email.isBlank()) {
            _uiState.update { it.copy(error = SpaceMembersError.EmailRequired) }
            return
        }
        scope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = collaborationRepository.addUserToGroup(groupId, email)
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    groupMemberEmailInput = if (result.isSuccess) "" else state.groupMemberEmailInput,
                    successMessageKey = if (result.isSuccess) SpaceMembersSuccess.GroupMemberAdded else null,
                    error = result.exceptionOrNull()?.let { mapFailure(it) },
                )
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

    private fun parseDateInput(raw: String): Long? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        return runCatching {
            LocalDate.parse(trimmed)
                .atStartOfDayIn(TimeZone.UTC)
                .toEpochMilliseconds()
        }.getOrNull()
    }

    private fun mapFailure(throwable: Throwable): SpaceMembersError =
        when (throwable.message) {
            "member_email_required" -> SpaceMembersError.EmailRequired
            "member_email_not_found" -> SpaceMembersError.EmailNotFound
            "group_name_required" -> SpaceMembersError.GroupNameRequired
            "event_expires_required" -> SpaceMembersError.EventExpiresRequired
            "supabase_not_configured" -> SpaceMembersError.NotConfigured
            else -> SpaceMembersError.Generic
        }
}
