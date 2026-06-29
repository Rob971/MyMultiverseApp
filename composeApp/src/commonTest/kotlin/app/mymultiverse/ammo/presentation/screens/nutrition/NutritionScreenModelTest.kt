package app.mymultiverse.ammo.presentation.screens.nutrition

import app.mymultiverse.ammo.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.presentation.di.FakeHouseholdCollaborationRepository
import app.mymultiverse.ammo.presentation.di.FakeHouseholdRepository
import app.mymultiverse.ammo.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.russhwolf.settings.MapSettings
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val weekKey = "2026-05-18"
    private lateinit var modelScope: CoroutineScope

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        modelScope = CoroutineScope(SupervisorJob() + testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addGroceryItem_trimsLabelAndPersists() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope) { "item-1" }

        assertTrue(model.addGroceryItem("  Olive oil  "))
        advanceUntilIdle()

        assertEquals(listOf(GroceryItem("item-1", "Olive oil", false)), repository.grocery.value)
    }

    @Test
    fun addGroceryItem_ignoresBlankInput() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope)

        assertFalse(model.addGroceryItem("   "))
        advanceUntilIdle()

        assertTrue(repository.grocery.value.isEmpty())
    }

    @Test
    fun toggleGroceryItem_flipsCheckedState() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(GroceryItem("1", "Rice", false))
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.toggleGroceryItem("1")
        advanceUntilIdle()

        assertTrue(repository.grocery.value.single().isChecked)
    }

    @Test
    fun addGroceryItem_mergesDuplicateLabelsSilently() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope) { "item-1" }

        assertTrue(model.addGroceryItem("Milk"))
        advanceUntilIdle()
        assertTrue(model.addGroceryItem(" milk "))
        advanceUntilIdle()

        assertEquals(1, repository.grocery.value.size)
    }

    @Test
    fun updateGroceryItemLabel_updatesExistingItem() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(GroceryItem("1", "Rice", false))
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        assertTrue(model.updateGroceryItemLabel("1", "Brown rice"))
        advanceUntilIdle()

        assertEquals("Brown rice", repository.grocery.value.single().label)
    }

    @Test
    fun updateGroceryItemLabel_rejectsBlankAndDuplicateLabels() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(
            GroceryItem("1", "Rice", false),
            GroceryItem("2", "Beans", false),
        )
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        assertFalse(model.updateGroceryItemLabel("1", "   "))
        assertFalse(model.updateGroceryItemLabel("1", " beans "))
        advanceUntilIdle()

        assertEquals(listOf("Rice", "Beans"), repository.grocery.value.map { it.label })
    }

    @Test
    fun restoreGroceryItem_insertsAtIndex() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(GroceryItem("2", "Beans", false))
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.restoreGroceryItem(GroceryItem("1", "Rice", false), index = 0)
        advanceUntilIdle()

        assertEquals(listOf("Rice", "Beans"), repository.grocery.value.map { it.label })
    }

    @Test
    fun clearCheckedGroceryItems_removesOnlyChecked() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(
            GroceryItem("1", "Rice", false),
            GroceryItem("2", "Beans", true),
        )
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.clearCheckedGroceryItems()
        advanceUntilIdle()

        assertEquals("Rice", repository.grocery.value.single().label)
    }

    @Test
    fun restoreGroceryItemsSnapshot_restoresBulkClearedItemsInOriginalOrder() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(
            GroceryItem("1", "Rice", false),
            GroceryItem("2", "Beans", true),
            GroceryItem("3", "Milk", true),
        )
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        val snapshot = model.clearCheckedGroceryItems()
        advanceUntilIdle()
        assertEquals(listOf("Rice"), repository.grocery.value.map { it.label })

        model.restoreGroceryItemsSnapshot(snapshot)
        advanceUntilIdle()

        assertEquals(listOf("Rice", "Beans", "Milk"), repository.grocery.value.map { it.label })
    }

    @Test
    fun removeGroceryItem_deletesItem() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(
            GroceryItem("1", "Rice", false),
            GroceryItem("2", "Beans", false),
        )
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.removeGroceryItem("1")
        advanceUntilIdle()

        assertEquals("2", repository.grocery.value.single().id)
    }

    @Test
    fun moveActiveGroceryItem_reordersActiveItems() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(
            GroceryItem("1", "Milk", false),
            GroceryItem("2", "Bread", false),
            GroceryItem("3", "Eggs", true),
        )
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.moveActiveGroceryItem("1", direction = 1)
        advanceUntilIdle()

        assertEquals(listOf("Bread", "Milk", "Eggs"), repository.grocery.value.map { it.label })
    }

    @Test
    fun updateMeal_updatesLunchAndDinnerForDay() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.updateMeal(dayIndex = 2, lunch = "Quinoa bowl")
        advanceUntilIdle()
        model.updateMeal(dayIndex = 2, dinner = "Roast chicken")
        advanceUntilIdle()

        assertEquals("Quinoa bowl", repository.mealPlan.value.days[2].lunch)
        assertEquals("Roast chicken", repository.mealPlan.value.days[2].dinner)
    }

    @Test
    fun updateMeal_ignoresInvalidDayIndex() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.updateMeal(dayIndex = 99, lunch = "Ignored")
        advanceUntilIdle()

        assertEquals(DayMeals(), repository.mealPlan.value.days.first())
    }

    @Test
    fun askNutritionAdvice_successSetsAnswerState() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val advice = FakeNutritionAdviceService(answer = "Eat more vegetables.")
        val model = nutritionScreenModel(repository, advice, scope = modelScope)

        model.askNutritionAdvice("Vegetables?")
        advanceUntilIdle()

        assertIs<NutritionAiState.Advice>(model.aiState.value)
        assertEquals("Eat more vegetables.", (model.aiState.value as NutritionAiState.Advice).text)
    }

    @Test
    fun askNutritionAdvice_failureSetsErrorState() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val advice = FakeNutritionAdviceService(shouldFail = true, failureMessage = "empty_question")
        val model = nutritionScreenModel(repository, advice, scope = modelScope)

        model.askNutritionAdvice("")
        advanceUntilIdle()

        assertIs<NutritionAiState.Error>(model.aiState.value)
        assertEquals("empty_question", (model.aiState.value as NutritionAiState.Error).message)
    }

    @Test
    fun resetAiState_returnsToIdle() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope)
        model.askNutritionAdvice("Protein?")
        advanceUntilIdle()

        model.resetAiState()

        assertEquals(NutritionAiState.Idle, model.aiState.value)
    }

    @Test
    fun runAiAssistant_groceryMode_persistsReadOnlyAiGrocery() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val ai = FakeNutritionAdviceService(groceryLabels = listOf("Milk", "Eggs"))
        val model = nutritionScreenModel(repository, ai, scope = modelScope) { "ai-1" }

        model.runAiAssistant(NutritionAiMode.GroceryList, "high protein")
        advanceUntilIdle()

        val groceryState = model.aiState.value
        assertIs<NutritionAiState.GroceryList>(groceryState)
        assertEquals(2, groceryState.itemCount)
        assertEquals(2, repository.aiGrocery.value.size)
        assertEquals("Milk", repository.aiGrocery.value.first().label)
    }

    @Test
    fun adoptAiGrocerySuggestion_movesItemToEditableListAndRemovesChip() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.aiGrocery.value = listOf(
            GroceryItem("ai-1", "Olive oil"),
            GroceryItem("ai-2", "Salt"),
        )
        val model = nutritionScreenModel(repository, scope = modelScope) { "grocery-1" }
        advanceUntilIdle()

        assertTrue(model.adoptAiGrocerySuggestion("ai-1"))
        advanceUntilIdle()

        assertEquals(listOf(GroceryItem("grocery-1", "Olive oil", false)), repository.grocery.value)
        assertEquals(listOf("Salt"), repository.aiGrocery.value.map { it.label })
    }

    @Test
    fun adoptAiGrocerySuggestion_whenDuplicateOnlyRemovesChip() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(GroceryItem("g-1", "Milk", false))
        repository.aiGrocery.value = listOf(GroceryItem("ai-1", "Milk"))
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        assertTrue(model.adoptAiGrocerySuggestion("ai-1"))
        advanceUntilIdle()

        assertEquals(1, repository.grocery.value.size)
        assertTrue(repository.aiGrocery.value.isEmpty())
    }

    @Test
    fun clearAiGrocery_returnsSnapshotAndRestoreRebuildsReadOnlyList() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.aiGrocery.value = listOf(
            GroceryItem("ai-1", "Spinach"),
            GroceryItem("ai-2", "Eggs"),
        )
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        val snapshot = model.clearAiGrocery()
        advanceUntilIdle()
        assertTrue(repository.aiGrocery.value.isEmpty())

        model.restoreAiGroceryItems(snapshot)
        advanceUntilIdle()

        assertEquals(listOf("Spinach", "Eggs"), repository.aiGrocery.value.map { it.label })
    }

    @Test
    fun runAiAssistant_mealPlanMode_previewsThenApplies() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val ai = FakeNutritionAdviceService()
        val model = nutritionScreenModel(repository, ai, scope = modelScope)

        model.runAiAssistant(
            NutritionAiMode.MealPlan,
            "vegetarian",
            MealPlanGenerationScope.FullWeek,
        )
        advanceUntilIdle()

        assertIs<NutritionAiState.MealPlanPreview>(model.aiState.value)
        model.applyPreviewedMealPlan()
        advanceUntilIdle()

        assertEquals(NutritionAiState.Idle, model.aiState.value)
        assertTrue(repository.mealPlan.value.days.any { it.lunch.isNotBlank() })
    }

    @Test
    fun viewerRole_blocksGroceryWrites() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val householdRepository = FakeHouseholdRepository(role = HouseholdMemberRole.Viewer)
        val model = nutritionScreenModel(
            repository = repository,
            householdRepository = householdRepository,
            scope = modelScope,
        ) { "item-1" }
        advanceUntilIdle()

        assertFalse(model.canWriteHouseholdData.value)
        assertFalse(model.addGroceryItem("Milk"))
        advanceUntilIdle()
        assertTrue(repository.grocery.value.isEmpty())
    }

    @Test
    fun activateHousehold_delegatesToSessionCoordinator() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val session = nutritionSession(repository)
        val model = NutritionScreenModel(
            session = session,
            householdRepository = FakeHouseholdRepository(),
            collaborationRepository = FakeHouseholdCollaborationRepository(),
            aiAssistant = FakeNutritionAdviceService(),
            ghostPairingDismissStore = GroceryGhostPairingDismissStore(MapSettings()),
            scope = modelScope,
        )

        model.activateHousehold("household-99")
        advanceUntilIdle()

        assertEquals("household-99", session.activatedHouseholdId)
    }

    @Test
    fun generateGroceryForMeal_appendsDistinctGroceryItems() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val ai = FakeNutritionAdviceService(mealGroceryLabels = listOf("Garlic", "Pasta"))
        val model = nutritionScreenModel(repository, ai, scope = modelScope) { "meal-g1" }

        model.updateMeal(0, lunch = "Pasta carbonara")
        advanceUntilIdle()
        model.generateGroceryForMeal(0, MealSlot.Lunch, "Monday")
        advanceUntilIdle()

        assertTrue(repository.aiGrocery.value.isEmpty())
        assertEquals(setOf("Garlic", "Pasta"), repository.grocery.value.map { it.label }.toSet())
    }

    @Test
    fun generateGroceryForMeal_skipsDuplicateGroceryLabels() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(GroceryItem("g-1", "Pasta"))
        val ai = FakeNutritionAdviceService(mealGroceryLabels = listOf("Garlic", "Pasta"))
        val model = nutritionScreenModel(repository, ai, scope = modelScope) { "meal-g2" }

        model.updateMeal(0, lunch = "Pasta carbonara")
        advanceUntilIdle()
        model.generateGroceryForMeal(0, MealSlot.Lunch, "Monday")
        advanceUntilIdle()

        assertEquals(2, repository.grocery.value.size)
        assertEquals(1, model.mealGroceryResult.value?.itemCount)
        assertEquals("Garlic", repository.grocery.value.first { it.label == "Garlic" }.label)
    }

    @Test
    fun adoptAllAiGrocerySuggestions_leavesPantryStaplesSeparate() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.aiGrocery.value = listOf(
            GroceryItem("ai-1", "Milk"),
            GroceryItem("ai-2", "Salt", isPantryCheck = true),
        )
        val model = nutritionScreenModel(repository, scope = modelScope) { "grocery-1" }
        advanceUntilIdle()

        model.adoptAllAiGrocerySuggestions()
        advanceUntilIdle()

        assertEquals(1, repository.grocery.value.size)
        assertEquals("Milk", repository.grocery.value.single().label)
        assertEquals(listOf("Salt"), repository.aiGrocery.value.map { it.label })
    }

    @Test
    fun adoptAllAiGrocerySuggestions_movesDistinctItemsToEditableList() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.aiGrocery.value = listOf(
            GroceryItem("ai-1", "Milk"),
            GroceryItem("ai-2", "Eggs"),
        )
        val model = nutritionScreenModel(repository, scope = modelScope) {
            if (repository.grocery.value.isEmpty()) "grocery-1" else "grocery-2"
        }
        advanceUntilIdle()

        model.adoptAllAiGrocerySuggestions()
        advanceUntilIdle()

        assertEquals(setOf("Milk", "Eggs"), repository.grocery.value.map { it.label }.toSet())
        assertTrue(repository.aiGrocery.value.isEmpty())
        assertEquals(2, model.adoptAllGroceryResult.value)
    }

    @Test
    fun adoptAllAiGrocerySuggestions_whenAllDuplicates_reportsZeroAdopted() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(
            GroceryItem("g-1", "Milk"),
            GroceryItem("g-2", "Eggs"),
        )
        repository.aiGrocery.value = listOf(
            GroceryItem("ai-1", "Milk"),
            GroceryItem("ai-2", "Eggs"),
        )
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.adoptAllAiGrocerySuggestions()
        advanceUntilIdle()

        assertEquals(2, repository.grocery.value.size)
        assertTrue(repository.aiGrocery.value.isEmpty())
        assertEquals(0, model.adoptAllGroceryResult.value)
    }

    @Test
    fun copyDinnerToTomorrowLunch_copiesTextToNextDay() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope)
        advanceUntilIdle()

        model.updateMeal(1, dinner = "Leftover stew")
        advanceUntilIdle()
        model.copyDinnerToTomorrowLunch(1)
        advanceUntilIdle()

        assertEquals("Leftover stew", repository.mealPlan.value.days[2].lunch)
    }

    @Test
    fun clearMealPlanWeek_resetsAllDays() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope)
        model.updateMeal(0, lunch = "Soup")
        advanceUntilIdle()

        model.clearMealPlanWeek()
        advanceUntilIdle()

        assertTrue(repository.mealPlan.value.days.all { it.lunch.isBlank() && it.dinner.isBlank() })
    }

    @Test
    fun generateGroceryForAllPlannedMeals_appendsDistinctGroceryItems() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val ai = FakeNutritionAdviceService(mealGroceryLabels = listOf("Garlic", "Pasta"))
        val model = nutritionScreenModel(repository, ai, scope = modelScope)
        model.updateMeal(0, lunch = "Pasta")
        model.updateMeal(0, dinner = "Salad")
        advanceUntilIdle()

        model.generateGroceryForAllPlannedMeals()
        advanceUntilIdle()

        assertTrue(repository.aiGrocery.value.isEmpty())
        assertEquals(setOf("Garlic", "Pasta"), repository.grocery.value.map { it.label }.toSet())
        assertIs<NutritionScreenModel.BulkMealGroceryResult>(model.bulkMealGroceryResult.value)
    }

    @Test
    fun addGroceryItem_showsGhostPairingOfferForTortillas() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope) { "new-item" }
        model.addGroceryItem("Tortillas")
        advanceUntilIdle()

        val offer = model.ghostPairingOffer.value
        assertNotNull(offer)
        assertEquals(3, offer.suggestions.size)
    }

    @Test
    fun acceptGhostPairing_addsSuggestedItems() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope) { "new-item" }
        model.addGroceryItem("Tortillas")
        advanceUntilIdle()

        model.acceptGhostPairing(listOf("Salsa", "Cheese", "Sour cream"))
        advanceUntilIdle()

        assertNull(model.ghostPairingOffer.value)
        val labels = repository.grocery.value.map { it.label }
        assertTrue(labels.contains("Tortillas"))
        assertTrue(labels.contains("Salsa"))
        assertTrue(labels.contains("Cheese"))
        assertTrue(labels.contains("Sour cream"))
    }

    @Test
    fun dismissGhostPairing_preventsRepeatOffer() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val dismissStore = GroceryGhostPairingDismissStore(MapSettings())
        val model = nutritionScreenModel(
            repository,
            scope = modelScope,
            ghostPairingDismissStore = dismissStore,
        ) { "new-item" }

        model.addGroceryItem("Tortillas")
        advanceUntilIdle()
        assertNotNull(model.ghostPairingOffer.value)

        model.dismissGhostPairing()
        advanceUntilIdle()
        assertNull(model.ghostPairingOffer.value)

        model.addGroceryItem("Tacos")
        advanceUntilIdle()
        assertNull(model.ghostPairingOffer.value)
    }
}

