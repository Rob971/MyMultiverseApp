package app.mymultiverse.ammo.domain.service

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface NutritionAiAssistantService {
    /**
     * The currently active Gemini API key, or an empty string when operating in
     * local-only mode. Implementations that support remote AI override this with
     * the live key from user settings so the UI can reactively show setup notices.
     */
    val geminiApiKey: StateFlow<String> get() = NO_KEY

    suspend fun askAdvice(question: String): Result<String>

    suspend fun generateGroceryList(criteria: String): Result<List<String>>

    suspend fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): Result<NutritionAiPlanner.MealPlanGeneration>

    suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>>

    companion object {
        /** Shared empty-key sentinel used by local-only implementations. */
        val NO_KEY: StateFlow<String> = MutableStateFlow("")
    }
}

/** @deprecated Use [NutritionAiAssistantService] */
typealias NutritionAdviceService = NutritionAiAssistantService
