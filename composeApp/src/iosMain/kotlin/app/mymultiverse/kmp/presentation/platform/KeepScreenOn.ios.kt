package app.mymultiverse.kmp.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    DisposableEffect(enabled) {
        val application = UIApplication.sharedApplication
        val previous = application.idleTimerDisabled
        if (enabled) {
            application.idleTimerDisabled = true
        }
        onDispose {
            application.idleTimerDisabled = previous
        }
    }
}
