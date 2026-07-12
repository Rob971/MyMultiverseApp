package app.mymultiverse.ammo.data.platform

import app.mymultiverse.ammo.domain.location.DeviceRegion
import app.mymultiverse.ammo.domain.location.DeviceRegionService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLPlacemark
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.currentLocale
import kotlin.coroutines.resume

/**
 * iOS implementation of [DeviceRegionService].
 *
 * Country detection uses [NSLocale.currentLocale] — no permission required.
 * Sub-region (admin area) detection uses [CLLocationManager.location] (cached last-known
 * position) combined with [CLGeocoder] reverse-geocoding, and is attempted **only**
 * when location services are already authorised and the device country is Italy (IT).
 * No permission prompt is ever raised by this service.
 */
class IosDeviceRegionService : DeviceRegionService {

    override suspend fun getRegion(): DeviceRegion? {
        val countryCode = (NSLocale.currentLocale.objectForKey(NSLocaleCountryCode) as? String)
            ?.uppercase()
            ?.ifBlank { null }
            ?: return null

        val adminArea = if (countryCode == "IT") {
            withTimeoutOrNull(REGION_TIMEOUT_MS) { tryResolveAdminArea() }
        } else {
            null
        }

        return DeviceRegion(countryCode = countryCode, adminArea = adminArea)
    }

    private suspend fun tryResolveAdminArea(): String? {
        val authStatus = CLLocationManager.authorizationStatus()
        val isAuthorized = authStatus == kCLAuthorizationStatusAuthorizedWhenInUse ||
            authStatus == kCLAuthorizationStatusAuthorizedAlways
        if (!isAuthorized) return null

        val location = CLLocationManager().location ?: return null

        return suspendCancellableCoroutine { continuation ->
            CLGeocoder().reverseGeocodeLocation(location) { placemarks, _ ->
                @Suppress("UNCHECKED_CAST")
                val adminArea = (placemarks as? List<CLPlacemark>)
                    ?.firstOrNull()
                    ?.administrativeArea
                continuation.resume(adminArea)
            }
        }
    }

    private companion object {
        const val REGION_TIMEOUT_MS = 5_000L
    }
}
