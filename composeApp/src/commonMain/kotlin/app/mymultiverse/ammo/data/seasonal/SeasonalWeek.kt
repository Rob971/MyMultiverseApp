package app.mymultiverse.ammo.data.seasonal

import app.mymultiverse.ammo.data.service.GeminiResponseParser
import app.mymultiverse.ammo.data.service.GeminiTextClient
import app.mymultiverse.ammo.domain.location.DeviceRegionService
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalCatalog
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalFoodCategory
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.model.nutrition.favoriteKeyFor
import app.mymultiverse.ammo.domain.nutrition.NutritionAiPlanner
import app.mymultiverse.ammo.domain.nutrition.SeasonalWeekSuggester
import app.mymultiverse.ammo.domain.repository.FavoriteDishesRepository
import app.mymultiverse.ammo.domain.service.GeminiApiException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class SeasonalMealDto(val dish: String = "", val foods: List<String> = emptyList())

@Serializable
internal data class SeasonalDayDto(val lunch: SeasonalMealDto = SeasonalMealDto(), val dinner: SeasonalMealDto = SeasonalMealDto())

@Serializable
internal data class SeasonalWeekDto(val days: List<SeasonalDayDto> = emptyList(), val summary: String = "")

internal data class SeasonalMeal(val dish: String, val foodIds: List<String>)

internal data class SeasonalWeekDraft(val days: List<Pair<SeasonalMeal, SeasonalMeal>>, val summary: String) {
    val meals: List<SeasonalMeal> get() = days.flatMap { listOf(it.first, it.second) }

    fun toGeneration() = NutritionAiPlanner.MealPlanGeneration(
        days = days.map { DayMeals(lunch = it.first.dish, dinner = it.second.dish) },
        summary = summary,
    )
}

internal object SeasonalWeekPrompt {
    fun build(catalog: SeasonalCatalog, favorites: List<String>, alreadyPlanned: List<String>, languageCode: String): String =
        buildString {
            append(GeminiResponseParser.languagePromptDirective(languageCode))
            append(" Suggest a healthy, varied week of home cooking: 7 lunches and 7 dinners, 14 different dishes, ")
            append("built mostly from these in-season foods (id: name, category):\n")
            catalog.foods.forEach { append("- ${it.id}: ${it.name} (${it.category.name.lowercase()})\n") }
            append("Use at least ${SeasonalWeekValidator.MIN_VEGETABLES} different vegetables from the list across the week. ")
            append("Never repeat a dish.\n")
            if (favorites.isNotEmpty()) {
                append("The user's favorite dishes: ${favorites.joinToString("; ")}. ")
                append("Include at most ${SeasonalWeekValidator.MAX_FAVORITES} of them.\n")
            }
            if (alreadyPlanned.isNotEmpty()) {
                append("Already planned this week, do not repeat: ${alreadyPlanned.joinToString("; ")}.\n")
            }
            append("Return ONLY JSON: {\"days\":[{\"lunch\":{\"dish\":\"...\",\"foods\":[\"id\"]},")
            append("\"dinner\":{\"dish\":\"...\",\"foods\":[\"id\"]}}],\"summary\":\"...\"} with exactly 7 days, Monday first. ")
            append("dish is the dish name in the user's language, foods lists the ids of the in-season foods it uses, ")
            append("summary is one short sentence.")
        }

    fun retry(previousPrompt: String, problems: List<String>): String =
        previousPrompt + "\nYour previous answer broke these rules: " + problems.joinToString("; ") +
            ". Answer again and follow every rule."
}

private val weekJson = Json { ignoreUnknownKeys = true; isLenient = true }

internal object SeasonalWeekParser {
    /** The week in a Gemini reply, tolerating fences and prose around the JSON object. */
    fun parse(text: String): Result<SeasonalWeekDraft> = runCatching {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        require(start in 0 until end) { "seasonal_week_no_json" }
        val dto = weekJson.decodeFromString<SeasonalWeekDto>(text.substring(start, end + 1))
        fun SeasonalMealDto.clean() = SeasonalMeal(dish.trim(), foods.map { it.trim().lowercase() })
        SeasonalWeekDraft(dto.days.map { it.lunch.clean() to it.dinner.clean() }, dto.summary.trim())
    }
}

internal object SeasonalWeekValidator {
    const val DAYS = 7
    const val MIN_VEGETABLES = 7
    const val MAX_FAVORITES = 2

    /** A week the preview can show: 7 days, each with a lunch and a dinner. */
    fun isComplete(draft: SeasonalWeekDraft): Boolean =
        draft.days.size == DAYS && draft.meals.all { it.dish.isNotBlank() }

