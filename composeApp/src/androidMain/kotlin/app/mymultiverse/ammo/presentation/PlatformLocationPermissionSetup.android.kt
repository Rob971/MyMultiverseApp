package app.mymultiverse.ammo.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.mymultiverse.ammo.data.location.LocationLanguageSuggestionBootstrapper
import org.koin.compose.koinInject

@Composable
actual fun PlatformLocationPermissionSetup() {
    val bootstrapper = koinInject<LocationLanguageSuggestionBootstrapper>()

    if (!bootstrapper.isFirstLaunch()) return

    if (!bootstrapper.needsLocationPermissionForLanguage()) {
        // Non-Italy first launch: apply locale-based language right away.
        LaunchedEffect(Unit) { bootstrapper.bootstrapIfFirstLaunch() }
        return
    }

    // Italy on first launch: show the coarse-location permission dialog so we can
    // determine whether the user is in Campania → Neapolitan, or elsewhere → Italian.
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Whether the user grants or denies, run the bootstrap.
        // AndroidDeviceRegionService will use cached location only if granted;
        // otherwise it falls back to locale-only ("it").
        bootstrapper.bootstrapIfFirstLaunch()
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            bootstrapper.bootstrapIfFirstLaunch()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}
