package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.data.observability.AppLogger
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
    private val appLogger: AppLogger,
) : NutritionAiAssistantService {

    private companion object {
        const val TAG = "GeminiIngredients"
    }

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
        val lang = currentLanguageCode()

        // Try Gemini; fall back to local heuristics when the key IS set but the call fails.
        val geminiResult = runCatching {
            geminiClient.generateIngredients(dish = trimmed, languageCode = lang)
        }

        val remoteIngredients = geminiResult.getOrNull()?.getOrNull()

        if (!remoteIngredients.isNullOrEmpty()) {
            appLogger.breadcrumb("gemini_ingredients_ok dish=$trimmed lang=$lang count=${remoteIngredients.size}")
            return Result.success(remoteIngredients)
        }

        // Log the reason for the fallback to Crashlytics so the team can see
        // whether failures are transient (network) or systematic (API change).
        val failure = geminiResult.exceptionOrNull()
            ?: geminiResult.getOrNull()?.exceptionOrNull()
        if (failure != null) {
            appLogger.recordError(
                tag = TAG,
                message = "Gemini ingredient call failed; using local fallback",
                throwable = failure,
                context = mapOf("dish" to trimmed, "lang" to lang),
            )
        } else {
            // Gemini returned success but an empty list — unexpected, worth a breadcrumb.
            appLogger.breadcrumb("gemini_ingredients_empty dish=$trimmed lang=$lang")
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
