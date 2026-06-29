package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLanguageManager(
    initialCode: String = SupportedAppLanguages.DEFAULT_CODE,
) : LanguageManager {
    private val _currentLanguage = MutableStateFlow(initialCode)
    override val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    override fun changeLanguage(languageCode: String) {
        _currentLanguage.value = SupportedAppLanguages.normalize(languageCode)
    }
}
