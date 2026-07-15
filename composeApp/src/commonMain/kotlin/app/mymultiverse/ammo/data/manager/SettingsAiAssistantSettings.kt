package app.mymultiverse.ammo.data.manager

import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsAiAssistantSettings(
    private val settings: Settings,
    /**
     * Compile-time API key embedded via the build system (e.g. from local.properties).
     * Used as the initial value when no user-saved key exists in Settings yet.
     */
    compiledKey: String = "",
) : AiAssistantSettings {

    private val _geminiApiKey = MutableStateFlow(
        settings.getString(KEY, compiledKey).trim(),
    )
    override val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    override fun setGeminiApiKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isBlank()) {
            clearGeminiApiKey()
            return
        }
        settings.putString(KEY, trimmed)
        _geminiApiKey.value = trimmed
    }

    override fun clearGeminiApiKey() {
        settings.remove(KEY)
        _geminiApiKey.value = ""
    }

    companion object {
        const val KEY = "gemini_api_key"
    }
}
