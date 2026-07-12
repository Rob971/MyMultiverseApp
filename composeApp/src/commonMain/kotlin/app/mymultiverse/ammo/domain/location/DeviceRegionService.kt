package app.mymultiverse.ammo.domain.location

/**
 * Platform service that resolves the device's current geographic region without prompting
 * the user for location permission.
 *
 * Implementations use only already-granted permissions and cached data (last-known
 * location, network cell info, device locale). If the region cannot be determined
 * reliably, implementations return null so callers can fall back gracefully.
 */
interface DeviceRegionService {
    /**
     * Returns the best-effort [DeviceRegion] for the current device, or null when
     * no reliable region can be determined.
     *
     * This is a suspend function because sub-region resolution (e.g. reverse-geocoding)
     * may involve brief I/O. It must not block the main thread and must complete quickly
     * (implementations should apply an internal timeout of ≤ 5 s).
     */
    suspend fun getRegion(): DeviceRegion?
}
