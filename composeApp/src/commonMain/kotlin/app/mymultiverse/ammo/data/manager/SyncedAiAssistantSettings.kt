package app.mymultiverse.ammo.data.manager

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.repository.AiSettingsRemoteRepository
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * [AiAssistantSettings] that keeps the Gemini API key in sync between device-local
 * [SharedPreferences][SettingsAiAssistantSettings] and the per-user Supabase record.
 *
 * **Sync strategy:**
 * - On every sign-in, the remote key is fetched (with retries) and wins over a stale local value.
 *   This ensures a key saved on another device is available immediately.
 * - When the signed-in user changes, any locally cached key from the previous account is cleared
 *   before syncing so household device hand-offs never reuse another user's key.
 * - When remote is empty but local still has a key (e.g. first save while offline), the local
 *   value is pushed to Supabase so reinstall / new-device sign-in can restore it.
 * - On [setGeminiApiKey] / [clearGeminiApiKey] the local store is updated first
 *   (instant UI feedback), then the remote is updated in the background.
 * - If the remote call fails (offline, network error) the local key is still valid
 *   for the current session and will be pushed on the next successful write.
 * - [refreshFromRemote] can be called from settings/AI entry points to recover after
 *   reinstall or a failed first sync.
 */
class SyncedAiAssistantSettings(
    private val local: SettingsAiAssistantSettings,
    private val remote: AiSettingsRemoteRepository,
    authRepository: AuthRepository,
    private val scope: CoroutineScope,
    private val appLogger: AppLogger? = null,
) : AiAssistantSettings {

    override val geminiApiKey: StateFlow<String> = local.geminiApiKey

    private val log = Logger.withTag("SyncedAiSettings")
    private var lastSyncedUserId: String? = null

    init {
        scope.launch {
            authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .distinctUntilChangedBy { it.user.id }
                .collect { auth ->
                    if (lastSyncedUserId != null && lastSyncedUserId != auth.user.id) {
                        log.d { "Signed-in user changed; clearing stale local Gemini key" }
                        local.clearGeminiApiKey()
                    }
                    lastSyncedUserId = auth.user.id
                    syncFromRemote()
                }
        }
    }

    override fun setGeminiApiKey(key: String) {
        local.setGeminiApiKey(key)
        scope.launch {
            remote.upsertGeminiApiKey(key)
                .onFailure { log.w(it) { "Remote upsert of Gemini key failed; local already updated" } }
        }
    }

    override fun clearGeminiApiKey() {
        local.clearGeminiApiKey()
        scope.launch {
            remote.clearGeminiApiKey()
                .onFailure { log.w(it) { "Remote clear of Gemini key failed; local already cleared" } }
        }
    }

    override suspend fun refreshFromRemote() {
        syncFromRemote()
    }

    private suspend fun syncFromRemote() {
        val remoteResult = fetchRemoteKeyWithRetry()
            .onFailure { error ->
                log.w(error) { "Remote Gemini key fetch failed on sign-in; using local" }
                appLogger?.recordError(
                    tag = TAG,
                    message = "gemini_key_fetch_failed",
                    throwable = error,
                )
            }
        val remoteKey = remoteResult.getOrNull() ?: return

        val localKey = local.geminiApiKey.value
        when {
            remoteKey.isNotBlank() && remoteKey != localKey -> {
                log.d { "Gemini key synced from remote to local" }
                local.setGeminiApiKey(remoteKey)
            }
            remoteKey.isBlank() && localKey.isNotBlank() -> {
                log.d { "Backfilling local Gemini key to remote" }
                remote.upsertGeminiApiKey(localKey)
                    .onFailure { log.w(it) { "Remote backfill of Gemini key failed; local still valid" } }
            }
        }
    }

    private suspend fun fetchRemoteKeyWithRetry(): Result<String> {
        var lastFailure: Result<String>? = null
        repeat(REMOTE_FETCH_ATTEMPTS) { attempt ->
            val result = remote.getGeminiApiKey()
            if (result.isSuccess) {
                return result
            }
            lastFailure = result
            if (attempt < REMOTE_FETCH_ATTEMPTS - 1) {
                delay(REMOTE_FETCH_BACKOFF_MS * (attempt + 1))
            }
        }
        return lastFailure ?: Result.failure(IllegalStateException("remote_fetch_exhausted"))
    }

    private companion object {
        const val TAG = "SyncedAiSettings"
        const val REMOTE_FETCH_ATTEMPTS = 3
        const val REMOTE_FETCH_BACKOFF_MS = 400L
    }
}
