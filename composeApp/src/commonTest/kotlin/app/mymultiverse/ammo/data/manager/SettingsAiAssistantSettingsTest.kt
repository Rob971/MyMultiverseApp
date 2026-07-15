package app.mymultiverse.ammo.data.manager

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsAiAssistantSettingsTest {

    private fun settings(initial: String = ""): Pair<SettingsAiAssistantSettings, MapSettings> {
        val map = MapSettings()
        if (initial.isNotBlank()) {
            map.putString(SettingsAiAssistantSettings.KEY, initial)
        }
        return SettingsAiAssistantSettings(settings = map) to map
    }

    @Test
    fun initialKey_isEmptyWhenNoPersisted() {
        val (sut, _) = settings()
        assertEquals("", sut.geminiApiKey.value)
    }

    @Test
    fun initialKey_readsPersistedValue() {
        val (sut, _) = settings(initial = "my-saved-key")
        assertEquals("my-saved-key", sut.geminiApiKey.value)
    }

    @Test
    fun compiledKey_usedWhenNoPersistedKey() {
        val map = MapSettings()
        val sut = SettingsAiAssistantSettings(settings = map, compiledKey = "compiled-key")
        assertEquals("compiled-key", sut.geminiApiKey.value)
    }

    @Test
    fun persistedKeyTakesPrecedenceOverCompiledKey() {
        val map = MapSettings()
        map.putString(SettingsAiAssistantSettings.KEY, "user-key")
        val sut = SettingsAiAssistantSettings(settings = map, compiledKey = "compiled-key")
        assertEquals("user-key", sut.geminiApiKey.value)
    }

    @Test
    fun setGeminiApiKey_trimmedAndPersisted() = runTest {
        val (sut, map) = settings()

        sut.setGeminiApiKey("  AIzaSy_test_key  ")

        assertEquals("AIzaSy_test_key", sut.geminiApiKey.value)
        assertEquals("AIzaSy_test_key", map.getString(SettingsAiAssistantSettings.KEY, ""))
    }

    @Test
    fun setGeminiApiKey_emitsUpdatedValueToFlow() = runTest {
        val (sut, _) = settings()

        sut.setGeminiApiKey("new-key")

        assertEquals("new-key", sut.geminiApiKey.value)
    }

    @Test
    fun setGeminiApiKey_blankInput_clearsKey() = runTest {
        val (sut, map) = settings(initial = "existing-key")

        sut.setGeminiApiKey("   ")

        assertEquals("", sut.geminiApiKey.value)
        assertTrue(map.getStringOrNull(SettingsAiAssistantSettings.KEY) == null)
    }

    @Test
    fun clearGeminiApiKey_removesFromSettingsAndEmitsEmpty() = runTest {
        val (sut, map) = settings(initial = "some-key")

        sut.clearGeminiApiKey()

        assertEquals("", sut.geminiApiKey.value)
        assertTrue(map.getStringOrNull(SettingsAiAssistantSettings.KEY) == null)
    }

    @Test
    fun clearGeminiApiKey_whenAlreadyEmpty_doesNotThrow() = runTest {
        val (sut, _) = settings()

        sut.clearGeminiApiKey()

        assertEquals("", sut.geminiApiKey.value)
    }
}
