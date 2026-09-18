package app.mymultiverse.ammo.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import app.mymultiverse.ammo.data.manager.SettingsAiAssistantSettings
import app.mymultiverse.ammo.data.tour.ProductTourStore
import app.mymultiverse.ammo.domain.manager.AppThemePreference
import app.mymultiverse.ammo.domain.manager.AppThemePreferences
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import app.mymultiverse.ammo.domain.manager.ThemeManager
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import app.mymultiverse.ammo.presentation.screens.tour.ProductTourScreenModel
import com.russhwolf.settings.MapSettings
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

internal val instrumentedKoinModule = module {
    single<LanguageManager> { InstrumentedLanguageManager() }
    single<ThemeManager> { InstrumentedThemeManager() }
    // NutritionAiAssistantContent injects this directly.
    single<AiAssistantSettings> { SettingsAiAssistantSettings(settings = MapSettings()) }
}

/**
 * `Modifier.productTourTarget` (MainTabShell, home hub) injects the tour model. Kept out of
 * [instrumentedKoinModule] because ProductTourInstrumentedTest registers its own instance.
 */
internal val instrumentedTourModule = module {
    single { ProductTourScreenModel(store = ProductTourStore(MapSettings())) }
}

/** Minimal Koin graph for composables that use `koinInject` (e.g. [LanguagePicker] on home). */
@Composable
fun InstrumentedKoinHost(content: @Composable () -> Unit) {
    KoinApplication(application = { modules(instrumentedKoinModule, instrumentedTourModule) }) {
        content()
    }
}

/** `setContent` inside [InstrumentedKoinHost]; without it a `koinInject` call crashes the whole run. */
fun ComposeContentTestRule.setKoinContent(content: @Composable () -> Unit) {
    setContent { InstrumentedKoinHost(content) }
}
