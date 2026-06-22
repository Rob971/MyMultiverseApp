package app.mymultiverse.kmp.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalAppDarkTheme = compositionLocalOf { false }

@Composable
fun ProvideAppDarkTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppDarkTheme provides darkTheme, content = content)
}

@Composable
fun isAppInDarkTheme(): Boolean = LocalAppDarkTheme.current
