package app.mymultiverse.ammo.presentation.screens.nutrition

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.NutritionHouseholdSelectionStore
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext
import app.mymultiverse.ammo.presentation.navigation.toNavigationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NutritionEntryError {
    data object Generic : NutritionEntryError
    data object NotConfigured : NutritionEntryError
}

sealed interface NutritionEntryState {
    data object Loading : NutritionEntryState

    data class Ready(val household: HouseholdContext) : NutritionEntryState

    data class Error(val error: NutritionEntryError) : NutritionEntryState
}

class NutritionEntryScreenModel(
    private val householdRepository: HouseholdRepository,
    private val selectionStore: NutritionHouseholdSelectionStore,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val logger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _state = MutableStateFlow<NutritionEntryState>(NutritionEntryState.Loading)
    val state: StateFlow<NutritionEntryState> = _state.asStateFlow()

    fun ensureHousehold() {
        scope.launch {
            _state.value = NutritionEntryState.Loading
            try {
                val household = householdRepository.ensureHousehold().getOrElse { throw it }
                val context = household.toNavigationContext()
                selectionStore.setActiveHouseholdId(context.id)
                sessionCoordinator.activateHousehold(context.id)
                _state.value = NutritionEntryState.Ready(context)
            } catch (e: CancellationException) {
                // Coroutine cancelled — leave state as Loading so next call retries cleanly.
                _state.value = NutritionEntryState.Loading
                throw e
            } catch (throwable: Throwable) {
                logger.recordError(
                    tag = "NutritionEntry",
                    message = "ensure_household_failed: ${throwable.message}",
                    throwable = throwable,
                )
                _state.value = NutritionEntryState.Error(mapFailure(throwable))
            }
        }
    }

    private fun mapFailure(throwable: Throwable): NutritionEntryError =
        when (throwable.message) {
            "supabase_not_configured" -> NutritionEntryError.NotConfigured
            else -> NutritionEntryError.Generic
        }
}
