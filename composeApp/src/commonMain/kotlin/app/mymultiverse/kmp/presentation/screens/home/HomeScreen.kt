package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptState
import app.mymultiverse.kmp.presentation.invite.InviteJoinFlowCoordinator
import app.mymultiverse.kmp.presentation.screens.household.InviteActionMessage
import org.koin.compose.koinInject

object HomeTestTags {
    const val NUTRITION_CARD = "home_nutrition_card"
    const val HOUSEHOLD_CARD = "home_household_card"
    const val SIGN_OUT_BUTTON = "home_sign_out_button"
    const val EXPORT_DATA_BUTTON = "home_export_personal_data_button"
    const val DELETE_ACCOUNT_BUTTON = "home_delete_account_button"
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
    val authRepository = koinInject<AuthRepository>()
    val inviteFlow = koinInject<InviteJoinFlowCoordinator>()
    val inviteAcceptState by inviteFlow.acceptState.collectAsState()
    val greeting by screenModel.greeting.collectAsState()
    val userDisplayName by screenModel.userDisplayName.collectAsState()
    val household by screenModel.household.collectAsState()
    val hasActiveHousehold by screenModel.hasActiveHousehold.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()
    val pendingInvites by screenModel.pendingInvites.collectAsState()
    val inviteActionMessage by screenModel.inviteActionMessage.collectAsState()
    val switchHouseholdPrompt by screenModel.switchHouseholdPrompt.collectAsState()
    val personalDataExportMessage by screenModel.personalDataExportMessage.collectAsState()
    val deleteAccountMessage by screenModel.deleteAccountMessage.collectAsState()
    val showDeleteAccountDialog by screenModel.showDeleteAccountDialog.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val inviteErrorMessage = stringResource(Res.string.auth_pending_invites_error_generic)
    val sessionEmail = (authState as? AuthState.Authenticated)?.user?.email.orEmpty()
    val emailMismatchMessage = stringResource(
        Res.string.auth_pending_invites_email_mismatch,
        pendingInvites.firstOrNull()?.email.orEmpty(),
        sessionEmail,
    )
    val exportSuccessMessage = stringResource(Res.string.home_export_personal_data_success)
    val exportShareUnavailableMessage = stringResource(Res.string.home_export_personal_data_share_unavailable)
    val exportErrorMessage = stringResource(Res.string.home_export_personal_data_error)
    val deleteAccountSuccessMessage = stringResource(Res.string.home_delete_account_success)
    val deleteAccountOwnerMessage = stringResource(Res.string.home_delete_account_error_owner)
    val deleteAccountErrorMessage = stringResource(Res.string.home_delete_account_error_generic)
    val joinedInviteMessage = (inviteActionMessage as? InviteActionMessage.Joined)?.householdName
        ?.let { name -> stringResource(Res.string.auth_household_joined_success, name) }
    val inviteFlowJoinedMessage = (inviteAcceptState as? InviteJoinAcceptState.Succeeded)?.householdName
        ?.let { name -> stringResource(Res.string.auth_household_joined_success, name) }

    LaunchedEffect(joinedInviteMessage) {
        joinedInviteMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            screenModel.clearInviteActionMessage()
        }
    }

    LaunchedEffect(inviteFlowJoinedMessage) {
        inviteFlowJoinedMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            inviteFlow.clearAcceptSuccess()
        }
    }

    LaunchedEffect(personalDataExportMessage) {
        when (personalDataExportMessage) {
            PersonalDataExportMessage.Success -> {
                snackbarHostState.showSnackbar(exportSuccessMessage)
                screenModel.clearPersonalDataExportMessage()
            }
            PersonalDataExportMessage.ShareUnavailable -> {
                snackbarHostState.showSnackbar(exportShareUnavailableMessage)
                screenModel.clearPersonalDataExportMessage()
            }
            PersonalDataExportMessage.Error -> {
                snackbarHostState.showSnackbar(exportErrorMessage)
                screenModel.clearPersonalDataExportMessage()
            }
            null -> Unit
        }
    }

    LaunchedEffect(deleteAccountMessage) {
        when (deleteAccountMessage) {
            DeleteAccountMessage.Success -> {
                snackbarHostState.showSnackbar(deleteAccountSuccessMessage)
                screenModel.clearDeleteAccountMessage()
            }
            DeleteAccountMessage.OwnerMustTransfer -> {
                snackbarHostState.showSnackbar(deleteAccountOwnerMessage)
                screenModel.clearDeleteAccountMessage()
            }
            DeleteAccountMessage.Error -> {
                snackbarHostState.showSnackbar(deleteAccountErrorMessage)
                screenModel.clearDeleteAccountMessage()
            }
            null -> Unit
        }
    }

    LaunchedEffect(inviteActionMessage) {
        when (val message = inviteActionMessage) {
            InviteActionMessage.AcceptFailed -> {
                snackbarHostState.showSnackbar(inviteErrorMessage)
                screenModel.clearInviteActionMessage()
            }
            InviteActionMessage.EmailMismatch -> {
                snackbarHostState.showSnackbar(emailMismatchMessage)
                screenModel.clearInviteActionMessage()
            }
            is InviteActionMessage.Joined -> Unit
            null -> Unit
        }
    }

    switchHouseholdPrompt?.let { prompt ->
        AlertDialog(
            onDismissRequest = screenModel::dismissSwitchHouseholdPrompt,
            title = { Text(stringResource(Res.string.auth_pending_invites_switch_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.auth_pending_invites_switch_message,
                        prompt.currentHouseholdName,
                        prompt.invitedHouseholdName,
                    ),
                )
            },
            confirmButton = {
                Button(onClick = screenModel::confirmLeaveAndAccept) {
                    Text(stringResource(Res.string.auth_pending_invites_switch_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = screenModel::dismissSwitchHouseholdPrompt) {
                    Text(stringResource(Res.string.auth_pending_invites_switch_cancel))
                }
            },
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissDeleteAccountDialog,
            title = { Text(stringResource(Res.string.home_delete_account_confirm_title)) },
            text = { Text(stringResource(Res.string.home_delete_account_confirm_message)) },
            confirmButton = {
                Button(onClick = screenModel::confirmDeleteAccount) {
                    Text(stringResource(Res.string.home_delete_account_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = screenModel::dismissDeleteAccountDialog) {
                    Text(stringResource(Res.string.auth_pending_invites_switch_cancel))
                }
            },
        )
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
            onAcceptInvite = { inviteId ->
                pendingInvites.find { it.id == inviteId }?.let { invite ->
                    screenModel.onAcceptInviteClicked(invite)
                }
            },
            onDeclineInvite = screenModel::declineInvite,
            onExportPersonalData = screenModel::exportPersonalData,
            onDeleteAccount = screenModel::requestDeleteAccount,
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
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite>,
    onRefreshClick: () -> Unit,
    onOpenNutrition: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
    onSignOut: () -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
    onExportPersonalData: () -> Unit,
    onDeleteAccount: () -> Unit,
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
                        onClick = onExportPersonalData,
                        modifier = Modifier.testTag(HomeTestTags.EXPORT_DATA_BUTTON),
                    ) {
                        Text(
                            stringResource(Res.string.home_export_personal_data),
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.InkMuted,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TextButton(
                        onClick = onDeleteAccount,
                        modifier = Modifier.testTag(HomeTestTags.DELETE_ACCOUNT_BUTTON),
                    ) {
                        Text(
                            stringResource(Res.string.home_delete_account),
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.TerracottaOrange,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
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
