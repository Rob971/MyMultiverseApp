package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import app.mymultiverse.kmp.presentation.components.screenListPadding
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import app.mymultiverse.kmp.domain.AppBuildInfo
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.components.JourneyBanner
import app.mymultiverse.kmp.presentation.components.LanguagePicker
import app.mymultiverse.kmp.presentation.components.PendingInvitesCard
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import app.mymultiverse.kmp.presentation.screens.household.InviteActionMessage
import org.koin.compose.koinInject

object HomeTestTags {
    const val NUTRITION_CARD = "home_nutrition_card"
    const val HOUSEHOLD_CARD = "home_household_card"
    const val SIGN_OUT_BUTTON = "home_sign_out_button"
    const val APP_VERSION_LABEL = "home_app_version_label"
    const val LOADING_INDICATOR = "home_loading_indicator"
    const val GREETING_LINE = "home_greeting_line"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenNutrition: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
) {
    val screenModel = koinInject<HomeScreenModel>()
    val greeting by screenModel.greeting.collectAsState()
    val userDisplayName by screenModel.userDisplayName.collectAsState()
    val household by screenModel.household.collectAsState()
    val hasActiveHousehold by screenModel.hasActiveHousehold.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()
    val pendingInvites by screenModel.pendingInvites.collectAsState()
    val inviteActionMessage by screenModel.inviteActionMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val inviteErrorMessage = stringResource(Res.string.auth_pending_invites_error_generic)

    LaunchedEffect(inviteActionMessage) {
        if (inviteActionMessage == InviteActionMessage.AcceptFailed) {
            snackbarHostState.showSnackbar(inviteErrorMessage)
            screenModel.clearInviteActionMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    TextButton(
                        onClick = { screenModel.signOut() },
                        modifier = Modifier.testTag(HomeTestTags.SIGN_OUT_BUTTON),
                    ) {
                        Text(stringResource(Res.string.auth_sign_out))
                    }
                    LanguagePicker()
                },
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        HomeContent(
            greeting = greeting,
            userDisplayName = userDisplayName,
            householdName = household?.name,
            hasActiveHousehold = hasActiveHousehold,
            isRefreshing = isRefreshing,
            pendingInvites = pendingInvites,
            onRefreshClick = { screenModel.refresh() },
            onOpenNutrition = onOpenNutrition,
            onOpenHouseholdMembers = onOpenHouseholdMembers,
            onSignOut = { screenModel.signOut() },
            onAcceptInvite = screenModel::acceptInvite,
            onDeclineInvite = screenModel::declineInvite,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun HomeContent(
    greeting: Greeting?,
    userDisplayName: String?,
    householdName: String?,
    hasActiveHousehold: Boolean,
    isRefreshing: Boolean,
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.SpaceInvite>,
    onRefreshClick: () -> Unit,
    onOpenNutrition: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
    onSignOut: () -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val comingSoonLabel = stringResource(Res.string.home_logistics_coming_soon)
    val requiresHouseholdLabel = stringResource(Res.string.home_logistics_requires_household)
    val greetingSelection = HomeGreetingSelection.select(
        greetingReady = greeting != null,
        userDisplayName = userDisplayName,
    )
    val supportingLine = when (greetingSelection) {
        HomeGreetingSelection.Loading -> stringResource(Res.string.home_banner_loading)
        is HomeGreetingSelection.Personalized -> stringResource(
            Res.string.home_greeting_personalized,
            greetingSelection.name,
        )
        HomeGreetingSelection.Generic -> stringResource(Res.string.home_greeting)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
        contentPadding = screenListPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
            item {
                JourneyBanner(
                    headline = stringResource(Res.string.home_banner_headline),
                    supportingLine = supportingLine,
                    description = stringResource(Res.string.home_banner_description),
                    supportingLineTestTag = HomeTestTags.GREETING_LINE,
                )
            }

            item {
                PendingInvitesCard(
                    invites = pendingInvites,
                    onAccept = onAcceptInvite,
                    onDecline = onDeclineInvite,
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.home_household_title),
                    description = householdName?.let {
                        stringResource(Res.string.home_household_description_named, it)
                    } ?: stringResource(Res.string.home_household_description),
                    accentColor = SharedJourneyColors.MediterraneanTeal,
                    icon = AppIcons.Person,
                    modifier = Modifier.testTag(HomeTestTags.HOUSEHOLD_CARD),
                    onClick = onOpenHouseholdMembers,
                )
            }

            item {
                if (greeting == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = SharedJourneyColors.MediterraneanTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag(HomeTestTags.LOADING_INDICATOR),
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.home_dreams_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SharedJourneyColors.InkDeep,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.home_logistics_nutrition_title),
                    description = stringResource(Res.string.home_logistics_nutrition_description),
                    accentColor = SharedJourneyColors.SageSoft,
                    icon = AppIcons.Restaurant,
                    modifier = Modifier.testTag(HomeTestTags.NUTRITION_CARD),
                    enabled = hasActiveHousehold,
                    badge = if (hasActiveHousehold) null else requiresHouseholdLabel,
                    onClick = onOpenNutrition,
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.home_logistics_adventures_title),
                    description = stringResource(Res.string.home_logistics_adventures_description),
                    accentColor = SharedJourneyColors.TerracottaOrange,
                    icon = AppIcons.Explore,
                    enabled = false,
                    badge = comingSoonLabel,
                    onClick = {},
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.home_logistics_budget_title),
                    description = stringResource(Res.string.home_logistics_budget_description),
                    accentColor = SharedJourneyColors.MediterraneanTeal,
                    icon = AppIcons.AccountBalance,
                    enabled = false,
                    badge = comingSoonLabel,
                    onClick = {},
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TextButton(
                        onClick = onRefreshClick,
                        enabled = !isRefreshing,
                    ) {
                        Text(
                            stringResource(Res.string.home_refresh_inspirations),
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.InkMuted,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            item {
                val versionLabel =
                    if (AppBuildInfo.IS_RELEASE_CANDIDATE) {
                        stringResource(Res.string.home_app_version_rc, AppBuildInfo.VERSION_NAME)
                    } else {
                        stringResource(Res.string.home_app_version, AppBuildInfo.VERSION_NAME)
                    }
                Text(
                    text = versionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = SharedJourneyColors.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag(HomeTestTags.APP_VERSION_LABEL),
                    textAlign = TextAlign.Center,
                )
            }
        }
}
