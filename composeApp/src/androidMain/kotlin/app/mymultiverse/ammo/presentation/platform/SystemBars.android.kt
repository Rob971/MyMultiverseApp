package app.mymultiverse.ammo.presentation.platform

import android.app.Activity
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
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }
}
