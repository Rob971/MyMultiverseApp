package app.mymultiverse.kmp.presentation.screens.household

import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.domain.model.sharing.HouseholdGateError
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import app.mymultiverse.kmp.domain.sharing.CollaborationErrorCodes

data class HouseholdGateUiState(
    val membershipStatus: HouseholdMembershipStatus = HouseholdMembershipStatus.Loading,
    val householdNameInput: String = "",
    val isCreating: Boolean = false,
    val pendingInvites: List<HouseholdInvite> = emptyList(),
    val inviteActionMessage: InviteActionMessage? = null,
)

sealed interface InviteActionMessage {
    data object AcceptFailed : InviteActionMessage
    data object EmailMismatch : InviteActionMessage
    data class Joined(val householdName: String) : InviteActionMessage
}

data class SwitchHouseholdPrompt(
    val inviteId: String,
    val invitedHouseholdName: String,
    val currentHouseholdName: String,
)

class HouseholdGateScreenModel(
    private val householdRepository: HouseholdRepository,
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val authRepository: AuthRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val logger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(HouseholdGateUiState())
    val uiState: StateFlow<HouseholdGateUiState> = _uiState.asStateFlow()

    val pendingInvites: StateFlow<List<HouseholdInvite>> = collaborationRepository
        .observePendingInvites()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        scope.launch {
            pendingInvites.collect { invites ->
                _uiState.value = _uiState.value.copy(pendingInvites = invites)
            }
        }
        refreshMembership()
    }

    fun refreshMembership() {
        scope.launch {
            _uiState.value = _uiState.value.copy(
                membershipStatus = HouseholdMembershipStatus.Loading,
                isCreating = false,
            )
            householdRepository.refreshMembership()
                .onSuccess { status ->
                    _uiState.value = _uiState.value.copy(membershipStatus = status)
                    activateNutritionSessionIfActive(status)
                    runCatching { collaborationRepository.refreshPendingInvites() }
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HouseholdGate",
                        message = "refresh_membership_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    _uiState.value = _uiState.value.copy(
                        membershipStatus = HouseholdMembershipStatus.Error(mapFailure(throwable)),
                    )
                }
        }
    }

    fun onHouseholdNameChange(name: String) {
        _uiState.value = _uiState.value.copy(householdNameInput = name)
    }

    fun createHousehold() {
        val name = _uiState.value.householdNameInput.trim()
        if (name.isEmpty() || _uiState.value.isCreating) return

        scope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true)
            householdRepository.createHousehold(name)
                .onSuccess { created ->
                    householdRepository.refreshMembership()
                        .onSuccess { status ->
                            _uiState.value = _uiState.value.copy(
                                membershipStatus = status,
                                isCreating = false,
                            )
                            activateNutritionSessionIfActive(status, fallbackHouseholdId = created.id)
                        }
                        .onFailure { throwable ->
                            _uiState.value = _uiState.value.copy(
                                membershipStatus = HouseholdMembershipStatus.Error(mapFailure(throwable)),
                                isCreating = false,
                            )
                        }
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HouseholdGate",
                        message = "create_household_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    _uiState.value = _uiState.value.copy(
                        membershipStatus = HouseholdMembershipStatus.Error(mapFailure(throwable)),
                        isCreating = false,
                    )
                }
        }
    }

    fun acceptInvite(inviteId: String) {
        val householdName = _uiState.value.pendingInvites
            .find { it.id == inviteId }
            ?.householdName
            .orEmpty()
        scope.launch {
            _uiState.value = _uiState.value.copy(inviteActionMessage = null)
            collaborationRepository.acceptInvite(inviteId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        inviteActionMessage = InviteActionMessage.Joined(householdName),
                    )
                    refreshMembership()
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HouseholdGate",
                        message = "accept_invite_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    _uiState.value = _uiState.value.copy(
                        inviteActionMessage = throwable.toInviteActionMessage(),
                    )
                }
        }
    }

    fun clearInviteActionMessage() {
        _uiState.value = _uiState.value.copy(inviteActionMessage = null)
    }

    fun declineInvite(inviteId: String) {
        scope.launch {
            collaborationRepository.declineInvite(inviteId)
            runCatching { collaborationRepository.refreshPendingInvites() }
        }
    }

    fun signOut() {
        scope.launch {
            sessionCoordinator.deactivate()
            authRepository.signOut()
        }
    }

    private fun mapFailure(throwable: Throwable): HouseholdGateError =
        when (throwable.message) {
            "supabase_not_configured" -> HouseholdGateError.NotConfigured
            "household_already_active" -> HouseholdGateError.AlreadyActive
            "household_required" -> HouseholdGateError.HouseholdRequired
            else -> HouseholdGateError.Generic
        }

    private suspend fun activateNutritionSessionIfActive(
        status: HouseholdMembershipStatus,
        fallbackHouseholdId: String? = null,
    ) {
        val householdId = when (status) {
            is HouseholdMembershipStatus.Active -> status.household.id
            else -> fallbackHouseholdId
        } ?: return
        runCatching { sessionCoordinator.activateHousehold(householdId) }
    }
}

private fun Throwable.toInviteActionMessage(): InviteActionMessage =
    when {
        CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_EMAIL_MISMATCH, message) ->
            InviteActionMessage.EmailMismatch
        else -> InviteActionMessage.AcceptFailed
    }
