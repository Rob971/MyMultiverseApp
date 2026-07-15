package app.mymultiverse.ammo.data.service

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

private const val GEMINI_BASE_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent"
private const val REQUEST_TIMEOUT_MS = 10_000L
private val log = Logger.withTag("GeminiDishIngredientClient")

/**
 * Calls the Gemini REST API (free tier) to generate a dish-specific ingredient list.
 *
 * Uses [GeminiResponseParser] for all JSON parsing so the HTTP logic stays thin
 * and the parsing stays unit-testable without a network.
 *
 * **Body serialisation note:** the request body is sent as [TextContent] with
 * `Content-Type: application/json`. Do **not** use `setBody(String)` with a separate
 * `header(ContentType)` — in Ktor 3.x the DefaultTransformers would override the
 * header with `text/plain`, causing a 400 from the Gemini API.
 */
internal class GeminiDishIngredientClient(
    /**
     * Provides the Gemini API key at call time so the key can be changed in user
     * settings without recreating the client.
     */
    private val apiKeyProvider: () -> String,
    private val httpClient: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
        }
    },
) : DishIngredientClient {

    /**
     * Returns a localized list of ingredients for [dish] in the language identified
     * by [languageCode]. Returns a [Result] failure on any network or parse error.
     */
    override suspend fun generateIngredients(dish: String, languageCode: String): Result<List<String>> =
        try {
            val apiKey = apiKeyProvider()
            check(apiKey.isNotBlank()) { "Gemini API key is not configured" }

            val languageName = GeminiResponseParser.languageNameFor(languageCode)
            val requestBody = buildRequestBody(dish, languageName)

            val response = httpClient.post {
                url("$GEMINI_BASE_URL?key=$apiKey")
                // TextContent sets both the body bytes and the Content-Type atomically.
                // A separate header() call is not needed and must not be used here —
                // it would be silently overridden by Ktor's DefaultTransformers.
                setBody(TextContent(requestBody, ContentType.Application.Json))
            }

            if (!response.status.isSuccess()) {
                val errorBody = try { response.bodyAsText() } catch (_: Exception) { "" }
                log.w { "Gemini API error ${response.status.value}: $errorBody" }
                error("Gemini API error ${response.status.value}")
            }

            Result.success(GeminiResponseParser.parseIngredients(response.bodyAsText()))
        } catch (e: CancellationException) {
            throw e  // Always propagate cancellation
        } catch (e: Exception) {
            log.w(e) { "generateIngredients failed for dish='$dish' language='$languageCode'" }
            Result.failure(e)
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
