package app.mymultiverse.kmp.ui

import androidx.compose.runtime.Composable
import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.domain.manager.SupportedAppLanguages
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

private val instrumentedKoinModule = module {
    single<LanguageManager> { InstrumentedLanguageManager() }
}

/** Minimal Koin graph for composables that use `koinInject` (e.g. [LanguagePicker] on home). */
@Composable
fun InstrumentedKoinHost(content: @Composable () -> Unit) {
    KoinApplication(application = { modules(instrumentedKoinModule) }) {
        content()
    }
}
