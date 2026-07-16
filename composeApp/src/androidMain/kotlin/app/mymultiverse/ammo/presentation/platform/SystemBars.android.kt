package app.mymultiverse.ammo.presentation.platform

import android.app.Activity
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

@Composable
actual fun ConfigureSystemBars(darkTheme: Boolean) {
    val activity = LocalActivity.current as? Activity ?: return
    val window = activity.window
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    SideEffect {
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }
}
