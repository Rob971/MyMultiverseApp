package app.mymultiverse.kmp.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.data.observability.ObservabilityInitializer
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.observability.CrashReporter
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.navigation.AppRoute
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.navigation.rememberAppNavigator
import app.mymultiverse.kmp.presentation.screens.auth.LoginScreen
import app.mymultiverse.kmp.presentation.screens.home.HomeScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionFlow
import app.mymultiverse.kmp.presentation.theme.AppTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    AppTheme {
        val authRepository = koinInject<AuthRepository>()
        val logger = koinInject<AppLogger>()
        val crashReporter = koinInject<CrashReporter>()
        val diagnostics = koinInject<DiagnosticsContext>()
        val authState by authRepository.authState.collectAsState(initial = AuthState.Loading)

        LaunchedEffect(Unit) {
            ObservabilityInitializer.start(logger, crashReporter, diagnostics)
        }

        LaunchedEffect(authState) {
            when (val state = authState) {
                is AuthState.Authenticated -> {
                    diagnostics.userId = state.user.id
                    crashReporter.setUserId(state.user.id)
                    logger.breadcrumb("auth_authenticated")
                }
                AuthState.Unauthenticated -> {
                    diagnostics.userId = null
                    crashReporter.setUserId(null)
                }
                else -> Unit
            }
        }

        LaunchedEffect(authRepository) {
            authRepository.restoreSession()
        }

        NapolitanBackground {
            when (val state = authState) {
                AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                AuthState.Unauthenticated,
                AuthState.ConfigurationMissing,
                -> {
                    LoginScreen(
                        showConfigMissing = state is AuthState.ConfigurationMissing,
                    )
                }

                is AuthState.Authenticated -> {
                    AuthenticatedApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AuthenticatedApp() {
    val navigator = rememberAppNavigator()
    val route = navigator.current

    BackHandler(enabled = navigator.canGoBack) {
        navigator.navigateBack()
    }

    when (route) {
        AppRoute.Home -> HomeScreen(
            onOpenNutrition = {
                navigator.navigateTo(AppRoute.Nutrition())
            },
        )

        is AppRoute.Nutrition -> NutritionFlow(
            space = route.space,
            section = route.section,
            onBack = navigator::navigateBack,
            onOpenSection = { section ->
                navigator.navigateTo(
                    AppRoute.Nutrition(
                        space = route.space,
                        section = section,
                    ),
                )
            },
            onSpaceSelected = { space ->
                navigator.navigateTo(
                    AppRoute.Nutrition(
                        space = space,
                        section = NutritionSection.Hub,
                    ),
                )
            },
        )
    }
}
