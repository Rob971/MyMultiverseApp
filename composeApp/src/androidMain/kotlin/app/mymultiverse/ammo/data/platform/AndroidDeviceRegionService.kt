package app.mymultiverse.ammo.data.platform

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.mymultiverse.ammo.domain.location.DeviceRegion
import app.mymultiverse.ammo.domain.location.DeviceRegionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Android implementation of [DeviceRegionService].
 *
 * Country detection uses [Locale.getDefault] — no permission required.
 * Sub-region (admin area) detection uses the last-known location from [LocationManager]
 * combined with [Geocoder] reverse-geocoding, and is attempted **only** when
 * [android.Manifest.permission.ACCESS_COARSE_LOCATION] is already granted and the
 * device country is Italy (IT). This prevents unnecessary location access for users
 * outside Italy while enabling the Campania → Neapolitan special case.
 */
class AndroidDeviceRegionService(
    private val context: Context,
) : DeviceRegionService {

    override suspend fun getRegion(): DeviceRegion? {
        val countryCode = Locale.getDefault().country.uppercase().ifBlank { return null }

        val adminArea = if (countryCode == "IT") {
            withTimeoutOrNull(REGION_TIMEOUT_MS) { tryResolveAdminArea() }
        } else {
            null
        }

        return DeviceRegion(countryCode = countryCode, adminArea = adminArea)
    }

    @Suppress("MissingPermission") // Permission is checked via hasCoarseLocationPermission()
    private suspend fun tryResolveAdminArea(): String? {
        if (!hasCoarseLocationPermission()) return null

        val location = withContext(Dispatchers.IO) {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext null

            LOCATION_PROVIDERS
                .mapNotNull { provider ->
                    runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
                }
                .maxByOrNull { it.time }
        } ?: return null

        return reverseGeocodeAdminArea(location)
    }

    private suspend fun reverseGeocodeAdminArea(location: Location): String? {
        if (!Geocoder.isPresent()) return null

        val geocoder = Geocoder(context, Locale.ENGLISH)
        val lat = location.latitude
        val lon = location.longitude

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(lat, lon, MAX_RESULTS) { addresses ->
                    continuation.resume(addresses.firstOrNull()?.adminArea)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                @Suppress("DEPRECATION")
                runCatching {
                    geocoder.getFromLocation(lat, lon, MAX_RESULTS)?.firstOrNull()?.adminArea
                }.getOrNull()
            }
        }
    }

    private fun hasCoarseLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val REGION_TIMEOUT_MS = 5_000L
        const val MAX_RESULTS = 1
        val LOCATION_PROVIDERS = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
        )
    }
}
