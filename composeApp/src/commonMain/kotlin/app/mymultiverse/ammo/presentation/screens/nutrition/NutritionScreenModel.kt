package app.mymultiverse.ammo.presentation.screens.nutrition

import app.mymultiverse.ammo.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.GroceryGhostPairing
import app.mymultiverse.ammo.domain.nutrition.GroceryListPresentation
import app.mymultiverse.ammo.domain.nutrition.GroceryPartnerNudge
import app.mymultiverse.ammo.domain.nutrition.NutritionPartnerNudge
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealPlanPresentation
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import app.mymultiverse.ammo.domain.service.AiKeyNotConfiguredException
import app.mymultiverse.ammo.domain.service.GeminiApiException
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.sharing.canWriteHouseholdData
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext
import app.mymultiverse.ammo.presentation.navigation.toNavigationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    data class Error(val message: String, val isKeyMissing: Boolean = false) : NutritionAiState()
}

class NutritionScreenModel(
    private val session: NutritionSessionCoordinator,
    private val householdRepository: HouseholdRepository,
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val aiAssistant: NutritionAiAssistantService,
    private val ghostPairingDismissStore: GroceryGhostPairingDismissStore,
    private val logger: app.mymultiverse.ammo.data.observability.AppLogger,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    private val newItemId: () -> String = { "${Random.nextLong()}_${Random.nextInt()}" },
) {
    companion object {
        const val MAX_WEEK_OFFSET = 1
        private const val SYNCED_PULSE_MS = 2_500L
        private const val COLLABORATION_DEBOUNCE_MS = 2_000L
    }

    // Serializes all grocery and meal-plan writes to prevent read-modify-write races
    // when multiple concurrent coroutines each read the current list then overwrite it.
    private val groceryMutex = Mutex()
    private val mealPlanMutex = Mutex()

    private val repository: NutritionRepository
        get() = session.nutrition.value

    val weekKey: String
        get() = repository.weekKey

    // Declared here (before the init block below) so that the coroutine launched in init
    // can safely reference _aiState.  With Dispatchers.Main.immediate the coroutine body
    // executes synchronously during construction; accessing a MutableStateFlow declared
    // after the init block would cause a NullPointerException.
    private val _aiState = MutableStateFlow<NutritionAiState>(NutritionAiState.Idle)
    val aiState: StateFlow<NutritionAiState> = _aiState.asStateFlow()

    /**
     * Auto-reset key-missing AI error state when the user saves a key while the
     * sheet is showing an [AiKeyNotConfiguredException] error, so they can retry
     * immediately without having to manually dismiss and re-open the sheet.
     */
    init {
        scope.launch {
            aiAssistant.geminiApiKey.collect { key ->
                if (key.isBlank()) return@collect
                val current = _aiState.value
                if (current is NutritionAiState.Error && current.isKeyMissing) {
                    _aiState.value = NutritionAiState.Idle
                }
            }
        }
    }

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
    private val _householdMembers = MutableStateFlow<List<HouseholdMember>>(emptyList())
    private val pendingCollaborationActivities = mutableListOf<app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity>()
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

    sealed class GroceryPartnerNudgeResult {
        data object Success : GroceryPartnerNudgeResult()
        data object Cooldown : GroceryPartnerNudgeResult()
        data object Error : GroceryPartnerNudgeResult()
    }

    sealed class MealPlanPartnerNudgeResult {
        data object Success : MealPlanPartnerNudgeResult()
        data object Cooldown : MealPlanPartnerNudgeResult()
        data object Error : MealPlanPartnerNudgeResult()
    }

    private val _groceryPartnerNudgeResult = MutableStateFlow<GroceryPartnerNudgeResult?>(null)
    val groceryPartnerNudgeResult: StateFlow<GroceryPartnerNudgeResult?> =
        _groceryPartnerNudgeResult.asStateFlow()

    private val _mealPlanPartnerNudgeResult = MutableStateFlow<MealPlanPartnerNudgeResult?>(null)
    val mealPlanPartnerNudgeResult: StateFlow<MealPlanPartnerNudgeResult?> =
        _mealPlanPartnerNudgeResult.asStateFlow()

    private val _isNudgingGroceryPartners = MutableStateFlow(false)
    val isNudgingGroceryPartners: StateFlow<Boolean> = _isNudgingGroceryPartners.asStateFlow()

    private val _isNudgingMealPlanPartners = MutableStateFlow(false)
    val isNudgingMealPlanPartners: StateFlow<Boolean> = _isNudgingMealPlanPartners.asStateFlow()

    private val _ghostPairingOffer = MutableStateFlow<GroceryGhostPairing.Offer?>(null)
    val ghostPairingOffer: StateFlow<GroceryGhostPairing.Offer?> = _ghostPairingOffer.asStateFlow()

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
                    _householdMembers.value = members
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

    val showGroceryPartnerNudge: StateFlow<Boolean> = combine(
        canWriteHouseholdData,
        _weekOffset,
        _householdMembers,
    ) { canWrite, weekOffset, members ->
        GroceryPartnerNudge.canShow(
            members = members,
            canWrite = canWrite,
            weekOffset = weekOffset,
        )
    }.stateIn(scope, SharingStarted.Eagerly, false)

    val showMealPlanPartnerNudge: StateFlow<Boolean> = combine(
        canWriteHouseholdData,
        _weekOffset,
        _householdMembers,
    ) { canWrite, weekOffset, members ->
        NutritionPartnerNudge.canShow(
            members = members,
            canWrite = canWrite,
            weekOffset = weekOffset,
        )
    }.stateIn(scope, SharingStarted.Eagerly, false)

    suspend fun activateHousehold(household: HouseholdContext) {
        _weekOffset.value = 0
        session.activateHousehold(household.id)
        refreshHouseholdMembers(household)
    }

    private suspend fun refreshHouseholdMembers(household: HouseholdContext) {
        runCatching {
            collaborationRepository.refreshMembers(
                householdId = household.id,
                ownerId = household.ownerId,
                ownerDisplayName = household.ownerDisplayName.orEmpty(),
            )
        }
    }

    fun selectWeekOffset(offset: Int) {
        if (offset !in 0..MAX_WEEK_OFFSET) return
        if (offset == _weekOffset.value) return
        scope.launch {
            _weekOffset.value = offset
            session.selectWeek(WeekCalendar.weekKeyForOffset(offset = offset))
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        if (_isRefreshing.value) return
        scope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshFromRemote()
                householdRepository.refreshMembership()
                    .getOrNull()
                    ?.let { status ->
                        if (status is HouseholdMembershipStatus.Active) {
                            refreshHouseholdMembers(status.household.toNavigationContext())
                        }
                    }
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
        val isKeyMissing: Boolean = false,
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

    fun addGroceryItem(label: String, skipPairingPrompt: Boolean = false): Boolean {
        if (!canWriteHouseholdData.value) return false
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return false
        if (GroceryListPresentation.findItemByNormalizedLabel(groceryItems.value, trimmed) != null) {
            return true
        }
        scope.launch {
            groceryMutex.withLock {
                // Re-check for duplicate inside the lock (another launch may have added it).
                if (GroceryListPresentation.findItemByNormalizedLabel(groceryItems.value, trimmed) == null) {
                    val updated = listOf(GroceryItem(id = newId(), label = trimmed)) + groceryItems.value
                    repository.saveGroceryItems(updated)
                    logger.breadcrumb("grocery_add total=${updated.size}")
                    if (!skipPairingPrompt) maybeShowGhostPairing(trimmed, updated)
                }
            }
        }
        return true
    }

    fun dismissGhostPairing() {
        val offer = _ghostPairingOffer.value ?: return
        repository.householdId?.let { householdId ->
            ghostPairingDismissStore.dismiss(householdId, offer.id)
        }
        _ghostPairingOffer.value = null
    }

    fun acceptGhostPairing(localizedLabels: List<String>) {
        if (!canWriteHouseholdData.value) return
        if (_ghostPairingOffer.value == null) return
        _ghostPairingOffer.value = null
        scope.launch {
            groceryMutex.withLock {
                var current = groceryItems.value
                localizedLabels.forEach { label ->
                    val trimmed = label.trim()
                    if (trimmed.isEmpty()) return@forEach
                    if (GroceryListPresentation.findItemByNormalizedLabel(current, trimmed) == null) {
                        current = listOf(GroceryItem(id = newId(), label = trimmed)) + current
                    }
                }
                if (current != groceryItems.value) repository.saveGroceryItems(current)
            }
        }
    }

    private fun maybeShowGhostPairing(triggerLabel: String, itemsAfterAdd: List<GroceryItem>) {
        val householdId = repository.householdId ?: return
        val dismissed = ghostPairingDismissStore.dismissedIds(householdId)
        _ghostPairingOffer.value = GroceryGhostPairing.findOffer(
            triggerLabel = triggerLabel,
            existingItems = itemsAfterAdd,
            dismissedPairingIds = dismissed,
        )
    }

    fun updateGroceryItemLabel(id: String, label: String): Boolean {
        if (!canWriteHouseholdData.value) return false
        val trimmed = label.trim()
        if (trimmed.isEmpty()) return false
        if (GroceryListPresentation.isDuplicateLabel(groceryItems.value, trimmed, excludingId = id)) {
            return false
        }
        scope.launch {
            groceryMutex.withLock {
                val updated = groceryItems.value.map { item ->
                    if (item.id == id) item.copy(label = trimmed) else item
                }
                repository.saveGroceryItems(updated)
            }
        }
        return true
    }

    fun restoreGroceryItem(item: GroceryItem, index: Int) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            groceryMutex.withLock {
                val current = groceryItems.value.toMutableList()
                val insertAt = index.coerceIn(0, current.size)
                if (current.none { it.id == item.id }) {
                    current.add(insertAt, item)
                    repository.saveGroceryItems(current)
                }
            }
        }
    }

    fun clearCheckedGroceryItems(): List<GroceryItem> {
        if (!canWriteHouseholdData.value) return emptyList()
        // Snapshot before launch for the undo payload; actual save is serialized inside the lock.
        val undoSnapshot = groceryItems.value
        scope.launch {
            groceryMutex.withLock {
                val current = groceryItems.value
                val updated = current.filterNot { it.isChecked }
                if (updated.size < current.size) repository.saveGroceryItems(updated)
            }
        }
        return undoSnapshot
    }

    fun restoreGroceryItemsSnapshot(items: List<GroceryItem>) {
        if (!canWriteHouseholdData.value || items.isEmpty()) return
        scope.launch {
            groceryMutex.withLock {
                val current = groceryItems.value
                val currentById = current.associateBy { it.id }
                val restoredIds = items.map { it.id }.toSet()
                val restored = items.map { item -> currentById[item.id] ?: item }
                val newItems = current.filterNot { it.id in restoredIds }
                repository.saveGroceryItems(restored + newItems)
            }
        }
    }

    fun toggleGroceryItem(id: String) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            groceryMutex.withLock {
                val updated = groceryItems.value.map { item ->
                    if (item.id == id) item.copy(isChecked = !item.isChecked) else item
                }
                val checked = updated.count { it.isChecked }
                logger.breadcrumb("grocery_toggle checked=$checked total=${updated.size}")
                repository.saveGroceryItems(updated)
            }
        }
    }

    fun removeGroceryItem(id: String) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            groceryMutex.withLock {
                val updated = groceryItems.value.filterNot { it.id == id }
                repository.saveGroceryItems(updated)
                logger.breadcrumb("grocery_remove total=${updated.size}")
            }
        }
    }

    fun moveActiveGroceryItem(itemId: String, direction: Int) {
        if (!canWriteHouseholdData.value || direction == 0) return
        scope.launch {
            groceryMutex.withLock {
                val current = groceryItems.value
                val updated = GroceryListPresentation.moveActiveItem(current, itemId, direction)
                if (updated != current) repository.saveGroceryItems(updated)
            }
        }
    }

    fun updateMeal(dayIndex: Int, lunch: String? = null, dinner: String? = null) {
        if (!canWriteHouseholdData.value) return
        if (dayIndex !in 0 until WeeklyMealPlan.DAYS_IN_WEEK) return
        scope.launch {
            mealPlanMutex.withLock {
                val current = mealPlan.value
                val days = current.days.toMutableList()
                val existing = days[dayIndex]
                days[dayIndex] = existing.copy(
                    lunch = lunch ?: existing.lunch,
                    dinner = dinner ?: existing.dinner,
                )
                val slot = when {
                    lunch != null && dinner != null -> "both"
                    lunch != null -> "lunch"
                    else -> "dinner"
                }
                logger.breadcrumb("meal_update day=$dayIndex slot=$slot")
                repository.saveMealPlan(current.copy(days = days))
            }
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
            mealPlanMutex.withLock {
                repository.saveMealPlan(
                    mealPlan.value.copy(days = List(WeeklyMealPlan.DAYS_IN_WEEK) { DayMeals() }),
                )
            }
        }
    }

    fun copyDinnerToTomorrowLunch(dayIndex: Int) {
        if (!canWriteHouseholdData.value) return
        val tomorrowIndex = MealPlanPresentation.tomorrowIndex(dayIndex) ?: return
        val dinner = mealPlan.value.days[dayIndex].dinner.trim()
        if (dinner.isEmpty()) return
        updateMeal(tomorrowIndex, lunch = dinner)
    }

    // Tracked so a new runAiAssistant call can cancel an in-flight request.
    private var aiAssistantJob: Job? = null
    private var aiAssistantJobGeneration = 0

    fun runAiAssistant(
        mode: NutritionAiMode,
        criteria: String,
        mealPlanScope: MealPlanGenerationScope = MealPlanGenerationScope.FullWeek,
    ) {
        if (!canWriteHouseholdData.value) return
        aiAssistantJob?.cancel()
        val generation = ++aiAssistantJobGeneration
        aiAssistantJob = scope.launch {
            _aiState.value = NutritionAiState.Loading
            try {
                when (mode) {
                    NutritionAiMode.Advice -> {
                        aiAssistant.askAdvice(criteria)
                            .onSuccess { answer -> _aiState.value = NutritionAiState.Advice(answer) }
                            .onFailure { error -> _aiState.value = error.toAiErrorState() }
                    }

                    NutritionAiMode.GroceryList -> {
                        aiAssistant.generateGroceryList(criteria)
                            .onSuccess { labels ->
                                val addedCount = appendAiGrocery(labels)
                                _aiState.value = NutritionAiState.GroceryList(itemCount = addedCount)
                            }
                            .onFailure { error -> _aiState.value = error.toAiErrorState() }
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
                            .onFailure { error -> _aiState.value = error.toAiErrorState() }
                    }
                }
            } catch (e: CancellationException) {
                // Intentional cancel (new request superseded this one) — reset only if still current.
                if (generation == aiAssistantJobGeneration) {
                    _aiState.value = NutritionAiState.Idle
                }
                throw e
            } finally {
                // Ensure Loading is never left stuck even on unexpected exit.
                if (generation == aiAssistantJobGeneration && _aiState.value is NutritionAiState.Loading) {
                    _aiState.value = NutritionAiState.Idle
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
            groceryMutex.withLock {
                val remainingAi = aiGroceryItems.value.filterNot { it.id == itemId }
                if (!GroceryListPresentation.isDuplicateLabel(groceryItems.value, trimmed)) {
                    val updated = listOf(GroceryItem(id = newId(), label = trimmed)) + groceryItems.value
                    repository.saveGroceryItems(updated)
                }
                repository.saveAiGroceryItems(remainingAi)
            }
        }
        return true
    }

    fun adoptAllAiGrocerySuggestions() {
        if (!canWriteHouseholdData.value) return
        val suggestions = aiGroceryItems.value.filterNot { it.isPantryCheck }
        if (suggestions.isEmpty()) return
        scope.launch {
            groceryMutex.withLock {
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
                val remainingPantry = aiGroceryItems.value.filter { it.isPantryCheck }
                repository.saveAiGroceryItems(remainingPantry)
                _adoptAllGroceryResult.value = adoptedCount
            }
        }
    }

    fun dismissPantryCheckItem(itemId: String) {
        if (!canWriteHouseholdData.value) return
        scope.launch {
            groceryMutex.withLock {
                repository.saveAiGroceryItems(aiGroceryItems.value.filterNot { it.id == itemId })
            }
        }
    }

    fun adoptRemainingPantryItems() {
        if (!canWriteHouseholdData.value) return
        val pantry = aiGroceryItems.value.filter { it.isPantryCheck }
        if (pantry.isEmpty()) return
        scope.launch {
            groceryMutex.withLock {
            var grocery = groceryItems.value
            pantry.forEach { item ->
                val trimmed = item.label.trim()
                if (trimmed.isNotEmpty() && !GroceryListPresentation.isDuplicateLabel(grocery, trimmed)) {
                    grocery = listOf(GroceryItem(id = newId(), label = trimmed)) + grocery
                }
            }
            repository.saveGroceryItems(grocery)
            repository.saveAiGroceryItems(aiGroceryItems.value.filterNot { it.isPantryCheck })
            }
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
                .onFailure { error ->
                    // Surface key-missing so the AI sheet opens the inline key form
                    // even when called from the silent (ingredients-after-apply) path.
                    if (error is AiKeyNotConfiguredException) triggerKeySetupPrompt()
                }
                .getOrElse { return 0 }
                .let { labels -> appendMealGroceryLabels(labels) }
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
            try {
                aiAssistant.generateGroceryForMeal(mealText)
                    .onSuccess { labels ->
                        val addedCount = appendMealGroceryLabels(labels)
                        _mealGroceryResult.value = MealGroceryResult(
                            itemCount = addedCount,
                            dayLabel = dayLabel,
                            slot = slot,
                        )
                    }
                    .onFailure { error ->
                        _mealGroceryResult.value = MealGroceryResult(
                            itemCount = 0,
                            dayLabel = dayLabel,
                            slot = slot,
                            isError = true,
                            isKeyMissing = error is AiKeyNotConfiguredException,
                        )
                    }
            } finally {
                _mealGroceryLoading.value = null  // Always clear — even on cancellation
            }
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
            try {
                plannedMeals.forEach { plannedMeal ->
                    _mealGroceryLoading.value = MealGroceryRequest(plannedMeal.dayIndex, plannedMeal.slot)
                    aiAssistant.generateGroceryForMeal(plannedMeal.text)
                        .onSuccess { labels -> totalAdded += appendMealGroceryLabels(labels) }
                        .onFailure { hadError = true }
                }
                _bulkMealGroceryResult.value = BulkMealGroceryResult(
                    addedCount = totalAdded,
                    mealsProcessed = plannedMeals.size,
                    isError = hadError,
                )
            } finally {
                _mealGroceryLoading.value = null
                _bulkMealGroceryLoading.value = false
            }
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

    /**
     * Sets [aiState] to the key-missing error so the AI sheet immediately shows the
     * inline [AiKeyInlineForm] when opened. Called by the meal→grocery flow when it
     * detects [AiKeyNotConfiguredException] without the sheet being open.
     */
    fun triggerKeySetupPrompt() {
        _aiState.value = NutritionAiState.Error(
            message = "ai_key_not_configured",
            isKeyMissing = true,
        )
    }

    /** @deprecated Use [runAiAssistant] with [NutritionAiMode.Advice]. */
    fun askNutritionAdvice(question: String) {
        runAiAssistant(NutritionAiMode.Advice, question)
    }

    private suspend fun appendAiGrocery(labels: List<String>): Int {
        if (!canWriteHouseholdData.value) return 0
        return appendAiGroceryItems(labels, isPantryCheck = false)
    }

    /**
     * Meal-plan "Add to grocery list" writes directly to the editable list (all labels,
     * including pantry staples). AI adviser grocery mode still uses [appendLabeledAiGroceryItems].
     */
    private suspend fun appendMealGroceryLabels(labels: List<String>): Int {
        if (!canWriteHouseholdData.value) return 0
        return groceryMutex.withLock {
            var grocery = groceryItems.value
            val seenInBatch = mutableSetOf<String>()
            val newItems = buildList {
                labels.forEach { raw ->
                    val label = raw.trim()
                    val key = label.lowercase()
                    if (label.isEmpty() || key in seenInBatch) return@forEach
                    if (GroceryListPresentation.isDuplicateLabel(grocery, label)) return@forEach
                    seenInBatch += key
                    val item = GroceryItem(id = newId(), label = label)
                    add(item)
                    grocery = listOf(item) + grocery
                }
            }
            if (newItems.isEmpty()) return@withLock 0
            repository.saveGroceryItems(grocery)
            newItems.size
        }
    }

    private suspend fun appendAiGroceryItems(labels: List<String>, isPantryCheck: Boolean): Int {
        return appendLabeledAiGroceryItems(labels.map { it to isPantryCheck })
    }

    private suspend fun appendLabeledAiGroceryItems(labels: List<Pair<String, Boolean>>): Int {
        if (!canWriteHouseholdData.value) return 0
        return groceryMutex.withLock {
            val seen = aiGroceryItems.value
                .map { it.label.trim().lowercase() }
                .toMutableSet()
            val newItems = buildList {
                labels.forEach { (raw, isPantryCheck) ->
                    val label = raw.trim()
                    val key = label.lowercase()
                    if (label.isNotEmpty() && key !in seen) {
                        seen += key
                        add(GroceryItem(id = newId(), label = label, isPantryCheck = isPantryCheck))
                    }
                }
            }
            if (newItems.isEmpty()) return@withLock 0
            repository.saveAiGroceryItems(aiGroceryItems.value + newItems)
            newItems.size
        }
    }

    fun consumeCollaborationSnackbar() {
        _collaborationSnackbar.value = null
    }

    fun consumeGroceryPartnerNudgeResult() {
        _groceryPartnerNudgeResult.value = null
    }

    fun consumeMealPlanPartnerNudgeResult() {
        _mealPlanPartnerNudgeResult.value = null
    }

    fun nudgePartnersToUpdateGroceryList() {
        if (_isNudgingGroceryPartners.value) return
        val householdId = repository.householdId?.trim().orEmpty()
        if (householdId.isEmpty()) {
            _groceryPartnerNudgeResult.value = GroceryPartnerNudgeResult.Error
            return
        }
        scope.launch {
            _isNudgingGroceryPartners.value = true
            try {
                val result = collaborationRepository.nudgePartnersToUpdateGroceryList(
                    householdId = householdId,
                    weekKey = weekKey,
                )
                _groceryPartnerNudgeResult.value = result.fold(
                    onSuccess = { GroceryPartnerNudgeResult.Success },
                    onFailure = { error ->
                        when {
                            CollaborationErrorCodes.messageContains(
                                CollaborationErrorCodes.GROCERY_NUDGE_COOLDOWN,
                                error.message,
                            ) -> GroceryPartnerNudgeResult.Cooldown

                            else -> GroceryPartnerNudgeResult.Error
                        }
                    },
                )
            } finally {
                _isNudgingGroceryPartners.value = false
            }
        }
    }

    fun nudgePartnersToUpdateMealPlan() {
        if (_isNudgingMealPlanPartners.value) return
        val householdId = repository.householdId?.trim().orEmpty()
        if (householdId.isEmpty()) {
            _mealPlanPartnerNudgeResult.value = MealPlanPartnerNudgeResult.Error
            return
        }
        scope.launch {
            _isNudgingMealPlanPartners.value = true
            try {
                val result = collaborationRepository.nudgePartnersToUpdateMealPlan(
                    householdId = householdId,
                    weekKey = weekKey,
                )
                _mealPlanPartnerNudgeResult.value = result.fold(
                    onSuccess = { MealPlanPartnerNudgeResult.Success },
                    onFailure = { error ->
                        when {
                            CollaborationErrorCodes.messageContains(
                                CollaborationErrorCodes.MEAL_PLAN_NUDGE_COOLDOWN,
                                error.message,
                            ) -> MealPlanPartnerNudgeResult.Cooldown

                            else -> MealPlanPartnerNudgeResult.Error
                        }
                    },
                )
            } finally {
                _isNudgingMealPlanPartners.value = false
            }
        }
    }

    private fun onCollaborationActivity(
        activity: app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity,
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
        batch: List<app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity>,
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

    private fun Throwable.toAiErrorState(): NutritionAiState.Error =
        NutritionAiState.Error(
            message = toAiMessage(),
            isKeyMissing = this is AiKeyNotConfiguredException ||
                (this is GeminiApiException && isAuthError),
        )

    private fun newId(): String = newItemId()
}
