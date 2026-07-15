package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.domain.repository.AiSettingsRemoteRepository

/** No-op implementation used when Supabase is not configured (missing anon key). */
internal class UnconfiguredAiSettingsRepository : AiSettingsRemoteRepository {
    override suspend fun getGeminiApiKey(): Result<String> = Result.success("")
    override suspend fun upsertGeminiApiKey(key: String): Result<Unit> = Result.success(Unit)
    override suspend fun clearGeminiApiKey(): Result<Unit> = Result.success(Unit)
}
