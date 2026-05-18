package app.mymultiverse.kmp.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import app.mymultiverse.kmp.presentation.screens.home.HomeScreen
import app.mymultiverse.kmp.presentation.theme.AppTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        AppTheme {
            Navigator(screen = HomeScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
