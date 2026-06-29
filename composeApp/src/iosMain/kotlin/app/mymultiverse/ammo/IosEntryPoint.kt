package app.mymultiverse.ammo

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import app.mymultiverse.ammo.presentation.App
import app.mymultiverse.ammo.presentation.di.appModule
import org.koin.compose.KoinApplication

fun MainViewController() = ComposeUIViewController(
    configure = {
        onFocusBehavior = OnFocusBehavior.DoNothing
    },
) {
    KoinApplication(application = {
        modules(appModule)
    }) {
        App()
    }
}
