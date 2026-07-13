package app.mymultiverse.ammo.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.mymultiverse.ammo.data.location.LocationLanguageSuggestionBootstrapper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.compose.koinInject
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject
import kotlin.coroutines.resume

@Composable
actual fun PlatformLocationPermissionSetup() {
    val bootstrapper = koinInject<LocationLanguageSuggestionBootstrapper>()

    if (!bootstrapper.isFirstLaunch()) return

    LaunchedEffect(Unit) {
        if (bootstrapper.needsLocationPermissionForLanguage()) {
            // Italy on first launch: ask for WhenInUse authorization so Geocoder
            // can determine whether the user is in Campania.
            requestWhenInUseAuthorizationIfNeeded()
        }
        // Bootstrap after permission result (or immediately for non-Italy).
        bootstrapper.bootstrapIfFirstLaunch()
    }
}

/**
 * Suspends until location authorization is determined.
 * If status is already decided (granted or denied), returns immediately.
 * Does nothing if CoreLocation services are unavailable.
 */
private suspend fun requestWhenInUseAuthorizationIfNeeded() {
    val status = CLLocationManager.authorizationStatus()
    if (status != kCLAuthorizationStatusNotDetermined) return

    suspendCancellableCoroutine { continuation ->
        val manager = CLLocationManager()
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                val newStatus = manager.authorizationStatus
                if (newStatus != kCLAuthorizationStatusNotDetermined) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
        manager.delegate = delegate
        manager.requestWhenInUseAuthorization()

        continuation.invokeOnCancellation {
            manager.delegate = null
        }
    }
}
