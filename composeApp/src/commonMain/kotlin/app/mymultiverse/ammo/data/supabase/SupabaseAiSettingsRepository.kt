package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.data.supabase.dto.GeminiKeyRpcDecoder
import app.mymultiverse.ammo.data.supabase.dto.UpsertGeminiKeyParams
import app.mymultiverse.ammo.domain.repository.AiSettingsRemoteRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

/**
 * Supabase-backed implementation of [AiSettingsRemoteRepository].
 *
 * Delegates to `get_gemini_api_key()` / `upsert_gemini_api_key(p_key)` RPCs which
 * encrypt/decrypt the key server-side using `pgp_sym_encrypt`. The plaintext key
 * is returned over the TLS-protected PostgREST connection.
 */
internal class SupabaseAiSettingsRepository(
    private val client: SupabaseClient,
) : AiSettingsRemoteRepository {

    override suspend fun getGeminiApiKey(): Result<String> = runCatching {
        client.auth.awaitInitialization()
        val result = client.postgrest.rpc("get_gemini_api_key")
        GeminiKeyRpcDecoder.decodeKey(result)
    }

    override suspend fun upsertGeminiApiKey(key: String): Result<Unit> = runCatching {
        client.auth.awaitInitialization()
        client.postgrest.rpc("upsert_gemini_api_key", UpsertGeminiKeyParams(key.trim()))
    }

    override suspend fun clearGeminiApiKey(): Result<Unit> = runCatching {
        client.auth.awaitInitialization()
        client.postgrest.rpc("upsert_gemini_api_key", UpsertGeminiKeyParams(""))
    }
}
