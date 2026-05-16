package com.mymultiverse.kmp.domain.manager

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AndroidLanguageManager(
    private val context: Context,
    private val settings: Settings
) : LanguageManager {
    
    private val _currentLanguage = MutableStateFlow(settings.getString("app_language", "nap"))
    override val currentLanguage: StateFlow<String> = _currentLanguage

    init {
        val lang = settings.getString("app_language", "nap")
        if (lang.isNotEmpty()) {
            val localeList = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    override fun changeLanguage(languageCode: String) {
        settings.putString("app_language", languageCode)
        _currentLanguage.value = languageCode
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
