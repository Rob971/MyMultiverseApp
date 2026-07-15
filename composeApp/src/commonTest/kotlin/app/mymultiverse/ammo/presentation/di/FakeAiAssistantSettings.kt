package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [AiAssistantSettings] for unit and Koin integration tests. */
class FakeAiAssistantSettings(key: String = "") : AiAssistantSettings {
    private val _key = MutableStateFlow(key)
    override val geminiApiKey: StateFlow<String> = _key
    override fun setGeminiApiKey(key: String) { _key.value = key.trim() }
    override fun clearGeminiApiKey() { _key.value = "" }
}
