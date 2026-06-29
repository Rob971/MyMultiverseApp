package app.mymultiverse.ammo.presentation

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
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.manager.AppThemePreferences
import app.mymultiverse.ammo.domain.manager.ThemeManager
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.presentation.components.NapolitanBackground
import app.mymultiverse.ammo.presentation.navigation.AppNavigator
import app.mymultiverse.ammo.data.invite.HouseholdPushEvents
import app.mymultiverse.ammo.presentation.navigation.AppRoute
import app.mymultiverse.ammo.presentation.navigation.AppMainTab
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext
import app.mymultiverse.ammo.presentation.navigation.MainTabShell
import app.mymultiverse.ammo.presentation.navigation.NutritionSection
import app.mymultiverse.ammo.presentation.navigation.rememberAppNavigator
import app.mymultiverse.ammo.presentation.platform.ConfigureSystemBars
import app.mymultiverse.ammo.presentation.PlatformPushSetup
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.presentation.navigation.resolvePostAuthRoute
import app.mymultiverse.ammo.presentation.navigation.shouldBlockAuthenticatedShell
import app.mymultiverse.ammo.presentation.screens.onboarding.AuthScreen
import app.mymultiverse.ammo.presentation.screens.auth.LoginScreen
import app.mymultiverse.ammo.presentation.screens.householdsetup.HouseholdCreationScreen
import app.mymultiverse.ammo.presentation.screens.home.HomePhase
import app.mymultiverse.ammo.presentation.screens.home.HomeScreen
import app.mymultiverse.ammo.presentation.screens.home.HomeScreenModel
import app.mymultiverse.ammo.presentation.screens.home.PostCreateFocusTarget
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersFlow
import app.mymultiverse.ammo.presentation.invite.InviteJoinAcceptError
import app.mymultiverse.ammo.presentation.invite.InviteJoinAcceptState
import app.mymultiverse.ammo.presentation.invite.InviteJoinFlowCoordinator
import app.mymultiverse.ammo.presentation.screens.invite.InviteEmailMismatchScreen
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionFlow
import app.mymultiverse.ammo.presentation.theme.AppTheme
import app.mymultiverse.ammo.presentation.theme.ProvideAppDarkTheme
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

        val navigator = rememberAppNavigator(startDestination = AppRoute.Onboarding)

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
                    var showEmailAuth by rememberSaveable { mutableStateOf(false) }
                    LaunchedEffect(state) {
                        showEmailAuth = false
                        navigator.replaceCurrent(AppRoute.Onboarding)
                    }
                    if (showEmailAuth) {
                        LoginScreen(
                            showConfigMissing = state is AuthState.ConfigurationMissing,
                            showBackToSso = true,
                            onBackToSso = { showEmailAuth = false },
                        )
                    } else {
                        AuthScreen(
                            pendingInviteToken = pendingInviteToken,
                            showConfigMissing = state is AuthState.ConfigurationMissing,
                            onContinueWithEmail = { showEmailAuth = true },
                        )
                    }
                }

                is AuthState.Authenticated -> {
                    AuthenticatedApp(
                        navigator = navigator,
                    )
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AuthenticatedApp(
    navigator: AppNavigator,
) {
    val authRepository = koinInject<AuthRepository>()
    val householdRepository = koinInject<HouseholdRepository>()
    val inviteFlow = koinInject<InviteJoinFlowCoordinator>()
    val pendingInviteToken by inviteFlow.pendingInviteToken.collectAsState()
    val acceptState by inviteFlow.acceptState.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val membership by householdRepository.observeMembershipStatus().collectAsState(
        initial = HouseholdMembershipStatus.Loading,
    )

    LaunchedEffect(Unit) {
        householdRepository.refreshMembership()
    }

    LaunchedEffect(pendingInviteToken) {
        if (!pendingInviteToken.isNullOrBlank()) {
            inviteFlow.acceptPendingInviteIfNeeded()
        }
    }

    LaunchedEffect(membership, pendingInviteToken, acceptState) {
        resolvePostAuthRoute(
            membership = membership,
            pendingInviteToken = pendingInviteToken,
            acceptState = acceptState,
        )?.let { route ->
            navigator.replaceCurrent(route)
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

    if (shouldBlockAuthenticatedShell(membership, pendingInviteToken, acceptState)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    when (val route = navigator.current) {
        AppRoute.HouseholdSetup -> {
            HouseholdCreationScreen(
                onHouseholdCreated = { householdId ->
                    navigator.replaceCurrent(AppRoute.Dashboard(householdId = householdId))
                },
            )
        }

        is AppRoute.Dashboard -> {
            AuthenticatedMainApp()
        }

        AppRoute.Onboarding,
        AppRoute.Home,
        is AppRoute.HouseholdMembers,
        is AppRoute.Nutrition,
        -> {
            AuthenticatedMainApp()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AuthenticatedMainApp() {
    val navigator = rememberAppNavigator()
    val route = navigator.current
    val homeScreenModel = koinInject<HomeScreenModel>()
    val homePhase by homeScreenModel.homePhase.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(AppMainTab.Home) }
    var nutritionHousehold by remember { mutableStateOf<HouseholdContext?>(null) }

    val nutritionContext by homeScreenModel.nutritionHouseholdContext.collectAsState()
    val resolvedHousehold = nutritionHousehold ?: nutritionContext

    LaunchedEffect(nutritionContext?.id) {
        if (nutritionContext != null) {
            nutritionHousehold = nutritionContext
        }
    }

    LaunchedEffect(Unit) {
        HouseholdPushEvents.consumePendingMemberJoinedHouseholdId()?.let { householdId ->
            if (nutritionContext?.id == householdId || resolvedHousehold?.id == householdId) {
                navigator.navigateTo(
                    AppRoute.HouseholdMembers(household = resolvedHousehold ?: nutritionContext),
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        HouseholdPushEvents.memberJoinedHouseholdIds.collect { householdId ->
            if (nutritionContext?.id == householdId || resolvedHousehold?.id == householdId) {
                navigator.navigateTo(
                    AppRoute.HouseholdMembers(household = resolvedHousehold ?: nutritionContext),
                )
            }
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

        AppRoute.Home,
        AppRoute.Onboarding,
        AppRoute.HouseholdSetup,
        is AppRoute.Dashboard,
        -> Unit
    }

    val showBottomBar = when (selectedTab) {
        AppMainTab.Home -> homePhase is HomePhase.Welcome
        else -> true
    }

    val postCreateFocus by homeScreenModel.postCreateFocus.collectAsState()
    LaunchedEffect(postCreateFocus) {
        when (postCreateFocus) {
            PostCreateFocusTarget.Grocery -> {
                selectedTab = AppMainTab.Grocery
                homeScreenModel.consumePostCreateFocus()
            }
            null -> Unit
        }
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
                onOpenGrocery = { selectedTab = AppMainTab.Grocery },
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
