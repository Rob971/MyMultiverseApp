package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.domain.service.AiKeyNotConfiguredException
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import kotlinx.coroutines.flow.StateFlow

/**
 * Gemini-powered implementation of [NutritionAiAssistantService].
 *
 * **When key is not configured:** every method returns [AiKeyNotConfiguredException]
 * so the UI can show a polite setup prompt.
 *
 * **When key is configured:** all four methods call Gemini 2.0 Flash Lite with
 * language-aware prompts and fall back to [LocalNutritionAiAssistantService] only
 * when Gemini fails at the network or parse level.
 *
 * Failures and successes are recorded via [AppLogger] so they appear in
 * Firebase Crashlytics breadcrumbs and non-fatal events.
 */
internal class RemoteNutritionAiAssistantService(
    private val local: LocalNutritionAiAssistantService,
    private val geminiClient: DishIngredientClient,
    private val geminiApi: GeminiTextClient,
    private val currentLanguageCode: () -> String,
    private val aiSettings: AiAssistantSettings,
    private val appLogger: AppLogger,
) : NutritionAiAssistantService {

    private companion object {
        const val TAG = "GeminiAI"
    }

    override val geminiApiKey: StateFlow<String>
        get() = aiSettings.geminiApiKey

    // ── askAdvice ────────────────────────────────────────────────────────────

    override suspend fun askAdvice(question: String): Result<String> {
        if (question.isBlank()) return Result.failure(IllegalArgumentException("empty_question"))
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())

        val lang = GeminiResponseParser.languageNameFor(currentLanguageCode())
        val prompt = "You are a friendly nutrition assistant. " +
            "Answer this question in 2-4 practical sentences: \"${question.escapeJson()}\". " +
            "Reply in $lang only."

        return geminiApi.complete(prompt, maxOutputTokens = 512)
            .mapCatching { text ->
                val advice = GeminiResponseParser.parseAdviceText(text)
                check(advice.isNotBlank()) { "Empty advice" }
                appLogger.breadcrumb("gemini_advice_ok lang=$lang")
                advice
            }.recoverCatching { e ->
                appLogger.recordError(TAG, "Gemini advice failed; using local fallback", e,
                    mapOf("lang" to lang))
                local.askAdvice(question).getOrThrow()
            }
    }

    // ── generateGroceryList ──────────────────────────────────────────────────

    override suspend fun generateGroceryList(criteria: String): Result<List<String>> {
        if (criteria.isBlank()) return Result.failure(IllegalArgumentException("empty_criteria"))
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())

        val lang = GeminiResponseParser.languageNameFor(currentLanguageCode())
        val prompt = "You are a nutrition assistant. Generate a grocery shopping list for: " +
            "\"${criteria.escapeJson()}\". Reply in $lang only. " +
            "Return ONLY a JSON array of 8 to 14 food item names, no explanation. " +
            "Example: [\"item1\",\"item2\"]"

        val items = runCatching {
            geminiApi.complete(prompt, maxOutputTokens = 512).getOrThrow()
                .let { GeminiResponseParser.extractJsonArray(it) }
        }.getOrNull()

        if (!items.isNullOrEmpty()) {
            appLogger.breadcrumb("gemini_grocery_list_ok criteria=$criteria count=${items.size}")
            return Result.success(items)
        }
        return local.generateGroceryList(criteria)
    }

    // ── generateGroceryForMeal ───────────────────────────────────────────────

    override suspend fun generateGroceryForMeal(mealDescription: String): Result<List<String>> {
        if (mealDescription.isBlank()) {
            return Result.failure(IllegalArgumentException("empty_meal"))
        }
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())

        val trimmed = mealDescription.trim()
        val lang = currentLanguageCode()

        val geminiResult = runCatching {
            geminiClient.generateIngredients(dish = trimmed, languageCode = lang)
        }
        val remoteIngredients = geminiResult.getOrNull()?.getOrNull()

        if (!remoteIngredients.isNullOrEmpty()) {
            appLogger.breadcrumb("gemini_ingredients_ok dish=$trimmed lang=$lang count=${remoteIngredients.size}")
            return Result.success(remoteIngredients)
        }

        val failure = geminiResult.exceptionOrNull() ?: geminiResult.getOrNull()?.exceptionOrNull()
        if (failure != null) {
            appLogger.recordError(TAG, "Gemini ingredient call failed; using local fallback",
                failure, mapOf("dish" to trimmed, "lang" to lang))
        } else {
            appLogger.breadcrumb("gemini_ingredients_empty dish=$trimmed lang=$lang")
        }
        return local.generateGroceryForMeal(mealDescription)
    }

    // ── generateMealPlan ─────────────────────────────────────────────────────

    override suspend fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): Result<NutritionAiPlanner.MealPlanGeneration> {
        if (criteria.isBlank()) return Result.failure(IllegalArgumentException("empty_criteria"))
        if (keyMissing()) return Result.failure(AiKeyNotConfiguredException())

        val lang = GeminiResponseParser.languageNameFor(currentLanguageCode())
        val daysNeeded = if (scope is MealPlanGenerationScope.FullWeek) 7 else 1
        val prompt = buildMealPlanPrompt(criteria, lang, daysNeeded)

        val geminiText = runCatching {
            geminiApi.complete(prompt, maxOutputTokens = 1024, temperature = 0.7).getOrThrow()
        }.getOrElse { e ->
            appLogger.recordError(TAG, "Gemini meal plan call failed; using local fallback", e,
                mapOf("criteria" to criteria, "lang" to lang, "scope" to scope::class.simpleName.orEmpty()))
            return local.generateMealPlan(criteria, scope, currentPlan)
        }

        val (generatedDays, summary) = runCatching {
            GeminiResponseParser.parseMealPlan(geminiText)
        }.getOrElse { e ->
            appLogger.recordError(TAG, "Gemini meal plan parse failed; using local fallback", e,
                mapOf("criteria" to criteria, "lang" to lang))
            return local.generateMealPlan(criteria, scope, currentPlan)
        }

        if (generatedDays.isEmpty()) {
            appLogger.breadcrumb("gemini_meal_plan_empty criteria=$criteria")
            return local.generateMealPlan(criteria, scope, currentPlan)
        }

        val mergedDays = mergeDays(generatedDays, scope, currentPlan)
        appLogger.breadcrumb("gemini_meal_plan_ok criteria=$criteria days=${generatedDays.size}")
        return Result.success(NutritionAiPlanner.MealPlanGeneration(days = mergedDays, summary = summary))
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun keyMissing(): Boolean = aiSettings.geminiApiKey.value.isBlank()

    private fun buildMealPlanPrompt(criteria: String, languageName: String, daysNeeded: Int): String {
        val daySpec = if (daysNeeded == 1) {
            "Generate 1 day (lunch and dinner) of meal plan"
        } else {
            "Generate a $daysNeeded-day meal plan (each day has lunch and dinner)"
        }
        return "$daySpec based on: \"${criteria.escapeJson()}\". " +
            "Reply in $languageName only. Use dish names typical of $languageName cuisine when appropriate. " +
            "Return ONLY valid JSON — no markdown, no explanation — in this exact format:\n" +
            "{\"days\":[{\"lunch\":\"...\",\"dinner\":\"...\"}],\"summary\":\"Brief one-sentence description\"}"
    }

    private fun mergeDays(
        generated: List<DayMeals>,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
    ): List<DayMeals> {
        val total = WeeklyMealPlan.DAYS_IN_WEEK
        return when (scope) {
            is MealPlanGenerationScope.FullWeek ->
                List(total) { i -> generated.getOrElse(i) { DayMeals() } }

            is MealPlanGenerationScope.SingleDay -> {
                val idx = scope.dayIndex.coerceIn(0, total - 1)
                currentPlan.days.toMutableList().also { it[idx] = generated.firstOrNull() ?: DayMeals() }
            }

            is MealPlanGenerationScope.SingleMeal -> {
                val idx = scope.dayIndex.coerceIn(0, total - 1)
                val ai = generated.firstOrNull() ?: DayMeals()
                currentPlan.days.toMutableList().also { days ->
                    days[idx] = when (scope.slot) {
                        MealSlot.Lunch -> days[idx].copy(lunch = ai.lunch.ifBlank { ai.dinner })
                        MealSlot.Dinner -> days[idx].copy(dinner = ai.dinner.ifBlank { ai.lunch })
                    }
                }
            }
        }
    }

    private fun String.escapeJson(): String = buildString {
        for (ch in this@escapeJson) {
            when (ch) {
                '\\' -> append("\\\\"); '"' -> append("\\\"")
                '\n' -> append("\\n"); '\r' -> append("\\r"); '\t' -> append("\\t")
                else -> append(ch)
            }
        }
    }
}
