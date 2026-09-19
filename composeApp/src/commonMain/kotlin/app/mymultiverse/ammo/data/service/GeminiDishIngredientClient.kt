package app.mymultiverse.ammo.data.service

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException

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
    /** Resolves the ai-generate proxy target (with the user's session) for each request. */
    private val route: GeminiRoute,
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
            val target = route.target()
            val requestBody = buildRequestBody(dish, languageCode)

            val response = httpClient.post {
                url(target.url)
                target.headers.forEach { (name, value) -> header(name, value) }
                // TextContent sets both the body bytes and the Content-Type atomically.
                // A separate Content-Type header() call is not needed and must not be
                // used here — it would be silently overridden by Ktor's DefaultTransformers.
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

    private fun buildRequestBody(dish: String, languageCode: String): String {
        val prompt = buildString {
            append(GeminiResponseParser.languagePromptDirective(languageCode))
            append(' ')
            append("List the specific ingredients needed to prepare the dish: \"")
            append(dish.escapeForJson())
            append("\". ")
            append("Return ONLY a JSON array of ingredient names in the user's language, 6 to 10 items, no explanation. ")
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