    /** Every rule the week breaks, phrased for the model; empty when it follows them all. */
    fun problems(draft: SeasonalWeekDraft, catalog: SeasonalCatalog, favoriteKeys: Set<String>): List<String> {
        val problems = mutableListOf<String>()
        if (draft.days.size != DAYS) problems += "it must have exactly $DAYS days, it had ${draft.days.size}"
        if (draft.meals.any { it.dish.isBlank() }) problems += "every lunch and dinner needs a dish"
        val keys = draft.meals.map { favoriteKeyFor(it.dish) }.filter { it.isNotEmpty() }
        val repeated = keys.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (repeated.isNotEmpty()) problems += "these dishes were repeated: ${repeated.joinToString()}"
        val vegetables = catalog.foods.filter { it.category == SeasonalFoodCategory.VEGETABLE }.map { it.id }.toSet()
        val vegetablesUsed = draft.meals.flatMap { it.foodIds }.filter { it in vegetables }.toSet().size
        if (vegetablesUsed < MIN_VEGETABLES) {
            problems += "only $vegetablesUsed different vegetables from the list were used, at least $MIN_VEGETABLES are needed"
        }
        val favoritesUsed = keys.count { it in favoriteKeys }
        if (favoritesUsed > MAX_FAVORITES) problems += "$favoritesUsed favorite dishes were used, at most $MAX_FAVORITES are allowed"
        return problems
    }
}

/**
 * Seasonal week through [GeminiTextClient] (the ai-generate proxy). One retry sends the broken
 * rules back; if the retry still breaks a rule but is a complete week, it is shown anyway (best
 * effort). A reply that is not a complete week is an error.
 */
internal class GeminiSeasonalWeekSuggester(
    private val textClient: GeminiTextClient,
    private val catalogs: SeasonalCatalogRepository,
    private val regionService: DeviceRegionService,
    private val favorites: FavoriteDishesRepository,
    private val currentLanguageCode: () -> String,
    private val currentMonth: () -> Int,
) : SeasonalWeekSuggester {

    override suspend fun suggestWeek(currentPlan: WeeklyMealPlan): Result<NutritionAiPlanner.MealPlanGeneration> {
        val catalog = currentCatalog().getOrElse { return Result.failure(it) }
        val favoriteDishes = favorites.favorites.value
        val favoriteKeys = favoriteDishes.map { it.normalisedLabel }.toSet()
        val planned = currentPlan.days.flatMap { listOf(it.lunch, it.dinner) }.filter { it.isNotBlank() }
        var prompt = SeasonalWeekPrompt.build(catalog, favoriteDishes.map { it.label }, planned, currentLanguageCode())
        var bestEffort: SeasonalWeekDraft? = null
        repeat(ATTEMPTS) {
            val reply = textClient.complete(prompt, MAX_OUTPUT_TOKENS, TEMPERATURE).getOrElse { return Result.failure(it) }
            val draft = SeasonalWeekParser.parse(reply).getOrNull()
            val problems = draft?.let { SeasonalWeekValidator.problems(it, catalog, favoriteKeys) }
                ?: listOf("the reply was not the requested JSON")
            if (draft != null && problems.isEmpty()) return Result.success(draft.toGeneration())
            if (draft != null && SeasonalWeekValidator.isComplete(draft)) bestEffort = draft
            prompt = SeasonalWeekPrompt.retry(prompt, problems)
        }
        return bestEffort?.let { Result.success(it.toGeneration()) }
            ?: Result.failure(GeminiApiException(GeminiApiException.Reason.PARSE_ERROR))
    }

    override suspend fun prefetchCatalog() {
        currentCatalog()
    }

    private suspend fun currentCatalog(): Result<SeasonalCatalog> {
        val language = currentLanguageCode()
        val country = regionService.getRegion()?.countryCode?.takeIf { it.isNotBlank() } ?: fallbackCountry(language)
        return catalogs.catalogFor(country, currentMonth(), language)
    }

    private companion object {
        const val ATTEMPTS = 2
        const val MAX_OUTPUT_TOKENS = 3_072
        const val TEMPERATURE = 0.7

        /** Without a device region, the language's main country (the app is Italian-first). */
        fun fallbackCountry(languageCode: String): String = when (languageCode.substringBefore('-').lowercase()) {
            "fr" -> "FR"
            "es" -> "ES"
            "de" -> "DE"
            "ar" -> "SA"
            "en" -> "GB"
            else -> "IT"
        }
    }
}
