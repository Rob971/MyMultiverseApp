package app.mymultiverse.kmp.ui

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class InstrumentedNutritionRepository(
    override val weekKey: String,
    override val spaceId: String? = null,
) : NutritionRepository {
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

class InstrumentedNutritionSessionCoordinator(
    repository: InstrumentedNutritionRepository,
) : NutritionSessionCoordinator {
    private val _nutrition = MutableStateFlow<NutritionRepository>(repository)

    var activatedSpaceId: String? = null
        private set

    override val nutrition = _nutrition.asStateFlow()

    override fun observeSyncStatus(): Flow<NutritionSyncStatus> = flowOf(NutritionSyncStatus.Idle)

    override suspend fun activateSpace(spaceId: String) {
        activatedSpaceId = spaceId
    }

    override fun deactivate() = Unit
}

class InstrumentedNutritionAdviceService(
    private val answer: String = "Eat more vegetables.",
) : NutritionAiAssistantService {
    override suspend fun askAdvice(question: String): Result<String> {
        return if (question.isBlank()) {
            Result.failure(IllegalArgumentException("empty_question"))
        } else {
            Result.success(answer)
        }
    }

    override suspend fun generateGroceryList(criteria: String): Result<List<String>> {
        return if (criteria.isBlank()) {
            Result.failure(IllegalArgumentException("empty_criteria"))
        } else {
            Result.success(listOf("Spinach", "Eggs", "Brown rice"))
        }
    }

    override suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>> {
        return if (mealDescription.isBlank()) {
            Result.failure(IllegalArgumentException("empty_meal"))
        } else {
            Result.success(listOf("Tomatoes", "Olive oil", "Basil"))
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
