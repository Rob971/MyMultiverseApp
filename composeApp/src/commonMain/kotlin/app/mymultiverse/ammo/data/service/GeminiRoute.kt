package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.service.GeminiApiException

/** Where one Gemini `generateContent` request is sent, and the headers it needs. */
internal data class GeminiTarget(val url: String, val headers: Map<String, String>)

/** Resolves the target of each Gemini request. The app never holds a Gemini key. */
internal fun interface GeminiRoute {
    suspend fun target(): GeminiTarget
}

/**
 * Sends Gemini requests through the `ai-generate` Edge Function, which holds the key and
 * enforces the daily limits. Requests carry the signed-in user's session token; without a
 * session there is no target.
 */
internal class AiProxyGeminiRoute(
    supabaseUrl: String,
    private val anonKey: String,
    private val accessToken: suspend () -> String?,
) : GeminiRoute {
    private val url = supabaseUrl.trimEnd('/') + "/functions/v1/ai-generate"

    override suspend fun target(): GeminiTarget {
        val token = accessToken()?.takeIf { it.isNotBlank() }
            ?: throw GeminiApiException(GeminiApiException.Reason.AUTH_ERROR)
        return GeminiTarget(
            url = url,
            headers = mapOf("Authorization" to "Bearer $token", "apikey" to anonKey),
        )
    }
}
