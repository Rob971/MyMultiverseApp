package app.mymultiverse.ammo.domain.settings

import kotlinx.coroutines.flow.StateFlow

/**
 * Persisted user preference for the Gemini API key.
 *
 * When [geminiApiKey] is blank the app falls back to local heuristic ingredient
 * generation. Users obtain a free key at https://aistudio.google.com/app/apikey
 * and enter it in Account & settings.
 */
interface AiAssistantSettings {
    /** The currently persisted Gemini API key, or an empty string when not configured. */
    val geminiApiKey: StateFlow<String>

    /** Persists [key] (trimmed). Passing a blank string is equivalent to [clearGeminiApiKey]. */
    fun setGeminiApiKey(key: String)

    /** Removes the persisted key so [geminiApiKey] emits an empty string. */
    fun clearGeminiApiKey()
}
