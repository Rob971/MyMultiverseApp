package app.mymultiverse.ammo.presentation.screens.home

import app.mymultiverse.ammo.data.home.HomeFirstWinChecklistStore
import app.mymultiverse.ammo.data.home.HomeWeekPlanNudgeStore
import app.mymultiverse.ammo.domain.home.HomeFirstWinChecklist
import app.mymultiverse.ammo.domain.home.HomeWeekPlanNudge
import app.mymultiverse.ammo.domain.home.HomeTonightDinner
import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.domain.nutrition.NutritionHubSummary
import app.mymultiverse.ammo.domain.usecase.GetGreetingUseCase
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.auth.resolvedDisplayName
import app.mymultiverse.ammo.domain.model.sharing.Household
import app.mymultiverse.ammo.domain.model.sharing.HouseholdGateError
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.platform.PersonalDataExporter
import app.mymultiverse.ammo.domain.platform.PushNotificationRegistrar
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.sharing.HouseholdDefaultName
import app.mymultiverse.ammo.domain.sharing.HouseholdNameRules
import app.mymultiverse.ammo.domain.sharing.canRenameHousehold
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext
import app.mymultiverse.ammo.presentation.navigation.toNavigationContext
import app.mymultiverse.ammo.presentation.screens.household.InviteActionMessage
import app.mymultiverse.ammo.presentation.screens.household.SwitchHouseholdPrompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class HomeScreenModel(
    private val getGreetingUseCase: GetGreetingUseCase,
    private val authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val personalDataExporter: PersonalDataExporter,
    private val pushNotificationRegistrar: PushNotificationRegistrar,
    private val firstWinChecklistStore: HomeFirstWinChecklistStore,
    private val weekPlanNudgeStore: HomeWeekPlanNudgeStore,
    private val logger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _greeting = MutableStateFlow<Greeting?>(null)
    val greeting: StateFlow<Greeting?> = _greeting.asStateFlow()

    private val _household = MutableStateFlow<Household?>(null)
    val household: StateFlow<Household?> = _household.asStateFlow()

    private var latestMembershipStatus: HouseholdMembershipStatus = HouseholdMembershipStatus.Loading

    private val _membershipStatusOverride = MutableStateFlow<HouseholdMembershipStatus?>(null)

    val homePhase: StateFlow<HomePhase> = combine(
        householdRepository.observeMembershipStatus(),
        _membershipStatusOverride,
    ) { status, override ->
        (override ?: status).toHomePhase()
    }.stateIn(scope, SharingStarted.Eagerly, HomePhase.Loading)

    val hasActiveHousehold: StateFlow<Boolean> = householdRepository
        .observeMembershipStatus()
        .map { it is HouseholdMembershipStatus.Active }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), false)

    /** Household for nutrition tabs — uses membership when home cache is still loading. */
    val nutritionHouseholdContext: StateFlow<HouseholdContext?> = combine(
        householdRepository.observeMembershipStatus(),
        _household,
    ) { status, cached ->
        val household = cached ?: (status as? HouseholdMembershipStatus.Active)?.household
        household?.toNavigationContext()
    }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _onboardingUiState = MutableStateFlow(HomeOnboardingUiState())
    val onboardingUiState: StateFlow<HomeOnboardingUiState> = _onboardingUiState.asStateFlow()

    private val _renameUiState = MutableStateFlow(HomeRenameUiState())
    val renameUiState: StateFlow<HomeRenameUiState> = _renameUiState.asStateFlow()

    private var nameCheckJob: Job? = null
    private var renameNameCheckJob: Job? = null

    val activeMemberRole: StateFlow<HouseholdMemberRole?> = householdRepository
        .observeMembershipStatus()
        .map { status -> (status as? HouseholdMembershipStatus.Active)?.role }
        .stateIn(scope, SharingStarted.Eagerly, null)

    val canRenameHousehold: StateFlow<Boolean> = activeMemberRole
        .map { role -> role?.canRenameHousehold() == true }
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _inviteActionMessage = MutableStateFlow<InviteActionMessage?>(null)
    val inviteActionMessage: StateFlow<InviteActionMessage?> = _inviteActionMessage.asStateFlow()

    private val _postCreateInvitePrompt = MutableStateFlow<PostCreateInvitePrompt?>(null)
    val postCreateInvitePrompt: StateFlow<PostCreateInvitePrompt?> = _postCreateInvitePrompt.asStateFlow()

    private val _postCreateFocus = MutableStateFlow<PostCreateFocusTarget?>(null)
    val postCreateFocus: StateFlow<PostCreateFocusTarget?> = _postCreateFocus.asStateFlow()

    fun consumePostCreateFocus() {
        _postCreateFocus.value = null
    }

    private val _switchHouseholdPrompt = MutableStateFlow<SwitchHouseholdPrompt?>(null)
    val switchHouseholdPrompt: StateFlow<SwitchHouseholdPrompt?> = _switchHouseholdPrompt.asStateFlow()

    val pendingInvites: StateFlow<List<HouseholdInvite>> = collaborationRepository
        .observePendingInvites()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val nutritionSummary: StateFlow<HomeNutritionSummary?> = sessionCoordinator.nutrition
        .flatMapLatest { repository ->
            combine(
                repository.observeGroceryItems(),
                repository.observeMealPlan(),
            ) { groceryItems, mealPlan ->
                HomeNutritionSummary(
                    weekKey = repository.weekKey,
                    groceryProgress = NutritionHubSummary.groceryProgress(groceryItems),
                    plannedMealSlots = NutritionHubSummary.plannedSlotsCount(mealPlan.days),
                    tonightsDinner = HomeTonightDinner.resolve(mealPlan, weekKey = repository.weekKey),
                )
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val _firstWinDismissed = MutableStateFlow(false)
    private val _weekPlanNudgeDismissedWeekKey = MutableStateFlow<String?>(null)

    private data class HomeCollaborationSnapshot(
        val members: List<HouseholdMember> = emptyList(),
        val outboundInviteCount: Int = 0,
    )

    private val collaborationSnapshot: StateFlow<HomeCollaborationSnapshot> = _household
        .flatMapLatest { household ->
            if (household == null) {
                flowOf(HomeCollaborationSnapshot())
            } else {
                combine(
                    collaborationRepository.observeMembers(household.id),
                    collaborationRepository.observeOutboundInvites(household.id),
                ) { members, outboundInvites ->
                    HomeCollaborationSnapshot(
                        members = members,
                        outboundInviteCount = outboundInvites.size,
                    )
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), HomeCollaborationSnapshot())

    val firstWinChecklist: StateFlow<HomeFirstWinChecklistUiState> = combine(
        hasActiveHousehold,
        _firstWinDismissed,
        collaborationSnapshot,
        nutritionSummary,
    ) { activeHousehold, dismissed, collaboration, nutrition ->
        val inviteComplete = HomeFirstWinChecklist.inviteStepComplete(
            members = collaboration.members,
            outboundInviteCount = collaboration.outboundInviteCount,
        )
        val groceryCount = nutrition?.groceryProgress?.total ?: 0
        val nutritionComplete = HomeFirstWinChecklist.nutritionStepComplete(
            plannedMealSlots = nutrition?.plannedMealSlots ?: 0,
            groceryItemCount = groceryCount,
        )
        HomeFirstWinChecklistUiState(
            visible = HomeFirstWinChecklist.shouldShow(
                hasActiveHousehold = activeHousehold,
                dismissed = dismissed,
                inviteComplete = inviteComplete,
                nutritionComplete = nutritionComplete,
            ),
            inviteComplete = inviteComplete,
            nutritionComplete = nutritionComplete,
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), HomeFirstWinChecklistUiState())

    val weekPlanNudge: StateFlow<HomeWeekPlanNudgeUiState> = combine(
        hasActiveHousehold,
        _weekPlanNudgeDismissedWeekKey,
        nutritionSummary,
    ) { activeHousehold, dismissedWeekKey, nutrition ->
        HomeWeekPlanNudgeUiState(
            visible = HomeWeekPlanNudge.shouldShow(
                hasActiveHousehold = activeHousehold,
                plannedMealSlots = nutrition?.plannedMealSlots ?: 0,
                weekKey = nutrition?.weekKey.orEmpty(),
                today = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                dismissedWeekKey = dismissedWeekKey,
            ),
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), HomeWeekPlanNudgeUiState())

    val userDisplayName: StateFlow<String?> = authRepository.authState
        .map(::displayNameForAuthState)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            displayNameForAuthState(authRepository.authState.value),
        )

    val currentUserAvatarUrl: StateFlow<String?> = combine(
        authRepository.authState,
        collaborationSnapshot,
    ) { authState, snapshot ->
        val userId = (authState as? AuthState.Authenticated)?.user?.id ?: return@combine null
        snapshot.members
            .firstOrNull { it.kind != HouseholdMemberKind.Dependant && it.referenceId == userId }
            ?.avatarUrl
    }.stateIn(scope, SharingStarted.Eagerly, null)

    init {
        scope.launch {
            householdRepository.observeMembershipStatus().collect { status ->
                latestMembershipStatus = status
                if (status is HouseholdMembershipStatus.Active) {
                    _household.value = status.household
                    _membershipStatusOverride.value = null
                    syncFirstWinDismissed(status.household.id)
                    syncWeekPlanNudgeDismissed(status.household.id)
                    refreshCollaborationSnapshot(status.household)
                } else if (status == HouseholdMembershipStatus.None) {
                    _household.value = null
                    _firstWinDismissed.value = false
                    maybePrefillDefaultHouseholdName()
                }
            }
        }
        refreshMembership()
        refresh()
        scope.launch {
            firstWinChecklist.collect { checklist ->
                val householdId = _household.value?.id ?: return@collect
                if (checklist.inviteComplete && checklist.nutritionComplete) {
                    firstWinChecklistStore.setDismissed(householdId)
                    _firstWinDismissed.value = true
                }
            }
        }
    }

    fun refresh() {
        scope.launch {
            _isRefreshing.value = true
            try {
                _greeting.value = getGreetingUseCase()
                runCatching { sessionCoordinator.nutrition.value.refreshFromRemote() }
            } catch (_: Throwable) {
                // Keep the last greeting when refresh fails.
            } finally {
                _isRefreshing.value = false
            }
        }
        refreshPendingInvitesInBackground()
        registerPushNotificationsIfAuthenticated()
        _household.value?.let { refreshCollaborationSnapshot(it) }
    }

    fun dismissFirstWinChecklist() {
        val householdId = _household.value?.id ?: return
        firstWinChecklistStore.setDismissed(householdId)
        _firstWinDismissed.value = true
    }

    fun dismissWeekPlanNudge() {
        val householdId = _household.value?.id ?: return
        val weekKey = nutritionSummary.value?.weekKey ?: return
        weekPlanNudgeStore.setDismissed(householdId, weekKey)
        _weekPlanNudgeDismissedWeekKey.value = weekKey
    }

    fun refreshMembership(forceLoadingState: Boolean = false) {
        scope.launch {
            if (forceLoadingState) {
                _membershipStatusOverride.value = HouseholdMembershipStatus.Loading
            }
            _onboardingUiState.value = _onboardingUiState.value.copy(isCreating = false)
            householdRepository.refreshMembership()
                .onSuccess { status ->
                    _membershipStatusOverride.value = null
                    latestMembershipStatus = status
                    if (status is HouseholdMembershipStatus.Active) {
                        _household.value = status.household
                        activateNutritionSession(status.household.id)
                    } else if (status == HouseholdMembershipStatus.None) {
                        _household.value = null
                        maybePrefillDefaultHouseholdName()
                    }
                    runCatching { collaborationRepository.refreshPendingInvites() }
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HomeScreen",
                        message = "refresh_membership_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    _membershipStatusOverride.value = HouseholdMembershipStatus.Error(
                        mapFailure(throwable),
                    )
                }
        }
    }

    fun onHouseholdNameChange(name: String) {
        nameCheckJob?.cancel()
        _onboardingUiState.value = _onboardingUiState.value.copy(
            householdNameInput = name,
            nameAvailability = localNameAvailability(name),
        )
        scheduleNameAvailabilityCheck(
            name = name,
            excludeHouseholdId = null,
            onChecking = { availability ->
                _onboardingUiState.value = _onboardingUiState.value.copy(nameAvailability = availability)
            },
            onResult = { availability ->
                _onboardingUiState.value = _onboardingUiState.value.copy(nameAvailability = availability)
            },
        ) { job -> nameCheckJob = job }
    }

    fun openRenameHouseholdDialog() {
        val currentName = _household.value?.name.orEmpty()
        _renameUiState.value = HomeRenameUiState(
            isVisible = true,
            nameInput = currentName,
            nameAvailability = if (currentName.isBlank()) {
                HouseholdNameAvailability.Invalid
            } else {
                HouseholdNameAvailability.Available
            },
        )
    }

    fun dismissRenameHouseholdDialog() {
        renameNameCheckJob?.cancel()
        _renameUiState.value = HomeRenameUiState()
    }

    fun onRenameHouseholdNameChange(name: String) {
        renameNameCheckJob?.cancel()
        val householdId = _household.value?.id
        _renameUiState.value = _renameUiState.value.copy(
            nameInput = name,
            nameAvailability = localNameAvailability(name),
        )
        scheduleNameAvailabilityCheck(
            name = name,
            excludeHouseholdId = householdId,
            onChecking = { availability ->
                _renameUiState.value = _renameUiState.value.copy(nameAvailability = availability)
            },
            onResult = { availability ->
                _renameUiState.value = _renameUiState.value.copy(nameAvailability = availability)
            },
        ) { job -> renameNameCheckJob = job }
    }

    fun confirmRenameHousehold() {
        val name = _renameUiState.value.nameInput.trim()
        if (name.isEmpty() ||
            _renameUiState.value.isSaving ||
            _renameUiState.value.nameAvailability != HouseholdNameAvailability.Available
        ) {
            return
        }

        scope.launch {
            _renameUiState.value = _renameUiState.value.copy(isSaving = true)
            householdRepository.renameHousehold(name)
                .onSuccess { updated ->
                    _household.value = updated
                    dismissRenameHouseholdDialog()
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HomeScreen",
                        message = "rename_household_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    if (CollaborationErrorCodes.messageContains(
                            CollaborationErrorCodes.HOUSEHOLD_NAME_TAKEN,
                            throwable.message,
                        )
                    ) {
                        _renameUiState.value = _renameUiState.value.copy(
                            isSaving = false,
                            nameAvailability = HouseholdNameAvailability.Taken,
                        )
                    } else {
                        _renameUiState.value = _renameUiState.value.copy(isSaving = false)
                    }
                }
        }
    }

    fun createHousehold() {
        val name = _onboardingUiState.value.householdNameInput.trim()
        if (name.isEmpty() ||
            _onboardingUiState.value.isCreating ||
            _onboardingUiState.value.nameAvailability != HouseholdNameAvailability.Available
        ) {
            return
        }
        performCreateHousehold(name)
    }

    fun quickCreateHousehold() {
        if (_onboardingUiState.value.isCreating) return
        val name = _onboardingUiState.value.householdNameInput.trim()
            .ifBlank { suggestedDefaultHouseholdName() }
            ?: return
        if (HouseholdNameRules.validationError(name) != null) return
        performCreateHousehold(name)
    }

    fun suggestedDefaultHouseholdName(): String? {
        val auth = authRepository.authState.value as? AuthState.Authenticated ?: return null
        return HouseholdDefaultName.suggest(
            displayName = auth.user.resolvedDisplayName(),
            email = auth.user.email,
        ).takeIf { it.isNotBlank() }
    }

    private fun performCreateHousehold(name: String) {
        scope.launch {
            _onboardingUiState.value = _onboardingUiState.value.copy(isCreating = true)
            householdRepository.createHousehold(name)
                .onSuccess { created ->
                    firstWinChecklistStore.clearDismissed(created.id)
                    _firstWinDismissed.value = false
                    householdRepository.refreshMembership()
                        .onSuccess { status ->
                            _membershipStatusOverride.value = null
                            latestMembershipStatus = status
                            _onboardingUiState.value = _onboardingUiState.value.copy(isCreating = false)
                            if (status is HouseholdMembershipStatus.Active) {
                                _household.value = status.household
                                refreshCollaborationSnapshot(status.household)
                            }
                            activateNutritionSession(
                                householdId = (status as? HouseholdMembershipStatus.Active)?.household?.id
                                    ?: created.id,
                            )
                            _postCreateInvitePrompt.value = PostCreateInvitePrompt(created.name)
                            _postCreateFocus.value = PostCreateFocusTarget.Grocery
                            runCatching { collaborationRepository.refreshPendingInvites() }
                        }
                        .onFailure { throwable ->
                            _membershipStatusOverride.value = HouseholdMembershipStatus.Error(
                                mapFailure(throwable),
                            )
                            _onboardingUiState.value = _onboardingUiState.value.copy(isCreating = false)
                        }
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HomeScreen",
                        message = "create_household_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    if (CollaborationErrorCodes.messageContains(
                            CollaborationErrorCodes.HOUSEHOLD_NAME_TAKEN,
                            throwable.message,
                        )
                    ) {
                        _onboardingUiState.value = _onboardingUiState.value.copy(
                            isCreating = false,
                            nameAvailability = HouseholdNameAvailability.Taken,
                        )
                    } else {
                        _membershipStatusOverride.value = HouseholdMembershipStatus.Error(
                            mapFailure(throwable),
                        )
                        _onboardingUiState.value = _onboardingUiState.value.copy(isCreating = false)
                    }
                }
        }
    }

    private fun localNameAvailability(name: String): HouseholdNameAvailability =
        if (HouseholdNameRules.validationError(name) != null) {
            HouseholdNameAvailability.Invalid
        } else {
            HouseholdNameAvailability.Unknown
        }

    private fun scheduleNameAvailabilityCheck(
        name: String,
        excludeHouseholdId: String?,
        onChecking: (HouseholdNameAvailability) -> Unit,
        onResult: (HouseholdNameAvailability) -> Unit,
        assignJob: (Job) -> Unit,
    ) {
        if (HouseholdNameRules.validationError(name) != null) {
            onResult(HouseholdNameAvailability.Invalid)
            return
        }

        val job = scope.launch {
            delay(NAME_CHECK_DEBOUNCE_MS)
            onChecking(HouseholdNameAvailability.Checking)
            householdRepository.checkHouseholdNameAvailable(name, excludeHouseholdId)
                .onSuccess { available ->
                    onResult(
                        if (available) HouseholdNameAvailability.Available else HouseholdNameAvailability.Taken,
                    )
                }
                .onFailure {
                    onResult(HouseholdNameAvailability.Unknown)
                }
        }
        assignJob(job)
    }

    private companion object {
        const val NAME_CHECK_DEBOUNCE_MS = 400L
    }

    private fun registerPushNotificationsIfAuthenticated() {
        scope.launch {
            if (authRepository.authState.value is AuthState.Authenticated) {
                runCatching { pushNotificationRegistrar.registerCurrentDeviceToken() }
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
                            _membershipStatusOverride.value = null
                            latestMembershipStatus = status
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

    fun clearPostCreateInvitePrompt() {
        _postCreateInvitePrompt.value = null
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
        return if (status.role == HouseholdMemberRole.Owner) {
            householdRepository.dissolveHousehold()
        } else {
            householdRepository.leaveHousehold()
        }
    }

    private suspend fun activateNutritionSession(householdId: String) {
        runCatching { sessionCoordinator.activateHousehold(householdId) }
    }

    private fun syncFirstWinDismissed(householdId: String) {
        _firstWinDismissed.value = firstWinChecklistStore.isDismissed(householdId)
    }

    private fun syncWeekPlanNudgeDismissed(householdId: String) {
        _weekPlanNudgeDismissedWeekKey.value = weekPlanNudgeStore.dismissedWeekKey(householdId)
    }

    private fun refreshCollaborationSnapshot(household: Household) {
        scope.launch {
            runCatching {
                collaborationRepository.refreshMembers(
                    householdId = household.id,
                    ownerId = household.ownerId,
                    ownerDisplayName = household.ownerDisplayName.orEmpty(),
                )
                collaborationRepository.refreshOutboundInvites(household.id)
            }
        }
    }

    private fun mapFailure(throwable: Throwable): HouseholdGateError =
        when (throwable.message) {
            "supabase_not_configured" -> HouseholdGateError.NotConfigured
            "household_already_active" -> HouseholdGateError.AlreadyActive
            "household_required" -> HouseholdGateError.HouseholdRequired
            else -> HouseholdGateError.Generic
        }

    private fun maybePrefillDefaultHouseholdName() {
        if (_onboardingUiState.value.householdNameInput.isNotBlank()) return
        val auth = authRepository.authState.value as? AuthState.Authenticated ?: return
        val suggested = HouseholdDefaultName.suggest(
            displayName = auth.user.resolvedDisplayName(),
            email = auth.user.email,
        )
        if (suggested.isNotBlank()) {
            onHouseholdNameChange(suggested)
        }
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
