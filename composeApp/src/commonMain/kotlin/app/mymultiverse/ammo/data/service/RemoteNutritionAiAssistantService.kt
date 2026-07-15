package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.domain.service.AiKeyNotConfiguredException
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import kotlinx.coroutines.flow.StateFlow

/**
 * Enhances [LocalNutritionAiAssistantService] with Gemini-powered AI features.
 *
 * **When key is not configured:** every method returns
 * [AiKeyNotConfiguredException] so the caller can show a polite setup prompt
 * rather than a generic error or a silent local fallback.
 *
 * **When key is configured:**
 * - [generateGroceryForMeal] calls Gemini for dish-specific, language-aware
 *   ingredients; falls back to local heuristics if the network request fails.
 * - [askAdvice], [generateGroceryList], [generateMealPlan] delegate to
 *   [LocalNutritionAiAssistantService] (their local implementations are
 *   already high-quality without a remote call).
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

    override suspend fun askAdvice(question: String): Result<String> {
        if (question.isBlank()) return Result.failure(IllegalArgumentException("empty_question"))
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())
        return local.askAdvice(question)
    }

    override suspend fun generateGroceryList(criteria: String): Result<List<String>> {
        if (criteria.isBlank()) return Result.failure(IllegalArgumentException("empty_criteria"))
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())
        return local.generateGroceryList(criteria)
    }

    override suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>> {
        if (mealDescription.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_meal"))
        }
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())

        val trimmed = mealDescription.trim()

        // Try Gemini; fall back to local heuristics only when the key IS set but network fails.
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
    ): Result<NutritionAiPlanner.MealPlanGeneration> {
        if (criteria.isBlank()) return Result.failure(IllegalArgumentException("empty_criteria"))
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())
        return local.generateMealPlan(criteria, scope, currentPlan)
    }

    private fun keyMissing(): Boolean = aiSettings.geminiApiKey.value.isBlank()
}
