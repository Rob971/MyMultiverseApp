package com.example.kmp.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.example.kmp.presentation.di.appModule
import com.example.kmp.presentation.screens.home.HomeScreen
import com.example.kmp.presentation.theme.AppTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        AppTheme {
            Navigator(screen = HomeScreen) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
