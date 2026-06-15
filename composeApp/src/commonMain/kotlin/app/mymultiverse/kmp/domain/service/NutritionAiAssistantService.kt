package app.mymultiverse.kmp.domain.service

import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.NutritionAiPlanner

interface NutritionAiAssistantService {
    suspend fun askAdvice(question: String): Result<String>

    suspend fun generateGroceryList(criteria: String): Result<List<String>>

    suspend fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): Result<NutritionAiPlanner.MealPlanGeneration>

    suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>>
}

/** @deprecated Use [NutritionAiAssistantService] */
typealias NutritionAdviceService = NutritionAiAssistantService
