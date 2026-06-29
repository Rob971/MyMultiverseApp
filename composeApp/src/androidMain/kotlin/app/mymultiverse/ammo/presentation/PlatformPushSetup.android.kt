package app.mymultiverse.ammo.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.mymultiverse.ammo.data.observability.FirebaseBuildFlags
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.presentation.screens.home.HomeScreenModel
import org.koin.compose.koinInject

@Composable
actual fun PlatformPushSetup() {
    if (!FirebaseBuildFlags.PUSH_ENABLED) return

    val context = LocalContext.current
    val authRepository = koinInject<AuthRepository>()
    val homeScreenModel = koinInject<HomeScreenModel>()
    val authState by authRepository.authState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ ->
        homeScreenModel.refresh()
    }

    LaunchedEffect(authState is AuthState.Authenticated) {
        if (authState !is AuthState.Authenticated) return@LaunchedEffect
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            homeScreenModel.refresh()
            return@LaunchedEffect
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            homeScreenModel.refresh()
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