private class FakeNutritionRepository(
    override val weekKey: String,
) : NutritionRepository {
    override val householdId: String? = "test-household"
    val grocery = MutableStateFlow<List<GroceryItem>>(emptyList())
    val aiGrocery = MutableStateFlow<List<GroceryItem>>(emptyList())
    val mealPlan = MutableStateFlow(WeeklyMealPlan(weekKey = weekKey))

    override fun observeGroceryItems(): Flow<List<GroceryItem>> = grocery

    override fun observeAiGroceryItems(): Flow<List<GroceryItem>> = aiGrocery

    override fun observeMealPlan(): Flow<WeeklyMealPlan> = mealPlan

    override suspend fun refreshFromRemote() = Unit

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        grocery.value = items
    }

    override suspend fun saveAiGroceryItems(items: List<GroceryItem>) {
        aiGrocery.value = items
    }

    override suspend fun saveMealPlan(plan: WeeklyMealPlan) {
        mealPlan.value = plan
    }
}

private fun nutritionSession(
    repository: FakeNutritionRepository,
): FakeNutritionSessionCoordinator =
    FakeNutritionSessionCoordinator(repository)

private fun nutritionScreenModel(
    repository: FakeNutritionRepository,
    advice: NutritionAiAssistantService = FakeNutritionAdviceService(),
    householdRepository: FakeHouseholdRepository = FakeHouseholdRepository(),
    collaborationRepository: FakeHouseholdCollaborationRepository = FakeHouseholdCollaborationRepository(),
    ghostPairingDismissStore: GroceryGhostPairingDismissStore = GroceryGhostPairingDismissStore(MapSettings()),
    scope: CoroutineScope,
    newItemId: () -> String = { "item-1" },
): NutritionScreenModel =
    NutritionScreenModel(
        session = nutritionSession(repository),
        householdRepository = householdRepository,
        collaborationRepository = collaborationRepository,
        aiAssistant = advice,
        ghostPairingDismissStore = ghostPairingDismissStore,
        scope = scope,
        newItemId = newItemId,
    )

