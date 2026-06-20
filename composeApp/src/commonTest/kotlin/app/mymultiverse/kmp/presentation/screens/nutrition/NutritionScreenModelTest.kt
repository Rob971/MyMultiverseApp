package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
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
    fun addGroceryItem_rejectsDuplicateLabels() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = nutritionScreenModel(repository, scope = modelScope) { "item-1" }

        assertTrue(model.addGroceryItem("Milk"))
        advanceUntilIdle()
        assertFalse(model.addGroceryItem(" milk "))
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
            aiAssistant = FakeNutritionAdviceService(),
            scope = modelScope,
        )

        model.activateHousehold("household-99")
        advanceUntilIdle()

        assertEquals("household-99", session.activatedHouseholdId)
    }

    @Test
    fun generateGroceryForMeal_appendsDistinctAiGrocery() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val ai = FakeNutritionAdviceService(mealGroceryLabels = listOf("Garlic", "Pasta"))
        val model = nutritionScreenModel(repository, ai, scope = modelScope) { "meal-g1" }

        model.updateMeal(0, lunch = "Pasta carbonara")
        advanceUntilIdle()
        model.generateGroceryForMeal(0, MealSlot.Lunch, "Monday")
        advanceUntilIdle()

        assertEquals(2, repository.aiGrocery.value.size)
        assertEquals("Garlic", repository.aiGrocery.value.first().label)
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
    scope: CoroutineScope,
    newItemId: () -> String = { "item-1" },
): NutritionScreenModel =
    NutritionScreenModel(
        session = nutritionSession(repository),
        householdRepository = householdRepository,
        aiAssistant = advice,
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
