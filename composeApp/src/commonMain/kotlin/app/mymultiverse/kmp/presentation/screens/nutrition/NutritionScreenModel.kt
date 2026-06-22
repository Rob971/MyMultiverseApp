package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.GroceryListPresentation
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.MealPlanPresentation
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
import app.mymultiverse.kmp.domain.sharing.canWriteHouseholdData
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import app.mymultiverse.kmp.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val aiAssistant: NutritionAiAssistantService,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val newItemId: () -> String = { "${Random.nextLong()}_${Random.nextInt()}" },
) {
    companion object {
        const val MAX_WEEK_OFFSET = 1
        private const val SYNCED_PULSE_MS = 2_500L
        private const val COLLABORATION_DEBOUNCE_MS = 2_000L
    }

    private val repository: NutritionRepository
        get() = session.nutrition.value

    val weekKey: String
        get() = repository.weekKey

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    val canGoToPreviousWeek: Boolean
        get() = _weekOffset.value > 0

    val canGoToNextWeek: Boolean
        get() = _weekOffset.value < MAX_WEEK_OFFSET

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

    private val _displaySyncStatus = MutableStateFlow<NutritionSyncStatus>(NutritionSyncStatus.Idle)
    private var syncPulseJob: Job? = null
    private var cachedHouseholdMembers: List<HouseholdMember> = emptyList()
    private val pendingCollaborationActivities = mutableListOf<app.mymultiverse.kmp.domain.nutrition.NutritionCollaborationActivity>()
    private var collaborationDebounceJob: Job? = null

    val syncStatus: StateFlow<NutritionSyncStatus> = _displaySyncStatus.asStateFlow()

    data class CollaborationSnackbarEvent(
        val actorName: String,
        val kind: NutritionCollaborationActivityKind,
        val itemLabel: String,
        val batchedCount: Int,
    )

    private val _collaborationSnackbar = MutableStateFlow<CollaborationSnackbarEvent?>(null)
    val collaborationSnackbar: StateFlow<CollaborationSnackbarEvent?> = _collaborationSnackbar.asStateFlow()

    init {
        scope.launch {
            session.nutrition
                .flatMapLatest { repository ->
                    val householdId = repository.householdId
                    if (householdId.isNullOrBlank()) {
                        emptyFlow()
                    } else {
                        collaborationRepository.observeMembers(householdId)
                    }
                }
                .collect { members ->
                    cachedHouseholdMembers = members
                }
        }
        scope.launch {
            session.observeCollaborationActivity().collect(::onCollaborationActivity)
        }
        scope.launch {
            session.observeSyncStatus().collect { raw ->
                when {
                    raw == NutritionSyncStatus.Idle &&
                        (_displaySyncStatus.value == NutritionSyncStatus.Syncing ||
                            _displaySyncStatus.value is NutritionSyncStatus.PendingPush) -> {
                        _displaySyncStatus.value = NutritionSyncStatus.Synced
                        syncPulseJob?.cancel()
                        syncPulseJob = launch {
                            delay(SYNCED_PULSE_MS)
                            if (_displaySyncStatus.value == NutritionSyncStatus.Synced) {
                                _displaySyncStatus.value = NutritionSyncStatus.Idle
                            }
                        }
                    }
                    raw != NutritionSyncStatus.Idle -> {
                        syncPulseJob?.cancel()
                        _displaySyncStatus.value = raw
                    }
                    _displaySyncStatus.value != NutritionSyncStatus.Synced -> {
                        _displaySyncStatus.value = raw
                    }
                }
            }
        }
    }

    val canWriteHouseholdData: StateFlow<Boolean> = householdRepository
        .observeMembershipStatus()
        .map { status ->
            when (status) {
                is HouseholdMembershipStatus.Active -> status.role.canWriteHouseholdData()
                else -> true
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, true)

    suspend fun activateHousehold(householdId: String) {
        _weekOffset.value = 0
        session.activateHousehold(householdId)
    }

    fun selectWeekOffset(offset: Int) {
        if (offset !in 0..MAX_WEEK_OFFSET) return
        if (offset == _weekOffset.value) return
        scope.launch {
            _weekOffset.value = offset
            session.selectWeek(WeekCalendar.weekKeyForOffset(offset = offset))
        }
    }

    private val _aiState = MutableStateFlow<NutritionAiState>(NutritionAiState.Idle)
    val aiState: StateFlow<NutritionAiState> = _aiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        if (_isRefreshing.value) return
        scope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshFromRemote()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

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

    data class BulkMealGroceryResult(
        val addedCount: Int,
        val mealsProcessed: Int,
        val isError: Boolean = false,
    )

    private val _bulkMealGroceryLoading = MutableStateFlow(false)
    val bulkMealGroceryLoading: StateFlow<Boolean> = _bulkMealGroceryLoading.asStateFlow()

    private val _bulkMealGroceryResult = MutableStateFlow<BulkMealGroceryResult?>(null)
    val bulkMealGroceryResult: StateFlow<BulkMealGroceryResult?> = _bulkMealGroceryResult.asStateFlow()

    private val _adoptAllGroceryResult = MutableStateFlow<Int?>(null)
    val adoptAllGroceryResult: StateFlow<Int?> = _adoptAllGroceryResult.asStateFlow()

    fun addGroceryItem(label: String): Boolean {
        if (!canWriteHouseholdData.value) return false
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return false
        if (GroceryListPresentation.findItemByNormalizedLabel(groceryItems.value, trimmed) != null) {
            return true
        }
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

    fun clearMealSlot(dayIndex: Int, slot: MealSlot) {
        when (slot) {
            MealSlot.Lunch -> updateMeal(dayIndex, lunch = "")
            MealSlot.Dinner -> updateMeal(dayIndex, dinner = "")
        }
    }

    fun clearMealsForDay(dayIndex: Int) {
        updateMeal(dayIndex, lunch = "", dinner = "")
    }

    fun clearMealPlanWeek() {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            repository.saveMealPlan(
                mealPlan.value.copy(days = List(WeeklyMealPlan.DAYS_IN_WEEK) { DayMeals() }),
            )
        }
    }

    fun copyDinnerToTomorrowLunch(dayIndex: Int) {
        if (!canWriteHouseholdData.value) return
        val tomorrowIndex = MealPlanPresentation.tomorrowIndex(dayIndex) ?: return
        val dinner = mealPlan.value.days[dayIndex].dinner.trim()
        if (dinner.isEmpty()) return
        updateMeal(tomorrowIndex, lunch = dinner)
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
        scope.launch { applyPreviewedMealPlanAndAwait() }
    }

    suspend fun applyPreviewedMealPlanAndAwait() {
        if (!canWriteHouseholdData.value) return
        val preview = _aiState.value as? NutritionAiState.MealPlanPreview ?: return
        repository.saveMealPlan(preview.plan)
        _aiState.value = NutritionAiState.Idle
    }

    /**
     * Moves one AI suggestion into the editable grocery list and removes it from AI suggestions.
     * If the label is already on the list, only removes the AI chip (no snackbar).
     */
    fun adoptAiGrocerySuggestion(itemId: String): Boolean {
        if (!canWriteHouseholdData.value) return false
        val suggestion = aiGroceryItems.value.firstOrNull { it.id == itemId } ?: return false
        val trimmed = suggestion.label.trim()
        if (trimmed.isEmpty()) return false
        scope.launch {
            val remainingAi = aiGroceryItems.value.filterNot { it.id == itemId }
            if (!GroceryListPresentation.isDuplicateLabel(groceryItems.value, trimmed)) {
                val updated = listOf(GroceryItem(id = newId(), label = trimmed)) + groceryItems.value
                repository.saveGroceryItems(updated)
            }
            repository.saveAiGroceryItems(remainingAi)
        }
        return true
    }

    fun adoptAllAiGrocerySuggestions() {
        if (!canWriteHouseholdData.value) return
        val suggestions = aiGroceryItems.value
        if (suggestions.isEmpty()) return
        scope.launch {
            var adoptedCount = 0
            var grocery = groceryItems.value
            suggestions.forEach { suggestion ->
                val trimmed = suggestion.label.trim()
                if (trimmed.isEmpty()) return@forEach
                if (!GroceryListPresentation.isDuplicateLabel(grocery, trimmed)) {
                    grocery = listOf(GroceryItem(id = newId(), label = trimmed)) + grocery
                    adoptedCount++
                }
            }
            repository.saveGroceryItems(grocery)
            repository.saveAiGroceryItems(emptyList())
            _adoptAllGroceryResult.value = adoptedCount
        }
    }

    fun consumeAdoptAllGroceryResult() {
        _adoptAllGroceryResult.value = null
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

    suspend fun generateGroceryForMealSilent(dayIndex: Int, slot: MealSlot): Int {
        if (!canWriteHouseholdData.value) return 0
        if (dayIndex !in 0 until WeeklyMealPlan.DAYS_IN_WEEK) return 0
        val day = mealPlan.value.days[dayIndex]
        val mealText = when (slot) {
            MealSlot.Lunch -> day.lunch
            MealSlot.Dinner -> day.dinner
        }
        if (mealText.isBlank()) return 0

        _mealGroceryLoading.value = MealGroceryRequest(dayIndex, slot)
        return try {
            aiAssistant.generateGroceryForMeal(mealText)
                .getOrElse { return 0 }
                .let { labels -> appendAiGrocery(labels) }
        } finally {
            _mealGroceryLoading.value = null
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

    fun generateGroceryForAllPlannedMeals() {
        if (!canWriteHouseholdData.value) return
        val plannedMeals = MealPlanPresentation.plannedMeals(mealPlan.value.days)
        if (plannedMeals.isEmpty()) return

        scope.launch {
            _bulkMealGroceryLoading.value = true
            var totalAdded = 0
            var hadError = false
            plannedMeals.forEach { plannedMeal ->
                _mealGroceryLoading.value = MealGroceryRequest(plannedMeal.dayIndex, plannedMeal.slot)
                aiAssistant.generateGroceryForMeal(plannedMeal.text)
                    .onSuccess { labels -> totalAdded += appendAiGrocery(labels) }
                    .onFailure { hadError = true }
            }
            _mealGroceryLoading.value = null
            _bulkMealGroceryResult.value = BulkMealGroceryResult(
                addedCount = totalAdded,
                mealsProcessed = plannedMeals.size,
                isError = hadError,
            )
            _bulkMealGroceryLoading.value = false
        }
    }

    fun consumeBulkMealGroceryResult() {
        _bulkMealGroceryResult.value = null
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

    fun consumeCollaborationSnackbar() {
        _collaborationSnackbar.value = null
    }

    private fun onCollaborationActivity(
        activity: app.mymultiverse.kmp.domain.nutrition.NutritionCollaborationActivity,
    ) {
        pendingCollaborationActivities += activity
        collaborationDebounceJob?.cancel()
        collaborationDebounceJob = scope.launch {
            delay(COLLABORATION_DEBOUNCE_MS)
            val batch = pendingCollaborationActivities.toList()
            pendingCollaborationActivities.clear()
            emitCollaborationSnackbar(batch)
        }
    }

    private fun emitCollaborationSnackbar(
        batch: List<app.mymultiverse.kmp.domain.nutrition.NutritionCollaborationActivity>,
    ) {
        if (batch.isEmpty()) return
        val latest = batch.last()
        _collaborationSnackbar.value = CollaborationSnackbarEvent(
            actorName = resolveActorName(latest.actorUserId),
            kind = latest.kind,
            itemLabel = latest.itemLabel,
            batchedCount = batch.size,
        )
    }

    private fun resolveActorName(userId: String?): String {
        if (userId.isNullOrBlank()) return ""
        return cachedHouseholdMembers
            .asSequence()
            .filter { it.kind == HouseholdMemberKind.Person }
            .firstOrNull { it.referenceId == userId }
            ?.displayName
            .orEmpty()
    }

    private fun Throwable.toAiMessage(): String = message ?: "unknown_error"

    private fun newId(): String = newItemId()
}
