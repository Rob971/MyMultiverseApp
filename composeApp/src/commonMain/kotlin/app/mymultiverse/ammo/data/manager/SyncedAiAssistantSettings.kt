package app.mymultiverse.ammo.data.manager

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.repository.AiSettingsRemoteRepository
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * [AiAssistantSettings] that keeps the Gemini API key in sync between device-local
 * [SharedPreferences][SettingsAiAssistantSettings] and the per-user Supabase record.
 *
 * **Sync strategy:**
 * - On every sign-in, the remote key is fetched and wins over a stale local value.
 *   This ensures a key saved on another device is available immediately.
 * - On [setGeminiApiKey] / [clearGeminiApiKey] the local store is updated first
 *   (instant UI feedback), then the remote is updated in the background.
 * - If the remote call fails (offline, network error) the local key is still valid
 *   for the current session and will be pushed on the next successful write.
 */
class SyncedAiAssistantSettings(
    private val local: SettingsAiAssistantSettings,
    private val remote: AiSettingsRemoteRepository,
    authRepository: AuthRepository,
    private val scope: CoroutineScope,
) : AiAssistantSettings {

    override val geminiApiKey: StateFlow<String> = local.geminiApiKey

    private val log = Logger.withTag("SyncedAiSettings")

    init {
        scope.launch {
            authRepository.authState
                .filterIsInstance<AuthState.Authenticated>()
                .distinctUntilChangedBy { it.user.id }
                .collect { syncFromRemote() }
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

    private suspend fun syncFromRemote() {
        val remoteKey = remote.getGeminiApiKey()
            .onFailure { log.w(it) { "Remote Gemini key fetch failed on sign-in; using local" } }
            .getOrNull()
            ?: return
        if (remoteKey.isNotBlank() && remoteKey != local.geminiApiKey.value) {
            log.d { "Gemini key synced from remote to local" }
            local.setGeminiApiKey(remoteKey)
        }
    }
}
