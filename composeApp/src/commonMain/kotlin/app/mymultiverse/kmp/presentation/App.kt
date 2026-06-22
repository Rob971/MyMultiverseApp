package app.mymultiverse.kmp.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.manager.AppThemePreferences
import app.mymultiverse.kmp.domain.manager.ThemeManager
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.presentation.components.NapolitanBackground
import app.mymultiverse.kmp.presentation.navigation.AppMainTab
import app.mymultiverse.kmp.presentation.navigation.AppRoute
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
import app.mymultiverse.kmp.presentation.navigation.MainTabShell
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.navigation.rememberAppNavigator
import app.mymultiverse.kmp.presentation.navigation.toNavigationContext
import app.mymultiverse.kmp.presentation.platform.ConfigureSystemBars
import app.mymultiverse.kmp.presentation.PlatformPushSetup
import app.mymultiverse.kmp.presentation.screens.auth.LoginScreen
import app.mymultiverse.kmp.presentation.screens.home.HomePhase
import app.mymultiverse.kmp.presentation.screens.home.HomeScreen
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import app.mymultiverse.kmp.presentation.screens.household.HouseholdMembersFlow
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptError
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptState
import app.mymultiverse.kmp.presentation.invite.InviteJoinFlowCoordinator
import app.mymultiverse.kmp.presentation.screens.invite.InviteEmailMismatchScreen
import app.mymultiverse.kmp.presentation.screens.invite.JoinHouseholdScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionFlow
import app.mymultiverse.kmp.presentation.theme.AppTheme
import app.mymultiverse.kmp.presentation.theme.ProvideAppDarkTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    val themeManager = koinInject<ThemeManager>()
    val themePreference by themeManager.currentPreference.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val darkTheme = AppThemePreferences.resolveDarkTheme(themePreference, systemDark)

    ProvideAppDarkTheme(darkTheme) {
        AppTheme(darkTheme = darkTheme) {
            ConfigureSystemBars(darkTheme = darkTheme)
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
    val homeScreenModel = koinInject<HomeScreenModel>()
    val homePhase by homeScreenModel.homePhase.collectAsState()
    val homeHousehold by homeScreenModel.household.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(AppMainTab.Home) }
    var nutritionHousehold by remember { mutableStateOf<HouseholdContext?>(null) }

    val resolvedHousehold = nutritionHousehold ?: homeHousehold?.toNavigationContext()

    LaunchedEffect(homeHousehold?.id) {
        if (homeHousehold != null) {
            nutritionHousehold = homeHousehold?.toNavigationContext()
        }
    }

    BackHandler(enabled = navigator.canGoBack) {
        navigator.navigateBack()
    }

    when (val overlayRoute = route) {
        is AppRoute.HouseholdMembers -> {
            HouseholdMembersFlow(
                household = overlayRoute.household,
                onBack = navigator::navigateBack,
                onHouseholdReady = { household ->
                    navigator.replaceCurrent(AppRoute.HouseholdMembers(household = household))
                },
            )
            return
        }

        is AppRoute.Nutrition -> {
            NutritionFlow(
                household = overlayRoute.household ?: resolvedHousehold,
                section = overlayRoute.section,
                initialAiMode = overlayRoute.initialAiMode,
                onBack = navigator::navigateBack,
                onOpenSection = { section, initialAiMode ->
                    navigator.navigateTo(
                        AppRoute.Nutrition(
                            household = overlayRoute.household ?: resolvedHousehold,
                            section = section,
                            initialAiMode = initialAiMode,
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
            return
        }

        AppRoute.Home -> Unit
    }

    val showBottomBar = when (selectedTab) {
        AppMainTab.Home -> homePhase is HomePhase.Welcome
        else -> true
    }

    MainTabShell(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        showBottomBar = showBottomBar,
        modifier = Modifier.fillMaxSize(),
    ) { contentModifier ->
        when (selectedTab) {
            AppMainTab.Home -> HomeScreen(
                onOpenMealPlan = { selectedTab = AppMainTab.MealPlan },
                onOpenHouseholdMembers = {
                    navigator.navigateTo(AppRoute.HouseholdMembers(household = resolvedHousehold))
                },
                embeddedInMainTabs = showBottomBar,
                modifier = contentModifier,
            )

            AppMainTab.MealPlan -> NutritionFlow(
                household = resolvedHousehold,
                section = NutritionSection.MealPlan,
                onBack = { selectedTab = AppMainTab.Home },
                onOpenSection = { section, initialAiMode ->
                    navigator.navigateTo(
                        AppRoute.Nutrition(
                            household = resolvedHousehold,
                            section = section,
                            initialAiMode = initialAiMode,
                        ),
                    )
                },
                onHouseholdSelected = { nutritionHousehold = it },
                embeddedInTabs = true,
            )

            AppMainTab.Grocery -> NutritionFlow(
                household = resolvedHousehold,
                section = NutritionSection.Grocery,
                onBack = { selectedTab = AppMainTab.Home },
                onOpenSection = { _, _ -> },
                onHouseholdSelected = { nutritionHousehold = it },
                embeddedInTabs = true,
            )
        }
    }
}
