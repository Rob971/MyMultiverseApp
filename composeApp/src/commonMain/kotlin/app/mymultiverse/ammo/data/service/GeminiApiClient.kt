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

private const val GEMINI_API_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent"
private const val DEFAULT_TIMEOUT_MS = 15_000L
private val log = Logger.withTag("GeminiApiClient")

/**
 * Low-level Gemini 2.0 Flash Lite text-completion client.
 *
 * Sends a single-turn prompt and returns the raw text content from the first
 * candidate. All JSON parsing of the response content is left to callers
 * (via [GeminiResponseParser]).
 *
 * **Body serialisation:** always uses [TextContent] with `application/json`
 * so Ktor's DefaultTransformers cannot override the content-type to text/plain.
 */
internal class GeminiApiClient(
    private val apiKeyProvider: () -> String,
    private val httpClient: HttpClient = HttpClient {
        install(HttpTimeout) { requestTimeoutMillis = DEFAULT_TIMEOUT_MS }
    },
) : GeminiTextClient {
    /**
     * Sends [prompt] to Gemini and returns the model's plain-text output.
     * Returns [Result.failure] on any HTTP, network, or response-parsing error.
     */
    override suspend fun complete(
        prompt: String,
        maxOutputTokens: Int,
        temperature: Double,
    ): Result<String> = runCatching {
        val apiKey = apiKeyProvider()
        check(apiKey.isNotBlank()) { "Gemini API key is not configured" }

        val body = buildRequestJson(prompt, maxOutputTokens, temperature)
        val response = httpClient.post {
            url("$GEMINI_API_URL?key=$apiKey")
            setBody(TextContent(body, ContentType.Application.Json))
        }

        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.bodyAsText() }.getOrElse { "" }
            log.w { "Gemini HTTP ${response.status.value}: $errorBody" }
            error("Gemini API error ${response.status.value}")
        }

        GeminiResponseParser.extractResponseText(response.bodyAsText())
    }.onFailure { e ->
        log.w(e) { "Gemini complete() failed" }
    }

    private fun buildRequestJson(prompt: String, maxTokens: Int, temp: Double): String =
        buildString {
            append("{\"contents\":[{\"parts\":[{\"text\":\"")
            append(prompt.escapeJson())
            append("\"}]}],\"generationConfig\":{\"temperature\":")
            append(temp)
            append(",\"maxOutputTokens\":")
            append(maxTokens)
            append("}}")
        }

    private fun String.escapeJson(): String = buildString {
        for (ch in this@escapeJson) {
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
