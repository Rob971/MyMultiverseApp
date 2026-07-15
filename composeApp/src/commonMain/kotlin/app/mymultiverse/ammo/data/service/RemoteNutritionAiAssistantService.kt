package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import kotlinx.coroutines.flow.StateFlow

/**
 * Enhances [LocalNutritionAiAssistantService] by delegating [generateGroceryForMeal]
 * to the Gemini API for dish-specific, language-aware ingredient lists.
 *
 * When the Gemini API key is blank (not yet configured by the user), or when Gemini is
 * unavailable (network error, empty response, or parse failure), this service falls back
 * transparently to the local heuristic implementation.
 *
 * All other operations (advice, grocery list by criteria, meal plan) continue to use the
 * local service unchanged.
 */
internal class RemoteNutritionAiAssistantService(
    private val local: LocalNutritionAiAssistantService,
    private val geminiClient: DishIngredientClient,
    private val currentLanguageCode: () -> String,
    private val aiSettings: AiAssistantSettings,
) : NutritionAiAssistantService {

    /** Exposes the live Gemini API key so the UI can reactively show AI setup notices. */
    override val geminiApiKey: StateFlow<String>
        get() = aiSettings.geminiApiKey

    override suspend fun askAdvice(question: String): Result<String> =
        local.askAdvice(question)

    override suspend fun generateGroceryList(criteria: String): Result<List<String>> =
        local.generateGroceryList(criteria)

    override suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>> {
        if (mealDescription.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_meal"))
        }
        val trimmed = mealDescription.trim()

        // Skip Gemini immediately if no key is configured — no network call needed.
        if (aiSettings.geminiApiKey.value.isBlank()) {
            return local.generateGroceryForMeal(mealDescription)
        }

        // Try Gemini; fall back to local on any network or parse error.
        val remoteIngredients = runCatching {
            geminiClient.generateIngredients(
                dish = trimmed,
                languageCode = currentLanguageCode(),
            ).getOrNull()
        }.getOrNull()

        if (!remoteIngredients.isNullOrEmpty()) {
            return Result.success(remoteIngredients)
        }
        return local.generateGroceryForMeal(mealDescription)
    }

    override suspend fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): Result<NutritionAiPlanner.MealPlanGeneration> =
        local.generateMealPlan(criteria, scope, currentPlan)
}
