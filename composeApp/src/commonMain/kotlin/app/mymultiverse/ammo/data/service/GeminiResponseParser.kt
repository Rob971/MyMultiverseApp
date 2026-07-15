package app.mymultiverse.ammo.data.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Extracts a list of ingredient strings from a Gemini API JSON response.
 *
 * Gemini returns a JSON structure where the actual text content lives at
 * `candidates[0].content.parts[0].text`. That text is expected to be a JSON
 * array of ingredient name strings, possibly wrapped in markdown code fences.
 */
internal object GeminiResponseParser {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * Parses the full Gemini REST response body into a list of ingredient strings.
     *
     * @throws IllegalStateException if the response structure is unexpected
     * @throws IllegalArgumentException if the ingredient array cannot be extracted
     */
    fun parseIngredients(responseBody: String): List<String> {
        val root = jsonParser.parseToJsonElement(responseBody)
        val text = root.jsonObject["candidates"]
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("content")
            ?.jsonObject?.get("parts")
            ?.jsonArray?.getOrNull(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.content
            ?: error("Unexpected Gemini response structure: $responseBody")

        return extractJsonArray(text)
    }

    /**
     * Extracts a JSON array of strings from raw text, handling optional markdown code fences.
     */
    fun extractJsonArray(text: String): List<String> {
        // Strip markdown code fences (```json ... ``` or ``` ... ```)
        val stripped = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val start = stripped.indexOf('[')
        val end = stripped.lastIndexOf(']')
        require(start != -1 && end > start) {
            "No JSON array found in Gemini text output: $stripped"
        }

        return jsonParser.parseToJsonElement(stripped.substring(start, end + 1))
            .jsonArray
            .mapNotNull { element ->
                runCatching { element.jsonPrimitive.content }.getOrNull()
            }
            .filter { it.isNotBlank() }
    }

    /** Maps a BCP-47 language code to a human-readable language name for the Gemini prompt. */
    fun languageNameFor(code: String): String = when (code.lowercase().substringBefore('-')) {
        "it" -> "Italian"
        "fr" -> "French"
        "es" -> "Spanish"
        "de" -> "German"
        "ar" -> "Arabic"
        "nap" -> "Neapolitan Italian"
        else -> "English"
    }
}
