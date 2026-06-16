package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SharingSpace
import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
import app.mymultiverse.kmp.domain.repository.SharingSpaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface NutritionSpacesError {
    data object Generic : NutritionSpacesError

    data object NameRequired : NutritionSpacesError

    data object FeaturesRequired : NutritionSpacesError

    data object NotConfigured : NutritionSpacesError
}

data class CreateNutritionSpaceDraft(
    val name: String = "",
    val groceryEnabled: Boolean = true,
    val mealPlanEnabled: Boolean = true,
    val aiAdviceEnabled: Boolean = true,
)

data class NutritionSpacesUiState(
    val spaces: List<SharingSpace> = emptyList(),
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val showCreateDialog: Boolean = false,
    val createDraft: CreateNutritionSpaceDraft = CreateNutritionSpaceDraft(),
    val loadError: NutritionSpacesError? = null,
    val createError: NutritionSpacesError? = null,
)

class NutritionSpacesScreenModel(
    private val sharingSpaceRepository: SharingSpaceRepository,
    private val selectionStore: NutritionSpaceSelectionStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(NutritionSpacesUiState())
    val uiState: StateFlow<NutritionSpacesUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            sharingSpaceRepository.observeNutritionSpaces().collect { spaces ->
                _uiState.update { it.copy(spaces = spaces) }
            }
        }
        refresh()
    }

    fun refresh() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }
            runCatching { sharingSpaceRepository.refreshNutritionSpaces() }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            loadError = mapFailure(throwable),
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { state -> state.copy(isLoading = false, loadError = null) }
                }
        }
    }

    fun openCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                createDraft = CreateNutritionSpaceDraft(),
                createError = null,
            )
        }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false, createError = null) }
    }

    fun onCreateNameChange(value: String) {
        _uiState.update { state ->
            state.copy(createDraft = state.createDraft.copy(name = value), createError = null)
        }
    }

    fun onCreateFeatureToggle(feature: NutritionSharingFeature, enabled: Boolean) {
        _uiState.update { state ->
            val draft = state.createDraft
            val updated = when (feature) {
                NutritionSharingFeature.Grocery -> draft.copy(groceryEnabled = enabled)
                NutritionSharingFeature.MealPlan -> draft.copy(mealPlanEnabled = enabled)
                NutritionSharingFeature.AiAdvice -> draft.copy(aiAdviceEnabled = enabled)
            }
            state.copy(createDraft = updated, createError = null)
        }
    }

    fun submitCreateSpace(onCreated: (SharingSpace) -> Unit) {
        val draft = _uiState.value.createDraft
        val features = draft.selectedFeatures()
        if (draft.name.isBlank()) {
            _uiState.update { it.copy(createError = NutritionSpacesError.NameRequired) }
            return
        }
        if (features.isEmpty()) {
            _uiState.update { it.copy(createError = NutritionSpacesError.FeaturesRequired) }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }
            val result = sharingSpaceRepository.createNutritionSpace(draft.name, features)
            _uiState.update { state ->
                state.copy(
                    isCreating = false,
                    showCreateDialog = !result.isSuccess,
                    createError = result.exceptionOrNull()?.let { mapFailure(it) },
                )
            }
            result.onSuccess { space ->
                selectionStore.setActiveSpaceId(space.id)
                onCreated(space)
            }
        }
    }

    fun selectSpace(space: SharingSpace, onSelected: (SharingSpace) -> Unit) {
        scope.launch {
            selectionStore.setActiveSpaceId(space.id)
            onSelected(space)
        }
    }

    private fun CreateNutritionSpaceDraft.selectedFeatures(): Set<NutritionSharingFeature> =
        buildSet {
            if (groceryEnabled) add(NutritionSharingFeature.Grocery)
            if (mealPlanEnabled) add(NutritionSharingFeature.MealPlan)
            if (aiAdviceEnabled) add(NutritionSharingFeature.AiAdvice)
        }

    private fun mapFailure(throwable: Throwable): NutritionSpacesError =
        when (throwable.message) {
            "space_name_required" -> NutritionSpacesError.NameRequired
            "space_features_required" -> NutritionSpacesError.FeaturesRequired
            "supabase_not_configured" -> NutritionSpacesError.NotConfigured
            else -> NutritionSpacesError.Generic
        }
}
