package app.mymultiverse.kmp.data.service

import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.NutritionAdviceBuilder
import app.mymultiverse.kmp.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
import kotlinx.coroutines.delay

class LocalNutritionAiAssistantService(
    private val responseDelayMs: Long = DEFAULT_DELAY_MS,
) : NutritionAiAssistantService {

    override suspend fun askAdvice(question: String): Result<String> {
        if (question.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_question"))
        }
        delayIfNeeded()
        return Result.success(NutritionAdviceBuilder.buildAdvice(question.trim()))
    }

    override suspend fun generateGroceryList(criteria: String): Result<List<String>> {
        if (criteria.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_criteria"))
        }
        delayIfNeeded()
        val items = NutritionAiPlanner.generateGroceryList(criteria.trim())
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("empty_result"))
        }
        return Result.success(items)
    }

    override suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>> {
        if (mealDescription.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_meal"))
        }
        delayIfNeeded()
        val items = NutritionAiPlanner.generateGroceryForMeal(mealDescription.trim())
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("empty_result"))
        }
        return Result.success(items)
    }

    override suspend fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): Result<NutritionAiPlanner.MealPlanGeneration> {
        if (criteria.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_criteria"))
        }
        if (scope is MealPlanGenerationScope.SingleDay &&
            scope.dayIndex !in 0 until WeeklyMealPlan.DAYS_IN_WEEK
        ) {
            return Result.failure(IllegalArgumentException("invalid_day"))
        }
        delayIfNeeded()
        return Result.success(
            NutritionAiPlanner.generateMealPlan(criteria.trim(), scope, currentPlan),
        )
    }

    private suspend fun delayIfNeeded() {
        if (responseDelayMs > 0) delay(responseDelayMs)
    }

    companion object {
        const val DEFAULT_DELAY_MS = 700L
    }
}

/** @deprecated Use [LocalNutritionAiAssistantService] */
typealias LocalNutritionAdviceService = LocalNutritionAiAssistantService
