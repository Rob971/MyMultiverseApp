package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.auth.resolvedDisplayName
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.kmp.domain.platform.PersonalDataExporter
import app.mymultiverse.kmp.domain.platform.PushNotificationRegistrar
import app.mymultiverse.kmp.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.kmp.presentation.screens.household.InviteActionMessage
import app.mymultiverse.kmp.presentation.screens.household.SwitchHouseholdPrompt
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
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val personalDataExporter: PersonalDataExporter,
    private val pushNotificationRegistrar: PushNotificationRegistrar,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _greeting = MutableStateFlow<Greeting?>(null)
    val greeting: StateFlow<Greeting?> = _greeting.asStateFlow()

    private val _household = MutableStateFlow<Household?>(null)
    val household: StateFlow<Household?> = _household.asStateFlow()

    private var latestMembershipStatus: HouseholdMembershipStatus = HouseholdMembershipStatus.Loading

    val hasActiveHousehold: StateFlow<Boolean> = householdRepository
        .observeMembershipStatus()
        .map { it is HouseholdMembershipStatus.Active }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), false)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _inviteActionMessage = MutableStateFlow<InviteActionMessage?>(null)
    val inviteActionMessage: StateFlow<InviteActionMessage?> = _inviteActionMessage.asStateFlow()

    private val _switchHouseholdPrompt = MutableStateFlow<SwitchHouseholdPrompt?>(null)
    val switchHouseholdPrompt: StateFlow<SwitchHouseholdPrompt?> = _switchHouseholdPrompt.asStateFlow()

    val pendingInvites: StateFlow<List<HouseholdInvite>> = collaborationRepository
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
        scope.launch {
            householdRepository.observeMembershipStatus().collect { status ->
                latestMembershipStatus = status
                if (status is HouseholdMembershipStatus.Active) {
                    _household.value = status.household
                } else if (status == HouseholdMembershipStatus.None) {
                    _household.value = null
                }
            }
        }
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
        registerPushNotificationsIfAuthenticated()
    }

    private fun registerPushNotificationsIfAuthenticated() {
        scope.launch {
            if (authRepository.authState.value is AuthState.Authenticated) {
                runCatching { pushNotificationRegistrar.registerCurrentDeviceToken() }
            }
        }
    }

    private fun refreshHouseholdInBackground() {
        scope.launch {
            householdRepository.refreshMembership()
                .onSuccess { status ->
                    if (status is HouseholdMembershipStatus.Active) {
                        _household.value = status.household
                        activateNutritionSession(status.household.id)
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

    private val _personalDataExportMessage = MutableStateFlow<PersonalDataExportMessage?>(null)
    val personalDataExportMessage: StateFlow<PersonalDataExportMessage?> = _personalDataExportMessage.asStateFlow()

    fun exportPersonalData() {
        scope.launch {
            authRepository.exportPersonalData()
                .onSuccess { json ->
                    val shared = personalDataExporter.shareJson("mymultiverse-personal-data.json", json)
                    _personalDataExportMessage.value = when {
                        shared -> PersonalDataExportMessage.Success
                        else -> PersonalDataExportMessage.ShareUnavailable
                    }
                }
                .onFailure { _personalDataExportMessage.value = PersonalDataExportMessage.Error }
        }
    }

    private val _deleteAccountMessage = MutableStateFlow<DeleteAccountMessage?>(null)
    val deleteAccountMessage: StateFlow<DeleteAccountMessage?> = _deleteAccountMessage.asStateFlow()

    private val _showDeleteAccountDialog = MutableStateFlow(false)
    val showDeleteAccountDialog: StateFlow<Boolean> = _showDeleteAccountDialog.asStateFlow()

    fun requestDeleteAccount() {
        _showDeleteAccountDialog.value = true
    }

    fun dismissDeleteAccountDialog() {
        _showDeleteAccountDialog.value = false
    }

    fun confirmDeleteAccount() {
        _showDeleteAccountDialog.value = false
        scope.launch {
            sessionCoordinator.deactivate()
            authRepository.deleteAccount()
                .onSuccess { _deleteAccountMessage.value = DeleteAccountMessage.Success }
                .onFailure { throwable ->
                    _deleteAccountMessage.value = when (throwable.message) {
                        CollaborationErrorCodes.OWNER_MUST_TRANSFER_OR_DISSOLVE ->
                            DeleteAccountMessage.OwnerMustTransfer
                        else -> DeleteAccountMessage.Error
                    }
                }
        }
    }

    fun clearDeleteAccountMessage() {
        _deleteAccountMessage.value = null
    }

    fun clearPersonalDataExportMessage() {
        _personalDataExportMessage.value = null
    }

    fun onAcceptInviteClicked(invite: HouseholdInvite) {
        if (latestMembershipStatus is HouseholdMembershipStatus.Active) {
            val currentName = (latestMembershipStatus as HouseholdMembershipStatus.Active).household.name
            _switchHouseholdPrompt.value = SwitchHouseholdPrompt(
                inviteId = invite.id,
                invitedHouseholdName = invite.householdName,
                currentHouseholdName = currentName,
            )
        } else {
            acceptInvite(invite.id, invite.householdName)
        }
    }

    fun dismissSwitchHouseholdPrompt() {
        _switchHouseholdPrompt.value = null
    }

    fun confirmLeaveAndAccept() {
        val prompt = _switchHouseholdPrompt.value ?: return
        _switchHouseholdPrompt.value = null
        scope.launch {
            _inviteActionMessage.value = null
            exitCurrentHousehold()
                .onSuccess { acceptInvite(prompt.inviteId, prompt.invitedHouseholdName) }
                .onFailure {
                    _inviteActionMessage.value = InviteActionMessage.AcceptFailed
                }
        }
    }

    fun acceptInvite(inviteId: String, householdName: String) {
        scope.launch {
            _inviteActionMessage.value = null
            collaborationRepository.acceptInvite(inviteId)
                .onSuccess {
                    householdRepository.refreshMembership()
                        .onSuccess { status ->
                            if (status is HouseholdMembershipStatus.Active) {
                                _household.value = status.household
                                activateNutritionSession(status.household.id)
                            }
                            runCatching { collaborationRepository.refreshPendingInvites() }
                        }
                    _inviteActionMessage.value = InviteActionMessage.Joined(householdName)
                }
                .onFailure { throwable ->
                    _inviteActionMessage.value = throwable.toInviteActionMessage()
                }
        }
    }

    fun clearInviteActionMessage() {
        _inviteActionMessage.value = null
    }

    fun declineInvite(inviteId: String) {
        scope.launch {
            collaborationRepository.declineInvite(inviteId)
            runCatching { collaborationRepository.refreshPendingInvites() }
        }
    }

    private suspend fun exitCurrentHousehold(): Result<Unit> {
        val status = householdRepository.refreshMembership().getOrNull()
        if (status !is HouseholdMembershipStatus.Active) return Result.success(Unit)
        sessionCoordinator.deactivate()
        return when (status.role) {
            HouseholdMemberRole.Owner -> householdRepository.dissolveHousehold()
            else -> householdRepository.leaveHousehold()
        }
    }

    private suspend fun activateNutritionSession(householdId: String) {
        runCatching { sessionCoordinator.activateHousehold(householdId) }
    }

    private fun displayNameForAuthState(state: AuthState): String? =
        when (state) {
            is AuthState.Authenticated -> state.user.resolvedDisplayName()
            else -> null
        }
}

private fun Throwable.toInviteActionMessage(): InviteActionMessage =
    when {
        CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_EMAIL_MISMATCH, message) ->
            InviteActionMessage.EmailMismatch
        else -> InviteActionMessage.AcceptFailed
    }
