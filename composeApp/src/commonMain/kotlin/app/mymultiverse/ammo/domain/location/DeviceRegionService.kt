package app.mymultiverse.ammo.domain.location

/**
 * Platform service that resolves the device's current geographic region.
 *
 * The two methods have very different cost profiles:
 * - [getLocaleCountryCode] is synchronous and free — reads the device locale only.
 * - [getRegion] is async and may use cached location when permission is already granted.
 *
 * Neither method ever requests a runtime permission by itself. Permission prompts are
 * the responsibility of the presentation layer.
 */
interface DeviceRegionService {
    /**
     * Returns the ISO 3166-1 alpha-2 country code derived from the device locale
     * (e.g. "IT", "FR"). Synchronous, no I/O, no permissions.
     * Returns null if the locale carries no country.
     */
    fun getLocaleCountryCode(): String?

    /**
     * Returns the best-effort [DeviceRegion] for the current device, or null when
     * no reliable region can be determined.
     *
     * For sub-region resolution (e.g. reverse-geocoding to detect Campania) this uses
     * cached last-known location **only when the caller has already obtained
     * coarse-location permission**. Implementations must complete within ≤ 5 s.
     */
    suspend fun getRegion(): DeviceRegion?
}
