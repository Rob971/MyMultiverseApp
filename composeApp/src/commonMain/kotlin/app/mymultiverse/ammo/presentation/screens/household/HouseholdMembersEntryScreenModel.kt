package app.mymultiverse.ammo.presentation.screens.household

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext
import app.mymultiverse.ammo.presentation.navigation.toNavigationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HouseholdMembersEntryError {
    data object Generic : HouseholdMembersEntryError
    data object NotConfigured : HouseholdMembersEntryError
}

sealed interface HouseholdMembersEntryState {
    data object Loading : HouseholdMembersEntryState
    data class Ready(val household: HouseholdContext) : HouseholdMembersEntryState
    data class Error(val error: HouseholdMembersEntryError) : HouseholdMembersEntryState
}

class HouseholdMembersEntryScreenModel(
    private val householdRepository: HouseholdRepository,
    private val logger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _state = MutableStateFlow<HouseholdMembersEntryState>(HouseholdMembersEntryState.Loading)
    val state: StateFlow<HouseholdMembersEntryState> = _state.asStateFlow()

    fun ensureHousehold() {
        scope.launch {
            _state.value = HouseholdMembersEntryState.Loading
            householdRepository.ensureHousehold()
                .onSuccess { household ->
                    _state.value = HouseholdMembersEntryState.Ready(household.toNavigationContext())
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "HouseholdMembersEntry",
                        message = "ensure_household_failed: ${throwable.message}",
                        throwable = throwable,
                    )
                    _state.value = HouseholdMembersEntryState.Error(mapFailure(throwable))
                }
        }
    }

    private fun mapFailure(throwable: Throwable): HouseholdMembersEntryError =
        when (throwable.message) {
            "supabase_not_configured" -> HouseholdMembersEntryError.NotConfigured
            else -> HouseholdMembersEntryError.Generic
        }
}
