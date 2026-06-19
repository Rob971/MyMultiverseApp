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
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.navigation.AppRoute
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.navigation.rememberAppNavigator
import app.mymultiverse.kmp.presentation.PlatformPushSetup
import app.mymultiverse.kmp.presentation.screens.auth.LoginScreen
import app.mymultiverse.kmp.presentation.screens.home.HomeScreen
import app.mymultiverse.kmp.presentation.screens.household.HouseholdMembersFlow
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptError
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptState
import app.mymultiverse.kmp.presentation.invite.InviteJoinFlowCoordinator
import app.mymultiverse.kmp.presentation.screens.invite.InviteEmailMismatchScreen
import app.mymultiverse.kmp.presentation.screens.invite.JoinHouseholdScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionFlow
import app.mymultiverse.kmp.presentation.theme.AppTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    AppTheme {
        val authRepository = koinInject<AuthRepository>()
        val inviteFlow = koinInject<InviteJoinFlowCoordinator>()
        val logger = koinInject<AppLogger>()
        val authState by authRepository.authState.collectAsState(initial = AuthState.Loading)
        val pendingInviteToken by inviteFlow.pendingInviteToken.collectAsState()

        LaunchedEffect(Unit) {
            logger.startSession()
            inviteFlow.start()
        }

        PlatformPushSetup()

        LaunchedEffect(authState) {
            logger.onAuthStateChanged(authState)
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
                    val inviteToken = pendingInviteToken?.takeIf { it.isNotBlank() }
                    if (inviteToken != null) {
                        JoinHouseholdScreen(
                            inviteToken = inviteToken,
                            showConfigMissing = state is AuthState.ConfigurationMissing,
                        )
                    } else {
                        LoginScreen(
                            showConfigMissing = state is AuthState.ConfigurationMissing,
                        )
                    }
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
    val authRepository = koinInject<AuthRepository>()
    val inviteFlow = koinInject<InviteJoinFlowCoordinator>()
    val pendingInviteToken by inviteFlow.pendingInviteToken.collectAsState()
    val acceptState by inviteFlow.acceptState.collectAsState()
    val authState by authRepository.authState.collectAsState()

    LaunchedEffect(pendingInviteToken) {
        if (!pendingInviteToken.isNullOrBlank()) {
            inviteFlow.acceptPendingInviteIfNeeded()
        }
    }

    val emailMismatch = acceptState as? InviteJoinAcceptState.Failed
    if (emailMismatch?.error == InviteJoinAcceptError.EmailMismatch) {
        val mismatchContext = emailMismatch.mismatchContext
        if (mismatchContext != null) {
            InviteEmailMismatchScreen(
                invitedEmail = mismatchContext.invitedEmail,
                sessionEmail = (authState as? AuthState.Authenticated)?.user?.email.orEmpty(),
                onSignOutRetry = {
                    inviteFlow.retryAfterEmailMismatch { authRepository.signOut() }
                },
            )
            return
        }
    }

    if (!pendingInviteToken.isNullOrBlank() && acceptState is InviteJoinAcceptState.Accepting) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    AuthenticatedMainApp()
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AuthenticatedMainApp() {
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
            onOpenHouseholdMembers = {
                navigator.navigateTo(AppRoute.HouseholdMembers())
            },
        )

        is AppRoute.HouseholdMembers -> HouseholdMembersFlow(
            household = route.household,
            onBack = navigator::navigateBack,
            onHouseholdReady = { household ->
                navigator.replaceCurrent(AppRoute.HouseholdMembers(household = household))
            },
        )

        is AppRoute.Nutrition -> NutritionFlow(
            household = route.household,
            section = route.section,
            onBack = navigator::navigateBack,
            onOpenSection = { section ->
                navigator.navigateTo(
                    AppRoute.Nutrition(
                        household = route.household,
                        section = section,
                    ),
                )
            },
            onHouseholdSelected = { household ->
                navigator.replaceCurrent(
                    AppRoute.Nutrition(
                        household = household,
                        section = NutritionSection.Hub,
                    ),
                )
            },
        )
    }
}
