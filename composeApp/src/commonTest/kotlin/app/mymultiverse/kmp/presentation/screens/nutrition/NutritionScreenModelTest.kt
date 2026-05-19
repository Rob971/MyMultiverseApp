package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.service.NutritionAdviceService
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
        val model = NutritionScreenModel(repository, FakeNutritionAdviceService(), modelScope) { "item-1" }

        model.addGroceryItem("  Olive oil  ")
        advanceUntilIdle()

        assertEquals(listOf(GroceryItem("item-1", "Olive oil", false)), repository.grocery.value)
    }

    @Test
    fun addGroceryItem_ignoresBlankInput() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = NutritionScreenModel(repository, FakeNutritionAdviceService(), modelScope)

        model.addGroceryItem("   ")
        advanceUntilIdle()

        assertTrue(repository.grocery.value.isEmpty())
    }

    @Test
    fun toggleGroceryItem_flipsCheckedState() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(GroceryItem("1", "Rice", false))
        val model = NutritionScreenModel(repository, FakeNutritionAdviceService(), modelScope)
        advanceUntilIdle()

        model.toggleGroceryItem("1")
        advanceUntilIdle()

        assertTrue(repository.grocery.value.single().isChecked)
    }

    @Test
    fun removeGroceryItem_deletesItem() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        repository.grocery.value = listOf(
            GroceryItem("1", "Rice", false),
            GroceryItem("2", "Beans", false),
        )
        val model = NutritionScreenModel(repository, FakeNutritionAdviceService(), modelScope)
        advanceUntilIdle()

        model.removeGroceryItem("1")
        advanceUntilIdle()

        assertEquals("2", repository.grocery.value.single().id)
    }

    @Test
    fun updateMeal_updatesLunchAndDinnerForDay() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = NutritionScreenModel(repository, FakeNutritionAdviceService(), modelScope)
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
        val model = NutritionScreenModel(repository, FakeNutritionAdviceService(), modelScope)
        advanceUntilIdle()

        model.updateMeal(dayIndex = 99, lunch = "Ignored")
        advanceUntilIdle()

        assertEquals(DayMeals(), repository.mealPlan.value.days.first())
    }

    @Test
    fun askNutritionAdvice_successSetsAnswerState() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val advice = FakeNutritionAdviceService(answer = "Eat more vegetables.")
        val model = NutritionScreenModel(repository, advice, modelScope)

        model.askNutritionAdvice("Vegetables?")
        advanceUntilIdle()

        assertIs<NutritionAiState.Answer>(model.aiState.value)
        assertEquals("Eat more vegetables.", (model.aiState.value as NutritionAiState.Answer).text)
    }

    @Test
    fun askNutritionAdvice_failureSetsErrorState() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val advice = FakeNutritionAdviceService(shouldFail = true, failureMessage = "empty_question")
        val model = NutritionScreenModel(repository, advice, modelScope)

        model.askNutritionAdvice("")
        advanceUntilIdle()

        assertIs<NutritionAiState.Error>(model.aiState.value)
        assertEquals("empty_question", (model.aiState.value as NutritionAiState.Error).message)
    }

    @Test
    fun resetAiState_returnsToIdle() = runTest(testDispatcher) {
        val repository = FakeNutritionRepository(weekKey)
        val model = NutritionScreenModel(repository, FakeNutritionAdviceService(), modelScope)
        model.askNutritionAdvice("Protein?")
        advanceUntilIdle()

        model.resetAiState()

        assertEquals(NutritionAiState.Idle, model.aiState.value)
    }
}

private class FakeNutritionRepository(
    override val weekKey: String,
) : NutritionRepository {
    val grocery = MutableStateFlow<List<GroceryItem>>(emptyList())
    val mealPlan = MutableStateFlow(WeeklyMealPlan(weekKey = weekKey))

    override fun observeGroceryItems(): Flow<List<GroceryItem>> = grocery

    override fun observeMealPlan(): Flow<WeeklyMealPlan> = mealPlan

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        grocery.value = items
    }

    override suspend fun saveMealPlan(plan: WeeklyMealPlan) {
        mealPlan.value = plan
    }
}

private class FakeNutritionAdviceService(
    private val answer: String = "Advice",
    private val shouldFail: Boolean = false,
    private val failureMessage: String = "error",
) : NutritionAdviceService {
    override suspend fun ask(question: String): Result<String> {
        return if (shouldFail) {
            Result.failure(IllegalArgumentException(failureMessage))
        } else {
            Result.success(answer)
        }
    }
}
