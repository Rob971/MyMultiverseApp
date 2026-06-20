package app.mymultiverse.kmp

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import app.mymultiverse.kmp.presentation.App
import app.mymultiverse.kmp.presentation.di.appModule
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
