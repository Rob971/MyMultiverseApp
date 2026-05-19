package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.service.NutritionAdviceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class NutritionAiState {
    data object Idle : NutritionAiState()
    data object Loading : NutritionAiState()
    data class Answer(val text: String) : NutritionAiState()
    data class Error(val message: String) : NutritionAiState()
}

class NutritionScreenModel(
    private val repository: NutritionRepository,
    private val adviceService: NutritionAdviceService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val newItemId: () -> String = { "${Random.nextLong()}_${Random.nextInt()}" },
) {

    val weekKey: String = repository.weekKey

    val groceryItems: StateFlow<List<GroceryItem>> = repository.observeGroceryItems()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val mealPlan: StateFlow<WeeklyMealPlan> = repository.observeMealPlan()
        .stateIn(scope, SharingStarted.Eagerly, WeeklyMealPlan(weekKey = repository.weekKey))

    private val _aiState = MutableStateFlow<NutritionAiState>(NutritionAiState.Idle)
    val aiState: StateFlow<NutritionAiState> = _aiState.asStateFlow()

    fun addGroceryItem(label: String) {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return
        scope.launch {
            val updated = groceryItems.value + GroceryItem(
                id = newId(),
                label = trimmed,
            )
            repository.saveGroceryItems(updated)
        }
    }

    fun toggleGroceryItem(id: String) {
        scope.launch {
            val updated = groceryItems.value.map { item ->
                if (item.id == id) item.copy(isChecked = !item.isChecked) else item
            }
            repository.saveGroceryItems(updated)
        }
    }

    fun removeGroceryItem(id: String) {
        scope.launch {
            repository.saveGroceryItems(groceryItems.value.filterNot { it.id == id })
        }
    }

    fun updateMeal(dayIndex: Int, lunch: String? = null, dinner: String? = null) {
        if (dayIndex !in 0 until WeeklyMealPlan.DAYS_IN_WEEK) return
        scope.launch {
            val current = mealPlan.value
            val days = current.days.toMutableList()
            val existing = days[dayIndex]
            days[dayIndex] = existing.copy(
                lunch = lunch ?: existing.lunch,
                dinner = dinner ?: existing.dinner,
            )
            repository.saveMealPlan(current.copy(days = days))
        }
    }

    fun askNutritionAdvice(question: String) {
        scope.launch {
            _aiState.value = NutritionAiState.Loading
            adviceService.ask(question)
                .onSuccess { answer -> _aiState.value = NutritionAiState.Answer(answer) }
                .onFailure { error ->
                    _aiState.value = NutritionAiState.Error(
                        error.message ?: "unknown_error",
                    )
                }
        }
    }

    fun resetAiState() {
        _aiState.value = NutritionAiState.Idle
    }

    private fun newId(): String = newItemId()
}
