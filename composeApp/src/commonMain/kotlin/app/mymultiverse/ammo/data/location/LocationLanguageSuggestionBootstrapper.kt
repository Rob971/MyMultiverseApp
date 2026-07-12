package app.mymultiverse.ammo.data.location

import app.mymultiverse.ammo.domain.location.DeviceRegionService
import app.mymultiverse.ammo.domain.location.LocationLanguageMapper
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Runs once on the very first app launch (before the user has ever chosen a language)
 * to pre-select an appropriate language based on the device's geographic region.
 *
 * Subsequent launches are no-ops: once [SupportedAppLanguages.SETTINGS_KEY] is persisted,
 * the user's explicit or detected choice is preserved forever.
 *
 * Call [bootstrapIfFirstLaunch] from the app's root composable init effect.
 */
class LocationLanguageSuggestionBootstrapper(
    private val settings: Settings,
    private val languageManager: LanguageManager,
    private val deviceRegionService: DeviceRegionService,
    private val scope: CoroutineScope,
) {
    /**
     * Detects the device region and applies the matching language, but only when no
     * language preference has been stored yet. Safe to call multiple times.
     */
    fun bootstrapIfFirstLaunch() {
        if (settings.hasKey(SupportedAppLanguages.SETTINGS_KEY)) return

        scope.launch {
            val region = runCatching { deviceRegionService.getRegion() }.getOrNull()
            val code = LocationLanguageMapper.mapToLanguageCode(region)
            languageManager.changeLanguage(code)
        }
    }
}
