package app.mymultiverse.ammo.data.location

import app.mymultiverse.ammo.domain.location.DeviceRegionService
import app.mymultiverse.ammo.domain.location.LocationLanguageMapper
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Handles language pre-selection on the very first app launch.
 *
 * The flow is intentionally split into two stages so the presentation layer can
 * insert a location-permission prompt between them for Italian users:
 *
 * 1. The composable calls [needsLocationPermissionForLanguage] to decide whether
 *    to show a permission dialog.
 * 2. After the dialog result is known, the composable calls [bootstrapIfFirstLaunch].
 *    The [DeviceRegionService] will use the cached location only if permission was
 *    granted; otherwise it falls back to locale-only detection.
 *
 * On all subsequent launches [isFirstLaunch] returns false and nothing happens.
 */
class LocationLanguageSuggestionBootstrapper(
    private val settings: Settings,
    private val languageManager: LanguageManager,
    private val deviceRegionService: DeviceRegionService,
    private val scope: CoroutineScope,
) {
    /** True only when no language preference has been persisted yet. */
    fun isFirstLaunch(): Boolean = !settings.hasKey(SupportedAppLanguages.SETTINGS_KEY)

    /**
     * Returns the device's country code from the locale (synchronous, no I/O).
     * Delegates to [DeviceRegionService.getLocaleCountryCode].
     */
    fun localeCountryCode(): String? = deviceRegionService.getLocaleCountryCode()

    /**
     * Returns true when a location-permission dialog should be shown before
     * bootstrapping: only on the first launch when the device is in Italy, because
     * Italy is the only supported locale where a sub-region (Campania) maps to a
     * different language (Neapolitan).
     */
    fun needsLocationPermissionForLanguage(): Boolean =
        isFirstLaunch() && localeCountryCode()?.uppercase() == "IT"

    /**
     * Detects the device region and applies the matching language, but only when no
     * language preference has been stored yet. Safe to call multiple times.
     *
     * For Italian users this should be called **after** the location-permission
     * dialog has been resolved so that [DeviceRegionService.getRegion] can access
     * the cached location if the user granted permission.
     */
    fun bootstrapIfFirstLaunch() {
        if (!isFirstLaunch()) return

        scope.launch {
            val region = runCatching { deviceRegionService.getRegion() }.getOrNull()
            val code = LocationLanguageMapper.mapToLanguageCode(region)
            languageManager.changeLanguage(code)
        }
    }
}
