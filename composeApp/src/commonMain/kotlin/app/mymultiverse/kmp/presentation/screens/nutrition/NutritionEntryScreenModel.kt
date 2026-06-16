package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.sharing.orDefaultNutritionFeatures
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
import app.mymultiverse.kmp.presentation.navigation.NutritionSpaceContext
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

    data class Ready(val space: NutritionSpaceContext) : NutritionEntryState

    data class Error(val error: NutritionEntryError) : NutritionEntryState
}

class NutritionEntryScreenModel(
    private val householdRepository: HouseholdRepository,
    private val selectionStore: NutritionSpaceSelectionStore,
    private val sessionCoordinator: NutritionSessionCoordinator,
    private val logger: AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _state = MutableStateFlow<NutritionEntryState>(NutritionEntryState.Loading)
    val state: StateFlow<NutritionEntryState> = _state.asStateFlow()

    fun ensureHousehold() {
        scope.launch {
            _state.value = NutritionEntryState.Loading
            householdRepository.ensureHousehold()
                .onSuccess { household ->
                    val context = household.toSpaceContext()
                    selectionStore.setActiveSpaceId(context.id)
                    sessionCoordinator.activateSpace(context.id)
                    _state.value = NutritionEntryState.Ready(context)
                }
                .onFailure { throwable ->
                    logger.recordError(
                        tag = "NutritionEntry",
                        message = "ensure_household_failed",
                        throwable = throwable,
                    )
                    _state.value = NutritionEntryState.Error(mapFailure(throwable))
                }
        }
    }

    private fun Household.toSpaceContext(): NutritionSpaceContext =
        NutritionSpaceContext(
            id = id,
            name = name,
            ownerId = ownerId,
            ownerDisplayName = ownerDisplayName,
            features = nutritionFeatures.orDefaultNutritionFeatures(),
        )

    private fun mapFailure(throwable: Throwable): NutritionEntryError =
        when (throwable.message) {
            "supabase_not_configured" -> NutritionEntryError.NotConfigured
            else -> NutritionEntryError.Generic
        }
}
