package app.mymultiverse.ammo.domain.repository

/**
 * Remote persistence for the user's personal Gemini API key.
 *
 * The key is stored per-user server-side (encrypted at rest) so it survives
 * reinstalls and can be restored on a new device after signing in.
 * It is **not** shared across household members.
 */
interface AiSettingsRemoteRepository {
    /** Returns the stored Gemini API key, or an empty string when none is set. */
    suspend fun getGeminiApiKey(): Result<String>

    /** Persists [key] (trimmed). Passing a blank string is equivalent to [clearGeminiApiKey]. */
    suspend fun upsertGeminiApiKey(key: String): Result<Unit>

    /** Removes the stored key so subsequent [getGeminiApiKey] calls return an empty string. */
    suspend fun clearGeminiApiKey(): Result<Unit>
}