private class FakeNutritionAdviceService(
    private val answer: String = "Advice",
    private val groceryLabels: List<String> = listOf("Oats", "Bananas"),
    private val mealGroceryLabels: List<String> = listOf("Lemon", "Herbs"),
    private val shouldFail: Boolean = false,
    private val failureMessage: String = "error",
) : NutritionAiAssistantService {
    override suspend fun askAdvice(question: String): Result<String> {
        return if (shouldFail) {
            Result.failure(IllegalArgumentException(failureMessage))
        } else {
            Result.success(answer)
        }
    }

    override suspend fun generateGroceryList(criteria: String): Result<List<String>> {
        return if (criteria.isBlank()) {
            Result.failure(IllegalArgumentException("empty_criteria"))
        } else {
            Result.success(groceryLabels)
        }
    }

    override suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>> {
        return if (mealDescription.isBlank()) {
            Result.failure(IllegalArgumentException("empty_meal"))
        } else {
            Result.success(mealGroceryLabels)
        }
    }

    override suspend fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): Result<NutritionAiPlanner.MealPlanGeneration> {
        return if (criteria.isBlank()) {
            Result.failure(IllegalArgumentException("empty_criteria"))
        } else {
            Result.success(
                NutritionAiPlanner.generateMealPlan(criteria, scope, currentPlan),
            )
        }
    }
}
