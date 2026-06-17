package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.GroceryListPresentation
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
import app.mymultiverse.kmp.domain.sharing.canWriteHouseholdData
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
    data class Advice(val text: String) : NutritionAiState()
    data class GroceryList(val itemCount: Int) : NutritionAiState()
    data class MealPlanPreview(
        val plan: WeeklyMealPlan,
        val summary: String,
        val scope: MealPlanGenerationScope,
    ) : NutritionAiState()
    data class Error(val message: String) : NutritionAiState()
}

class NutritionScreenModel(
    private val session: NutritionSessionCoordinator,
    private val householdRepository: HouseholdRepository,
    private val aiAssistant: NutritionAiAssistantService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val newItemId: () -> String = { "${Random.nextLong()}_${Random.nextInt()}" },
) {
    private val repository: NutritionRepository
        get() = session.nutrition.value

    val weekKey: String
        get() = repository.weekKey

    val groceryItems: StateFlow<List<GroceryItem>> = session.nutrition
        .flatMapLatest { it.observeGroceryItems() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val aiGroceryItems: StateFlow<List<GroceryItem>> = session.nutrition
        .flatMapLatest { it.observeAiGroceryItems() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val mealPlan: StateFlow<WeeklyMealPlan> = session.nutrition
        .flatMapLatest { it.observeMealPlan() }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            WeeklyMealPlan(weekKey = WeekCalendar.currentWeekKey()),
        )

    val syncStatus: StateFlow<NutritionSyncStatus> = session.observeSyncStatus()
        .stateIn(scope, SharingStarted.Eagerly, NutritionSyncStatus.Idle)

    val canWriteHouseholdData: StateFlow<Boolean> = householdRepository
        .observeMembershipStatus()
        .map { status ->
            when (status) {
                is HouseholdMembershipStatus.Active -> status.role.canWriteHouseholdData()
                else -> true
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, true)

    suspend fun activateSpace(spaceId: String) {
        session.activateSpace(spaceId)
    }

    private val _aiState = MutableStateFlow<NutritionAiState>(NutritionAiState.Idle)
    val aiState: StateFlow<NutritionAiState> = _aiState.asStateFlow()

    data class MealGroceryRequest(val dayIndex: Int, val slot: MealSlot)

    data class MealGroceryResult(
        val itemCount: Int,
        val dayLabel: String,
        val slot: MealSlot,
        val isError: Boolean = false,
    )

    private val _mealGroceryLoading = MutableStateFlow<MealGroceryRequest?>(null)
    val mealGroceryLoading: StateFlow<MealGroceryRequest?> = _mealGroceryLoading.asStateFlow()

    private val _mealGroceryResult = MutableStateFlow<MealGroceryResult?>(null)
    val mealGroceryResult: StateFlow<MealGroceryResult?> = _mealGroceryResult.asStateFlow()

    fun addGroceryItem(label: String): Boolean {
        if (!canWriteHouseholdData.value) return false
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return false
        if (GroceryListPresentation.isDuplicateLabel(groceryItems.value, trimmed)) return false
        scope.launch {
            val updated = listOf(
                GroceryItem(id = newId(), label = trimmed),
            ) + groceryItems.value
            repository.saveGroceryItems(updated)
        }
        return true
    }

    fun updateGroceryItemLabel(id: String, label: String): Boolean {
        if (!canWriteHouseholdData.value) return false
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return false
        if (GroceryListPresentation.isDuplicateLabel(groceryItems.value, trimmed, excludingId = id)) {
            return false
        }
        scope.launch {
            val updated = groceryItems.value.map { item ->
                if (item.id == id) item.copy(label = trimmed) else item
            }
            repository.saveGroceryItems(updated)
        }
        return true
    }

    fun restoreGroceryItem(item: GroceryItem, index: Int) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            val current = groceryItems.value.toMutableList()
            val insertAt = index.coerceIn(0, current.size)
            if (current.none { it.id == item.id }) {
                current.add(insertAt, item)
                repository.saveGroceryItems(current)
            }
        }
    }

    fun clearCheckedGroceryItems(): List<GroceryItem> {
        if (!canWriteHouseholdData.value) return emptyList()
        val snapshot = groceryItems.value
        val updated = snapshot.filterNot { it.isChecked }
        if (updated.size == snapshot.size) return emptyList()
        scope.launch {
            repository.saveGroceryItems(updated)
        }
        return snapshot
    }

    fun restoreGroceryItemsSnapshot(items: List<GroceryItem>) {
        if (!canWriteHouseholdData.value || items.isEmpty()) return
        scope.launch {
            val currentById = groceryItems.value.associateBy { it.id }
            val restoredIds = items.map { it.id }.toSet()
            val restored = items.map { item -> currentById[item.id] ?: item }
            val newItems = groceryItems.value.filterNot { it.id in restoredIds }
            repository.saveGroceryItems(restored + newItems)
        }
    }

    fun toggleGroceryItem(id: String) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            val updated = groceryItems.value.map { item ->
                if (item.id == id) item.copy(isChecked = !item.isChecked) else item
            }
            repository.saveGroceryItems(updated)
        }
    }

    fun removeGroceryItem(id: String) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            repository.saveGroceryItems(groceryItems.value.filterNot { it.id == id })
        }
    }

    fun updateMeal(dayIndex: Int, lunch: String? = null, dinner: String? = null) {
        if (!canWriteHouseholdData.value) return
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

    fun runAiAssistant(
        mode: NutritionAiMode,
        criteria: String,
        mealPlanScope: MealPlanGenerationScope = MealPlanGenerationScope.FullWeek,
    ) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            _aiState.value = NutritionAiState.Loading
            when (mode) {
                NutritionAiMode.Advice -> {
                    aiAssistant.askAdvice(criteria)
                        .onSuccess { answer -> _aiState.value = NutritionAiState.Advice(answer) }
                        .onFailure { error -> _aiState.value = NutritionAiState.Error(error.toAiMessage()) }
                }

                NutritionAiMode.GroceryList -> {
                    aiAssistant.generateGroceryList(criteria)
                        .onSuccess { labels ->
                            val addedCount = appendAiGrocery(labels)
                            _aiState.value = NutritionAiState.GroceryList(itemCount = addedCount)
                        }
                        .onFailure { error -> _aiState.value = NutritionAiState.Error(error.toAiMessage()) }
                }

                NutritionAiMode.MealPlan -> {
                    aiAssistant.generateMealPlan(criteria, mealPlanScope, mealPlan.value)
                        .onSuccess { generation ->
                            _aiState.value = NutritionAiState.MealPlanPreview(
                                plan = mealPlan.value.copy(days = generation.days),
                                summary = generation.summary,
                                scope = mealPlanScope,
                            )
                        }
                        .onFailure { error -> _aiState.value = NutritionAiState.Error(error.toAiMessage()) }
                }
            }
        }
    }

    fun applyPreviewedMealPlan() {
        if (!canWriteHouseholdData.value) return
        val preview = _aiState.value as? NutritionAiState.MealPlanPreview ?: return
        scope.launch {
            repository.saveMealPlan(preview.plan)
            _aiState.value = NutritionAiState.Idle
        }
    }

    fun clearAiGrocery(): List<GroceryItem> {
        if (!canWriteHouseholdData.value) return emptyList()
        val snapshot = aiGroceryItems.value
        if (snapshot.isEmpty()) return emptyList()
        scope.launch {
            repository.saveAiGroceryItems(emptyList())
        }
        return snapshot
    }

    fun restoreAiGroceryItems(items: List<GroceryItem>) {
        if (!canWriteHouseholdData.value || items.isEmpty()) return
        scope.launch {
            val restoredIds = items.map { it.id }.toSet()
            repository.saveAiGroceryItems(items + aiGroceryItems.value.filterNot { it.id in restoredIds })
        }
    }

    fun generateGroceryForMeal(dayIndex: Int, slot: MealSlot, dayLabel: String) {
        if (!canWriteHouseholdData.value) return
        if (dayIndex !in 0 until WeeklyMealPlan.DAYS_IN_WEEK) return
        val day = mealPlan.value.days[dayIndex]
        val mealText = when (slot) {
            MealSlot.Lunch -> day.lunch
            MealSlot.Dinner -> day.dinner
        }
        if (mealText.isBlank()) return

        scope.launch {
            _mealGroceryLoading.value = MealGroceryRequest(dayIndex, slot)
            aiAssistant.generateGroceryForMeal(mealText)
                .onSuccess { labels ->
                    val addedCount = appendAiGrocery(labels)
                    _mealGroceryResult.value = MealGroceryResult(
                        itemCount = addedCount,
                        dayLabel = dayLabel,
                        slot = slot,
                    )
                }
                .onFailure {
                    _mealGroceryResult.value = MealGroceryResult(
                        itemCount = 0,
                        dayLabel = dayLabel,
                        slot = slot,
                        isError = true,
                    )
                }
            _mealGroceryLoading.value = null
        }
    }

    fun consumeMealGroceryResult() {
        _mealGroceryResult.value = null
    }

    fun resetAiState() {
        _aiState.value = NutritionAiState.Idle
    }

    /** @deprecated Use [runAiAssistant] with [NutritionAiMode.Advice]. */
    fun askNutritionAdvice(question: String) {
        runAiAssistant(NutritionAiMode.Advice, question)
    }

    private suspend fun appendAiGrocery(labels: List<String>): Int {
        if (!canWriteHouseholdData.value) return 0
        val seen = aiGroceryItems.value.map { it.label.trim().lowercase() }.toMutableSet()
        val newItems = buildList {
            labels.forEach { raw ->
                val label = raw.trim()
                val key = label.lowercase()
                if (label.isNotEmpty() && key !in seen) {
                    seen += key
                    add(GroceryItem(id = newId(), label = label))
                }
            }
        }
        if (newItems.isEmpty()) return 0
        repository.saveAiGroceryItems(aiGroceryItems.value + newItems)
        return newItems.size
    }

    private fun Throwable.toAiMessage(): String = message ?: "unknown_error"

    private fun newId(): String = newItemId()
}
