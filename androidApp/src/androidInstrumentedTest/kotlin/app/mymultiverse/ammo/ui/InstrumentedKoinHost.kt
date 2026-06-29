package app.mymultiverse.ammo.ui

import androidx.compose.runtime.Composable
import app.mymultiverse.ammo.domain.manager.AppThemePreference
import app.mymultiverse.ammo.domain.manager.AppThemePreferences
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import app.mymultiverse.ammo.domain.manager.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.KoinApplication
import org.koin.dsl.module

private class InstrumentedLanguageManager(
    initialCode: String = SupportedAppLanguages.DEFAULT_CODE,
) : LanguageManager {
    private val _currentLanguage = MutableStateFlow(initialCode)
    override val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    override fun changeLanguage(languageCode: String) {
        _currentLanguage.value = SupportedAppLanguages.normalize(languageCode)
    }
}

private class InstrumentedThemeManager(
    initial: AppThemePreference = AppThemePreferences.DEFAULT,
) : ThemeManager {
    private val _currentPreference = MutableStateFlow(initial)
    override val currentPreference: StateFlow<AppThemePreference> = _currentPreference.asStateFlow()

    override fun changeThemePreference(preference: AppThemePreference) {
        _currentPreference.value = preference
    }
}

private val instrumentedKoinModule = module {
    single<LanguageManager> { InstrumentedLanguageManager() }
    single<ThemeManager> { InstrumentedThemeManager() }
}

/** Minimal Koin graph for composables that use `koinInject` (e.g. [LanguagePicker] on home). */
@Composable
fun InstrumentedKoinHost(content: @Composable () -> Unit) {
    KoinApplication(application = { modules(instrumentedKoinModule) }) {
        content()
    }
}
