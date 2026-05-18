package app.mymultiverse.kmp.domain.manager

import kotlinx.coroutines.flow.StateFlow

interface LanguageManager {
    val currentLanguage: StateFlow<String>
    fun changeLanguage(languageCode: String)
}
