package app.mymultiverse.ammo.presentation.screens.householdsetup

import app.mymultiverse.ammo.data.home.HomeFirstWinChecklistStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.auth.resolvedDisplayName
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.sharing.HouseholdGateError
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.sharing.HouseholdDefaultName
import app.mymultiverse.ammo.domain.sharing.HouseholdNameRules
import app.mymultiverse.ammo.presentation.registration.RegistrationData
import app.mymultiverse.ammo.presentation.screens.home.HouseholdNameAvailability
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HouseholdSetupUiState(
    val householdNameInput: String = "",
    val isCreating: Boolean = false,
    val nameAvailability: HouseholdNameAvailability = HouseholdNameAvailability.Unknown,
    val gateError: HouseholdGateError? = null,
) {
    val canCreate: Boolean =
        householdNameInput.isNotBlank() &&
            !isCreating &&
            nameAvailability == HouseholdNameAvailability.Available
}

class HouseholdSetupScreenModel(
    private val authRepository: AuthRepository,
    private val householdRepository: HouseholdRepository,
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val firstWinChecklistStore: HomeFirstWinChecklistStore,
    private val logger: AppLogger,
    private val registrationData: RegistrationData = RegistrationData(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(HouseholdSetupUiState())
    val uiState: StateFlow<HouseholdSetupUiState> = _uiState.asStateFlow()

    private val _createdHouseholdId = MutableStateFlow<String?>(null)
    val createdHouseholdId: StateFlow<String?> = _createdHouseholdId.asStateFlow()

    private var nameCheckJob: Job? = null

    val suggestedNamePart: String?
        get() {
            val auth = authRepository.authState.value as? AuthState.Authenticated ?: return null
            return HouseholdDefaultName.suggest(
                displayName = auth.user.resolvedDisplayName(),
                email = auth.user.email,
            ).takeIf { it.isNotBlank() }
        }

    fun applyDefaultHouseholdNameIfEmpty(formattedDefaultName: String) {
        if (_uiState.value.householdNameInput.isNotBlank()) return
        val pending = registrationData.pendingHouseholdName?.trim()
        if (!pending.isNullOrBlank()) {
            onHouseholdNameChange(pending)
            registrationData.clear()
            return
        }
        if (formattedDefaultName.isBlank()) return
        onHouseholdNameChange(formattedDefaultName)
    }

    fun onHouseholdNameChange(name: String) {
        nameCheckJob?.cancel()
        _uiState.update {
            it.copy(
                householdNameInput = name,
                nameAvailability = localNameAvailability(name),
                gateError = null,
            )
        }
        scheduleNameAvailabilityCheck(name)
    }

    fun createHousehold() {
        val name = _uiState.value.householdNameInput.trim()
        if (name.isEmpty() || !_uiState.value.canCreate) return
        performCreateHousehold(name)
    }

    fun consumeCreatedHouseholdId() {
        _createdHouseholdId.value = null
    }

    private fun performCreateHousehold(name: String) {
        scope.launch {
            _uiState.update { it.copy(isCreating = true, gateError = null) }
            householdRepository.createHousehold(name)
                .onSuccess { created ->
                    firstWinChecklistStore.clearDismissed(created.id)
                    householdRepository.refreshMembership()
                        .onSuccess { status ->
                            _uiState.update { it.copy(isCreating = false) }
                            if (status is HouseholdMembershipStatus.Active) {
                                activateNutritionSession(status.household.id)
                            } else {
                                activateNutritionSession(created.id)
                            }
                            runCatching { collaborationRepository.refreshPendingInvites() }
                            _createdHouseholdId.value = created.id
                        }
                        .onFailure { throwable ->
                            _uiState.update {
                                it.copy(
                                    isCreating = false,
                                    gateError = mapFailure(throwable),
                                )
                            }
                        }
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HouseholdSetup",
                        message = "create_household_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    if (CollaborationErrorCodes.messageContains(
                            CollaborationErrorCodes.HOUSEHOLD_NAME_TAKEN,
                            throwable.message,
                        )
                    ) {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                nameAvailability = HouseholdNameAvailability.Taken,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                gateError = mapFailure(throwable),
                            )
                        }
                    }
                }
        }
    }

    private fun scheduleNameAvailabilityCheck(name: String) {
        if (HouseholdNameRules.validationError(name) != null) {
            _uiState.update { it.copy(nameAvailability = HouseholdNameAvailability.Invalid) }
            return
        }

        val job = scope.launch {
            delay(NAME_CHECK_DEBOUNCE_MS)
            _uiState.update { it.copy(nameAvailability = HouseholdNameAvailability.Checking) }
            householdRepository.checkHouseholdNameAvailable(name, excludeHouseholdId = null)
                .onSuccess { available ->
                    _uiState.update {
                        it.copy(
                            nameAvailability = if (available) {
                                HouseholdNameAvailability.Available
                            } else {
                                HouseholdNameAvailability.Taken
                            },
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(nameAvailability = HouseholdNameAvailability.Unknown) }
                }
        }
        nameCheckJob = job
    }

    private fun localNameAvailability(name: String): HouseholdNameAvailability =
        if (HouseholdNameRules.validationError(name) != null) {
            HouseholdNameAvailability.Invalid
        } else {
            HouseholdNameAvailability.Unknown
        }

    private suspend fun activateNutritionSession(householdId: String) {
        try {
            sessionCoordinator.activateHousehold(householdId)
        } catch (e: CancellationException) {
            throw e
        } catch (throwable: Throwable) {
            logger.recordError(
                tag = "HouseholdSetup",
                message = "activate_nutrition_session_failed: ${throwable.message}",
                throwable = throwable,
            )
            // Best-effort retry after a short delay — transient failures self-heal on next bind.
            scope.launch {
                delay(3_000L)
                try { sessionCoordinator.activateHousehold(householdId) } catch (_: Throwable) { }
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

    private companion object {
        const val NAME_CHECK_DEBOUNCE_MS = 400L
    }
}
