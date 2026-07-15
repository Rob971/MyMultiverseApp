package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService

/**
 * Enhances [LocalNutritionAiAssistantService] by delegating [generateGroceryForMeal]
 * to the Gemini API for dish-specific, language-aware ingredient lists.
 *
 * If Gemini is unavailable (network error, empty response, or parse failure),
 * this service falls back transparently to the local heuristic implementation.
 * All other operations (advice, grocery list by criteria, meal plan) continue to
 * use the local service unchanged.
 */
internal class RemoteNutritionAiAssistantService(
    private val local: LocalNutritionAiAssistantService,
    private val geminiClient: DishIngredientClient,
    private val currentLanguageCode: () -> String,
) : NutritionAiAssistantService {

    override suspend fun askAdvice(question: String): Result<String> =
        local.askAdvice(question)

    override suspend fun generateGroceryList(criteria: String): Result<List<String>> =
        local.generateGroceryList(criteria)

    override suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>> {
        if (mealDescription.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_meal"))
        }
        val trimmed = mealDescription.trim()

        // Attempt Gemini remote generation first
        val remoteIngredients = runCatching {
            geminiClient.generateIngredients(
                dish = trimmed,
                languageCode = currentLanguageCode(),
            ).getOrNull()
        }.getOrNull()

        if (!remoteIngredients.isNullOrEmpty()) {
            return Result.success(remoteIngredients)
        }

        // Fall back to local heuristics when Gemini fails or returns nothing
        return local.generateGroceryForMeal(mealDescription)
    }

    override suspend fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): Result<NutritionAiPlanner.MealPlanGeneration> =
        local.generateMealPlan(criteria, scope, currentPlan)
}
