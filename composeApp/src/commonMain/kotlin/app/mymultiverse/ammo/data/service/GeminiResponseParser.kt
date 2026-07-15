package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.nutrition.NutritionFoodSuggestionLocalization
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses text content from Gemini API JSON responses.
 *
 * Gemini returns a JSON structure where the actual text content lives at
 * `candidates[0].content.parts[0].text`. Methods in this object extract
 * and interpret that text in various forms (ingredient arrays, meal plan
 * objects, or plain advice text).
 */
internal object GeminiResponseParser {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    // ── Response text extraction ─────────────────────────────────────────────

    /**
     * Extracts the raw text content string from a full Gemini REST response body.
     *
     * @throws IllegalStateException if the response structure is unexpected
     */
    fun extractResponseText(responseBody: String): String {
        val root = jsonParser.parseToJsonElement(responseBody)
        return root.jsonObject["candidates"]
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("content")
            ?.jsonObject?.get("parts")
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.content
            ?: error("Unexpected Gemini response structure: $responseBody")
    }

    /**
     * Convenience: extracts response text then parses it as a JSON array of ingredients.
     */
    fun parseIngredients(responseBody: String): List<String> =
        extractJsonArray(extractResponseText(responseBody))

    // ── Meal plan parsing ────────────────────────────────────────────────────

    /**
     * Parses a Gemini text response into a list of [DayMeals] and a summary.
     *
     * Expects JSON with this shape (markdown fences stripped automatically):
     * ```json
     * {
     *   "days": [{"lunch":"…","dinner":"…"}, …],
     *   "summary": "…"
     * }
     * ```
     *
     * @throws IllegalArgumentException if no JSON object can be found in [text]
     */
    fun parseMealPlan(text: String): Pair<List<DayMeals>, String> {
        val stripped = stripMarkdownFences(text)
        val objStart = stripped.indexOf('{')
        val objEnd = stripped.lastIndexOf('}')
        require(objStart != -1 && objEnd > objStart) {
            "No JSON object found in Gemini meal plan output: $stripped"
        }
        val root = jsonParser.parseToJsonElement(stripped.substring(objStart, objEnd + 1)).jsonObject
        val days = root["days"]?.jsonArray?.mapNotNull { el ->
            runCatching {
                DayMeals(
                    lunch = el.jsonObject["lunch"]?.jsonPrimitive?.content?.trim() ?: "",
                    dinner = el.jsonObject["dinner"]?.jsonPrimitive?.content?.trim() ?: "",
                )
            }.getOrNull()
        } ?: emptyList()
        val summary = root["summary"]?.jsonPrimitive?.content?.trim() ?: ""
        return days to summary
    }

    // ── Advice text parsing ──────────────────────────────────────────────────

    /**
     * Returns clean plain text from a Gemini advice response, stripping any
     * markdown code fences or leading/trailing whitespace.
     */
    fun parseAdviceText(text: String): String = stripMarkdownFences(text).trim()

    // ── JSON array extraction ────────────────────────────────────────────────

    /**
     * Extracts a JSON array of strings from raw text, handling optional markdown code fences.
     */
    fun extractJsonArray(text: String): List<String> {
        val stripped = stripMarkdownFences(text)
        val start = stripped.indexOf('[')
        val end = stripped.lastIndexOf(']')
        require(start != -1 && end > start) {
            "No JSON array found in Gemini text output: $stripped"
        }
        return jsonParser.parseToJsonElement(stripped.substring(start, end + 1))
            .jsonArray
            .mapNotNull { element -> runCatching { element.jsonPrimitive.content }.getOrNull() }
            .filter { it.isNotBlank() }
    }

    // ── Language mapping ─────────────────────────────────────────────────────

    /** Maps a BCP-47 language code to a human-readable language name for Gemini prompts. */
    fun languageNameFor(code: String): String = when (promptLanguageKey(code)) {
        "it" -> "Italian"
        "fr" -> "French"
        "es" -> "Spanish"
        "de" -> "German"
        "ar" -> "Arabic"
        "nap" -> "Neapolitan Italian"
        else -> "English"
    }

    /**
     * Strong language constraint prepended to every Gemini prompt so dish names,
     * ingredients, summaries, and advice match the user's selected app language.
     */
    fun languagePromptDirective(languageCode: String): String {
        val normalized = promptLanguageKey(languageCode)
        val name = languageNameFor(languageCode)
        return "The user's app language is $name (locale: $normalized). " +
            "You MUST write the entire response in $name only — every dish name, ingredient, " +
            "summary sentence, and word of advice. " +
            "Do not use English or any other language unless the user's language is English."
    }

    /**
     * Resolves app/Gemini prompt locale without treating unknown codes as Napulitano.
     * Uses [SupportedAppLanguages] when the code is configured; otherwise falls back to English.
     */
    private fun promptLanguageKey(code: String): String {
        val lower = code.lowercase()
        if (lower == "ar-rsa" || lower == "ar") return "ar"
        if (lower in SupportedAppLanguages.codes) {
            return NutritionFoodSuggestionLocalization.normalizeLanguageCode(lower)
        }
        val base = lower.substringBefore('-')
        return when (base) {
            "it", "fr", "es", "de", "ar", "nap", "en" -> base
            else -> "en"
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun stripMarkdownFences(text: String): String =
        text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
}
