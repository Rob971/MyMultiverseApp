package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.koin.compose.koinInject

object HomeTestTags {
    const val NUTRITION_CARD = "home_nutrition_card"
    const val SIGN_OUT_BUTTON = "home_sign_out_button"
    const val APP_VERSION_LABEL = "home_app_version_label"
}

@Composable
fun HomeScreen(
    onOpenNutrition: () -> Unit,
) {
    val screenModel = koinInject<HomeScreenModel>()
    val greeting by screenModel.greeting.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()
    val pendingInvites by screenModel.pendingInvites.collectAsState()

    HomeContent(
        greeting = greeting,
        isRefreshing = isRefreshing,
        pendingInvites = pendingInvites,
        onRefreshClick = { screenModel.refresh() },
        onOpenNutrition = onOpenNutrition,
        onSignOut = { screenModel.signOut() },
        onAcceptInvite = screenModel::acceptInvite,
        onDeclineInvite = screenModel::declineInvite,
    )
}

@Composable
fun HomeContent(
    greeting: Greeting?,
    isRefreshing: Boolean,
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.SpaceInvite>,
    onRefreshClick: () -> Unit,
    onOpenNutrition: () -> Unit,
    onSignOut: () -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
) {
    val comingSoonLabel = stringResource(Res.string.home_logistics_coming_soon)

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    TextButton(
                        onClick = onSignOut,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = screenListPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                JourneyBanner(
                    headline = stringResource(Res.string.home_banner_headline),
                    supportingLine =
                        when (greeting) {
                            null -> stringResource(Res.string.home_banner_loading)
                            else -> stringResource(Res.string.home_greeting)
                        },
                    description = stringResource(Res.string.home_banner_description),
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
                if (greeting == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = SharedJourneyColors.MediterraneanTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp),
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
}
