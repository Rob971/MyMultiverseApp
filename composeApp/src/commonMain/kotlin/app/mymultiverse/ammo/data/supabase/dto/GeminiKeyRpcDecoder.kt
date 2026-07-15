package app.mymultiverse.ammo.data.supabase.dto

import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.json.Json

/**
 * Decodes `get_gemini_api_key()` RPC responses.
 *
 * PostgREST returns SETOF/table functions as a JSON array (`[{"key":"…"}]` or `[]`).
 * Some clients may surface a single object — both shapes are accepted.
 */
internal object GeminiKeyRpcDecoder {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun decodeKey(result: PostgrestResult): String = decodeKeyFromData(result.data)

    /** Visible for unit tests that assert JSON parsing without a live PostgREST client. */
    internal fun decodeKeyFromData(data: String): String {
        val row = runCatching { json.decodeFromString<List<GeminiKeyRow>>(data) }
            .recoverCatching { listOf(json.decodeFromString<GeminiKeyRow>(data)) }
            .getOrElse { error ->
                throw IllegalStateException(
                    "get_gemini_api_key_decode_failed: ${error.message}",
                    error,
                )
            }
            .firstOrNull()
        return row?.key?.trim().orEmpty()
    }
}
