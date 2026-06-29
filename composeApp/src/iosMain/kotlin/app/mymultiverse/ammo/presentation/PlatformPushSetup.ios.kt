package app.mymultiverse.ammo.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.mymultiverse.ammo.IosPushTokenBridge
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.presentation.screens.home.HomeScreenModel
import kotlin.coroutines.resume
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.compose.koinInject
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.sel_registerName

@Composable
actual fun PlatformPushSetup() {
    val authRepository = koinInject<AuthRepository>()
    val homeScreenModel = koinInject<HomeScreenModel>()
    val authState by authRepository.authState.collectAsState()

    DisposableEffect(homeScreenModel) {
        IosPushTokenBridge.setOnTokenUpdated {
            homeScreenModel.refresh()
        }
        onDispose {
            IosPushTokenBridge.setOnTokenUpdated(null)
        }
    }

    LaunchedEffect(authState is AuthState.Authenticated) {
        if (authState !is AuthState.Authenticated) return@LaunchedEffect
        val granted = requestNotificationAuthorization()
        if (granted) {
            dispatch_async(dispatch_get_main_queue()) {
                registerForRemoteNotifications()
            }
        }
        homeScreenModel.refresh()
    }
}

private suspend fun requestNotificationAuthorization(): Boolean =
    suspendCancellableCoroutine { continuation ->
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or
                UNAuthorizationOptionSound or
                UNAuthorizationOptionBadge,
            completionHandler = { granted, _ ->
                if (continuation.isActive) {
                    continuation.resume(granted)
                }
            },
        )
    }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun registerForRemoteNotifications() {
    UIApplication.sharedApplication.performSelector(sel_registerName("registerForRemoteNotifications"))
}
