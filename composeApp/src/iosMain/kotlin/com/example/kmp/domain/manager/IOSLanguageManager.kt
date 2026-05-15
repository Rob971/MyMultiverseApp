package com.example.kmp.domain.manager

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSUserDefaults

class IOSLanguageManager(
    private val settings: Settings
) : LanguageManager {
    private val _currentLanguage = MutableStateFlow(settings.getString("app_language", "nap"))
    override val currentLanguage: StateFlow<String> = _currentLanguage

    override fun changeLanguage(languageCode: String) {
        settings.putString("app_language", languageCode)
        _currentLanguage.value = languageCode
        NSUserDefaults.standardUserDefaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}
