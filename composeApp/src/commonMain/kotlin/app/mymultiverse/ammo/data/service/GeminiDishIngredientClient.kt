package app.mymultiverse.ammo.data.service

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

private const val GEMINI_BASE_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent"

/**
 * Calls the Gemini REST API (free tier) to generate a dish-specific ingredient list.
 *
 * Uses [GeminiResponseParser] for all JSON parsing so the HTTP logic stays thin
 * and the parsing stays unit-testable without a network.
 */
internal class GeminiDishIngredientClient(
    /**
     * Provides the Gemini API key at call time so the key can be changed in user
     * settings without recreating the client.
     */
    private val apiKeyProvider: () -> String,
    private val httpClient: HttpClient = HttpClient(),
) : DishIngredientClient {
    /**
     * Returns a localized list of ingredients for [dish] in the language identified
     * by [languageCode]. Returns a [Result] failure on any network or parse error.
     */
    override suspend fun generateIngredients(dish: String, languageCode: String): Result<List<String>> =
        runCatching {
            val apiKey = apiKeyProvider()
            check(apiKey.isNotBlank()) { "Gemini API key is not configured" }

            val languageName = GeminiResponseParser.languageNameFor(languageCode)
            val requestBody = buildRequestBody(dish, languageName)

            val response = httpClient.post {
                url("$GEMINI_BASE_URL?key=$apiKey")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(requestBody)
            }

            check(response.status.isSuccess()) {
                "Gemini API error ${response.status.value}"
            }

            GeminiResponseParser.parseIngredients(response.bodyAsText())
        }

    private fun buildRequestBody(dish: String, languageName: String): String {
        val prompt = buildString {
            append("List the specific ingredients needed to prepare the dish: \"")
            append(dish.escapeForJson())
            append("\". ")
            append("Reply in $languageName only. ")
            append("Return ONLY a JSON array of ingredient names, 6 to 10 items, no explanation. ")
            append("Example format: [\"item1\",\"item2\"]")
        }

        return buildString {
            append("{\"contents\":[{\"parts\":[{\"text\":\"")
            append(prompt.escapeForJson())
            append("\"}]}],\"generationConfig\":{\"temperature\":0.2,\"maxOutputTokens\":256}}")
        }
    }

    private fun String.escapeForJson(): String = buildString {
        for (ch in this@escapeForJson) {
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(ch)
            }
        }
    }
}
