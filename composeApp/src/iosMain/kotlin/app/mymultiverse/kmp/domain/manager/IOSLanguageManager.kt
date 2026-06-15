package app.mymultiverse.kmp.domain.manager

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSUserDefaults

class IOSLanguageManager(
    private val settings: Settings,
) : LanguageManager {
    private val persistedLanguage = readPersistedLanguage()
    private val _currentLanguage = MutableStateFlow(persistedLanguage)
    override val currentLanguage: StateFlow<String> = _currentLanguage

    init {
        applyLocale(persistedLanguage)
    }

    override fun changeLanguage(languageCode: String) {
        val normalized = SupportedAppLanguages.normalize(languageCode)
        if (normalized == _currentLanguage.value) return

        settings.putString(SupportedAppLanguages.SETTINGS_KEY, normalized)
        _currentLanguage.value = normalized
        applyLocale(normalized)
    }

    private fun readPersistedLanguage(): String {
        val stored = settings.getString(SupportedAppLanguages.SETTINGS_KEY, SupportedAppLanguages.DEFAULT_CODE)
        return SupportedAppLanguages.normalize(stored)
    }

    private fun applyLocale(languageCode: String) {
        NSUserDefaults.standardUserDefaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}
