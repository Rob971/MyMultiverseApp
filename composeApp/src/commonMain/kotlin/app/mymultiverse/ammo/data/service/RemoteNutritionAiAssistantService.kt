package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import kotlinx.coroutines.CancellationException
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.domain.service.AiKeyNotConfiguredException
import app.mymultiverse.ammo.domain.service.GeminiApiException
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
 * language-aware prompts. [generateMealPlan] surfaces Gemini failures to the UI
 * (no silent local fallback) so previews always reflect a real model response.
 * Other methods still fall back to [LocalNutritionAiAssistantService] when Gemini
 * fails at the network or parse level.
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
                if (e is CancellationException) throw e  // Never recover from cancellation
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

        val items = try {
            geminiApi.complete(prompt, maxOutputTokens = 512).getOrThrow()
                .let { GeminiResponseParser.extractJsonArray(it) }
        } catch (e: CancellationException) {
            throw e  // Never treat cancellation as "no items found"
        } catch (_: Exception) {
            null
        }

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
            if (failure is CancellationException) throw failure  // Propagate cancellation
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
        val prompt = buildMealPlanPrompt(criteria, lang, scope)

        val geminiText = geminiApi.complete(prompt, maxOutputTokens = 1024, temperature = 0.7)
            .getOrElse { error ->
                if (error is CancellationException) throw error
                return recordMealPlanFailureAndFail(
                    stage = "api_call",
                    error = error,
                    criteria = criteria,
                    lang = lang,
                    scope = scope,
                )
            }

        val (generatedDays, summary) = try {
            GeminiResponseParser.parseMealPlan(geminiText)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return recordMealPlanFailureAndFail(
                stage = "parse",
                error = GeminiApiException(GeminiApiException.Reason.PARSE_ERROR, cause = e),
                criteria = criteria,
                lang = lang,
                scope = scope,
            )
        }

        if (generatedDays.isEmpty()) {
            return recordMealPlanFailureAndFail(
                stage = "empty_response",
                error = GeminiApiException(GeminiApiException.Reason.EMPTY_RESPONSE),
                criteria = criteria,
                lang = lang,
                scope = scope,
            )
        }

        val mergedDays = mergeDays(generatedDays, scope, currentPlan)
        appLogger.breadcrumb("gemini_meal_plan_ok criteria=$criteria days=${generatedDays.size}")
        return Result.success(NutritionAiPlanner.MealPlanGeneration(days = mergedDays, summary = summary))
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun recordMealPlanFailureAndFail(
        stage: String,
        error: Throwable,
        criteria: String,
        lang: String,
        scope: MealPlanGenerationScope,
    ): Result<NutritionAiPlanner.MealPlanGeneration> {
        val geminiError = error as? GeminiApiException
            ?: GeminiApiException(GeminiApiException.Reason.NETWORK, cause = error)
        appLogger.recordError(
            tag = TAG,
            message = "Gemini meal plan $stage failed",
            throwable = geminiError,
            context = buildMap {
                put("stage", stage)
                put("criteria", criteria.take(120))
                put("lang", lang)
                put("scope", scope::class.simpleName.orEmpty())
                geminiError.httpStatus?.let { put("http_status", it.toString()) }
            },
        )
        return Result.failure(geminiError)
    }

    private fun keyMissing(): Boolean = aiSettings.geminiApiKey.value.isBlank()

    private fun buildMealPlanPrompt(
        criteria: String,
        languageName: String,
        scope: MealPlanGenerationScope,
    ): String {
        val daySpec = when (scope) {
            is MealPlanGenerationScope.FullWeek ->
                "Generate a 7-day meal plan (each day has lunch and dinner)"
            is MealPlanGenerationScope.SingleDay ->
                "Generate lunch and dinner for 1 day only"
            is MealPlanGenerationScope.SingleMeal -> {
                val meal = when (scope.slot) {
                    MealSlot.Lunch -> "lunch"
                    MealSlot.Dinner -> "dinner"
                }
                "Generate only the $meal for 1 day. Set the other meal slot to an empty string \"\"."
            }
        }
        return "$daySpec based on this exact user request: \"${criteria.escapeJson()}\". " +
            "Follow the request closely — dish names must match the user's ingredients and style. " +
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
