package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.auth.resolvedDisplayName
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.presentation.screens.household.InviteActionMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getGreetingUseCase: GetGreetingUseCase,
    private val authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
    private val collaborationRepository: SpaceCollaborationRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _greeting = MutableStateFlow<Greeting?>(null)
    val greeting: StateFlow<Greeting?> = _greeting.asStateFlow()

    private val _household = MutableStateFlow<Household?>(null)
    val household: StateFlow<Household?> = _household.asStateFlow()

    val hasActiveHousehold: StateFlow<Boolean> = householdRepository
        .observeMembershipStatus()
        .map { it is HouseholdMembershipStatus.Active }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _inviteActionMessage = MutableStateFlow<InviteActionMessage?>(null)
    val inviteActionMessage: StateFlow<InviteActionMessage?> = _inviteActionMessage.asStateFlow()

    val pendingInvites: StateFlow<List<SpaceInvite>> = collaborationRepository
        .observePendingInvites()
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val userDisplayName: StateFlow<String?> = authRepository.authState
        .map(::displayNameForAuthState)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            displayNameForAuthState(authRepository.authState.value),
        )

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            try {
                _greeting.value = getGreetingUseCase()
            } catch (_: Throwable) {
                // Keep the last greeting when refresh fails.
            } finally {
                _isRefreshing.value = false
            }
        }
        refreshPendingInvitesInBackground()
        refreshHouseholdInBackground()
    }

    private fun refreshHouseholdInBackground() {
        scope.launch {
            householdRepository.refreshMembership()
                .onSuccess { status ->
                    if (status is HouseholdMembershipStatus.Active) {
                        _household.value = status.household
                    } else {
                        _household.value = null
                    }
                }
        }
    }

    private fun refreshPendingInvitesInBackground() {
        scope.launch {
            runCatching { collaborationRepository.refreshPendingInvites() }
        }
    }

    fun signOut() {
        scope.launch {
            sessionCoordinator.deactivate()
            authRepository.signOut()
        }
    }

    fun acceptInvite(inviteId: String) {
        scope.launch {
            _inviteActionMessage.value = null
            collaborationRepository.acceptInvite(inviteId)
                .onSuccess {
                    householdRepository.refreshMembership()
                        .onSuccess { status ->
                            if (status is HouseholdMembershipStatus.Active) {
                                _household.value = status.household
                            }
                        }
                }
                .onFailure {
                    _inviteActionMessage.value = InviteActionMessage.AcceptFailed
                }
        }
    }

    fun clearInviteActionMessage() {
        _inviteActionMessage.value = null
    }

    fun declineInvite(inviteId: String) {
        scope.launch {
            collaborationRepository.declineInvite(inviteId)
        }
    }

    private fun displayNameForAuthState(state: AuthState): String? =
        when (state) {
            is AuthState.Authenticated -> state.user.resolvedDisplayName()
            else -> null
        }
}
