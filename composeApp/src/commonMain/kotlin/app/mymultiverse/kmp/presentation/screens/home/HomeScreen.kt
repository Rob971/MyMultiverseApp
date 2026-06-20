package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import app.mymultiverse.kmp.domain.model.sharing.HouseholdGateError
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.components.HouseholdNameChip
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
    const val ONBOARDING_LOADING = "home_onboarding_loading"
    const val ONBOARDING_ERROR = "home_onboarding_error"
    const val ONBOARDING_RETRY_BUTTON = "home_onboarding_retry"
    const val ONBOARDING_CREATE_NAME_FIELD = "home_onboarding_create_name"
    const val ONBOARDING_CREATE_BUTTON = "home_onboarding_create_button"
    const val ONBOARDING_REFRESH_INVITES = "home_onboarding_refresh_invites"
    const val ONBOARDING_WAIT_FOR_INVITE = "home_onboarding_wait_for_invite"
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
    val homePhase by screenModel.homePhase.collectAsState()
    val greeting by screenModel.greeting.collectAsState()
    val userDisplayName by screenModel.userDisplayName.collectAsState()
    val household by screenModel.household.collectAsState()
    val onboardingUiState by screenModel.onboardingUiState.collectAsState()
    val renameUiState by screenModel.renameUiState.collectAsState()
    val canRenameHousehold by screenModel.canRenameHousehold.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()
    val pendingInvites by screenModel.pendingInvites.collectAsState()
    val inviteActionMessage by screenModel.inviteActionMessage.collectAsState()
    val switchHouseholdPrompt by screenModel.switchHouseholdPrompt.collectAsState()
    val personalDataExportMessage by screenModel.personalDataExportMessage.collectAsState()
    val deleteAccountMessage by screenModel.deleteAccountMessage.collectAsState()
    val showDeleteAccountDialog by screenModel.showDeleteAccountDialog.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sessionEmail = (authState as? AuthState.Authenticated)?.user?.email.orEmpty()

    HomeInviteSnackbarEffects(
        snackbarHostState = snackbarHostState,
        inviteActionMessage = inviteActionMessage,
        inviteAcceptState = inviteAcceptState,
        pendingInvites = pendingInvites,
        sessionEmail = sessionEmail,
        personalDataExportMessage = personalDataExportMessage,
        deleteAccountMessage = deleteAccountMessage,
        onClearInviteActionMessage = screenModel::clearInviteActionMessage,
        onClearInviteFlowSuccess = inviteFlow::clearAcceptSuccess,
        onClearPersonalDataExportMessage = screenModel::clearPersonalDataExportMessage,
        onClearDeleteAccountMessage = screenModel::clearDeleteAccountMessage,
    )

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

    when (val phase = homePhase) {
        HomePhase.Loading -> {
            HomeLoadingContent(
                onSignOut = screenModel::signOut,
                modifier = Modifier.testTag(HomeTestTags.ONBOARDING_LOADING),
            )
        }

        is HomePhase.Error -> {
            HomeErrorContent(
                error = phase.cause,
                onRetry = { screenModel.refreshMembership(forceLoadingState = true) },
                onSignOut = screenModel::signOut,
            )
        }

        HomePhase.Onboarding -> {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    HomeTopBar(onSignOut = screenModel::signOut)
                },
                containerColor = Color.Transparent,
            ) { padding ->
                HomeOnboardingContent(
                    onboardingUiState = onboardingUiState,
                    pendingInvites = pendingInvites,
                    sessionEmail = sessionEmail,
                    onNameChange = screenModel::onHouseholdNameChange,
                    onCreate = screenModel::createHousehold,
                    onAcceptInvite = { inviteId ->
                        pendingInvites.find { it.id == inviteId }?.let { invite ->
                            screenModel.onAcceptInviteClicked(invite)
                        }
                    },
                    onDeclineInvite = screenModel::declineInvite,
                    onRefreshInvites = screenModel::refresh,
                    modifier = Modifier.padding(padding),
                )
            }
        }

        HomePhase.Welcome -> {
            if (renameUiState.isVisible) {
                HomeRenameHouseholdDialog(
                    uiState = renameUiState,
                    onNameChange = screenModel::onRenameHouseholdNameChange,
                    onDismiss = screenModel::dismissRenameHouseholdDialog,
                    onConfirm = screenModel::confirmRenameHousehold,
                )
            }
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    HomeTopBar(onSignOut = screenModel::signOut)
                },
                containerColor = Color.Transparent,
            ) { padding ->
                HomeWelcomeContent(
                    greeting = greeting,
                    userDisplayName = userDisplayName,
                    householdName = household?.name,
                    canRenameHousehold = canRenameHousehold,
                    onRenameHousehold = screenModel::openRenameHouseholdDialog,
                    isRefreshing = isRefreshing,
                    pendingInvites = pendingInvites,
                    onRefreshClick = { screenModel.refresh() },
                    onOpenNutrition = onOpenNutrition,
                    onOpenHouseholdMembers = onOpenHouseholdMembers,
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
    }
}

@Composable
private fun HomeInviteSnackbarEffects(
    snackbarHostState: SnackbarHostState,
    inviteActionMessage: InviteActionMessage?,
    inviteAcceptState: InviteJoinAcceptState,
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite>,
    sessionEmail: String,
    personalDataExportMessage: PersonalDataExportMessage?,
    deleteAccountMessage: DeleteAccountMessage?,
    onClearInviteActionMessage: () -> Unit,
    onClearInviteFlowSuccess: () -> Unit,
    onClearPersonalDataExportMessage: () -> Unit,
    onClearDeleteAccountMessage: () -> Unit,
) {
    val inviteErrorMessage = stringResource(Res.string.auth_pending_invites_error_generic)
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
            onClearInviteActionMessage()
        }
    }

    LaunchedEffect(inviteFlowJoinedMessage) {
        inviteFlowJoinedMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearInviteFlowSuccess()
        }
    }

    LaunchedEffect(personalDataExportMessage) {
        when (personalDataExportMessage) {
            PersonalDataExportMessage.Success -> {
                snackbarHostState.showSnackbar(exportSuccessMessage)
                onClearPersonalDataExportMessage()
            }
            PersonalDataExportMessage.ShareUnavailable -> {
                snackbarHostState.showSnackbar(exportShareUnavailableMessage)
                onClearPersonalDataExportMessage()
            }
            PersonalDataExportMessage.Error -> {
                snackbarHostState.showSnackbar(exportErrorMessage)
                onClearPersonalDataExportMessage()
            }
            null -> Unit
        }
    }

    LaunchedEffect(deleteAccountMessage) {
        when (deleteAccountMessage) {
            DeleteAccountMessage.Success -> {
                snackbarHostState.showSnackbar(deleteAccountSuccessMessage)
                onClearDeleteAccountMessage()
            }
            DeleteAccountMessage.OwnerMustTransfer -> {
                snackbarHostState.showSnackbar(deleteAccountOwnerMessage)
                onClearDeleteAccountMessage()
            }
            DeleteAccountMessage.Error -> {
                snackbarHostState.showSnackbar(deleteAccountErrorMessage)
                onClearDeleteAccountMessage()
            }
            null -> Unit
        }
    }

    LaunchedEffect(inviteActionMessage) {
        when (val message = inviteActionMessage) {
            InviteActionMessage.AcceptFailed -> {
                snackbarHostState.showSnackbar(inviteErrorMessage)
                onClearInviteActionMessage()
            }
            InviteActionMessage.EmailMismatch -> {
                snackbarHostState.showSnackbar(emailMismatchMessage)
                onClearInviteActionMessage()
            }
            is InviteActionMessage.Joined -> Unit
            null -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onSignOut: () -> Unit) {
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
}

@Composable
private fun HomeLoadingContent(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { HomeTopBar(onSignOut = onSignOut) },
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(color = SharedJourneyColors.MediterraneanTeal)
            Text(
                text = stringResource(Res.string.household_gate_loading),
                modifier = Modifier.padding(top = 12.dp),
                color = SharedJourneyColors.InkMuted,
            )
        }
    }
}

@Composable
private fun HomeErrorContent(
    error: HouseholdGateError,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
) {
    val message = when (error) {
        HouseholdGateError.Generic -> stringResource(Res.string.household_gate_error_generic)
        HouseholdGateError.NotConfigured -> stringResource(Res.string.household_gate_error_not_configured)
        HouseholdGateError.AlreadyActive -> stringResource(Res.string.household_gate_error_already_active)
        HouseholdGateError.HouseholdRequired -> stringResource(Res.string.household_gate_error_generic)
    }

    Scaffold(
        topBar = { HomeTopBar(onSignOut = onSignOut) },
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(HomeTestTags.ONBOARDING_ERROR),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = message,
                color = SharedJourneyColors.TerracottaOrange,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .testTag(HomeTestTags.ONBOARDING_RETRY_BUTTON),
            ) {
                Text(stringResource(Res.string.household_gate_retry))
            }
        }
    }
}

@Composable
fun HomeOnboardingContent(
    onboardingUiState: HomeOnboardingUiState,
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite>,
    sessionEmail: String,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
    onRefreshInvites: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasPendingInvites = pendingInvites.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(screenListPadding()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        JourneyBanner(
            headline = stringResource(Res.string.household_gate_title),
            supportingLine = stringResource(Res.string.household_gate_subtitle),
            description = "",
        )

        PendingInvitesCard(
            invites = pendingInvites,
            onAccept = onAcceptInvite,
            onDecline = onDeclineInvite,
        )

        if (!hasPendingInvites) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeTestTags.ONBOARDING_WAIT_FOR_INVITE),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(Res.string.home_onboarding_wait_for_invite_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SharedJourneyColors.InkDeep,
                )
                Text(
                    text = stringResource(Res.string.home_onboarding_wait_for_invite_body, sessionEmail),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.InkMuted,
                )
                TextButton(
                    onClick = onRefreshInvites,
                    modifier = Modifier.testTag(HomeTestTags.ONBOARDING_REFRESH_INVITES),
                ) {
                    Text(stringResource(Res.string.home_onboarding_refresh_invites))
                }
            }
        }

        if (hasPendingInvites) {
            HorizontalDivider(color = SharedJourneyColors.InkMuted.copy(alpha = 0.25f))
            Text(
                text = stringResource(Res.string.household_gate_create_divider),
                style = MaterialTheme.typography.labelLarge,
                color = SharedJourneyColors.InkMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = stringResource(Res.string.household_gate_create_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (hasPendingInvites) FontWeight.SemiBold else FontWeight.Bold,
            color = if (hasPendingInvites) {
                SharedJourneyColors.InkMuted
            } else {
                SharedJourneyColors.InkDeep
            },
        )

        OutlinedTextField(
            value = onboardingUiState.householdNameInput,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomeTestTags.ONBOARDING_CREATE_NAME_FIELD),
            label = { Text(stringResource(Res.string.household_gate_name_label)) },
            singleLine = true,
            enabled = !onboardingUiState.isCreating,
        )

        HouseholdNameAvailabilityLabel(onboardingUiState.nameAvailability)

        val canCreate = onboardingUiState.householdNameInput.isNotBlank() &&
            !onboardingUiState.isCreating &&
            onboardingUiState.nameAvailability == HouseholdNameAvailability.Available

        if (hasPendingInvites) {
            OutlinedButton(
                onClick = onCreate,
                enabled = canCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeTestTags.ONBOARDING_CREATE_BUTTON),
            ) {
                HomeCreateButtonLabel(isCreating = onboardingUiState.isCreating)
            }
        } else {
            Button(
                onClick = onCreate,
                enabled = canCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeTestTags.ONBOARDING_CREATE_BUTTON),
            ) {
                HomeCreateButtonLabel(isCreating = onboardingUiState.isCreating)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HomeCreateButtonLabel(isCreating: Boolean) {
    if (isCreating) {
        CircularProgressIndicator(
            modifier = Modifier.padding(end = 8.dp),
            strokeWidth = 2.dp,
        )
    }
    Text(stringResource(Res.string.household_gate_create_button))
}

@Composable
fun HomeWelcomeContent(
    greeting: Greeting?,
    userDisplayName: String?,
    householdName: String?,
    canRenameHousehold: Boolean,
    onRenameHousehold: () -> Unit,
    isRefreshing: Boolean,
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite>,
    onRefreshClick: () -> Unit,
    onOpenNutrition: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineInvite: (String) -> Unit,
    onExportPersonalData: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val comingSoonLabel = stringResource(Res.string.home_logistics_coming_soon)
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

        if (!householdName.isNullOrBlank()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    HouseholdNameChip(
                        name = householdName,
                        canEdit = canRenameHousehold,
                        onEditClick = onRenameHousehold,
                    )
                }
            }
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
                if (AppBuildInfo.IS_PRERELEASE) {
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

@Composable
private fun HouseholdNameAvailabilityLabel(availability: HouseholdNameAvailability) {
    val message = when (availability) {
        HouseholdNameAvailability.Unknown -> null
        HouseholdNameAvailability.Checking -> stringResource(Res.string.household_name_checking)
        HouseholdNameAvailability.Available -> stringResource(Res.string.household_name_available)
        HouseholdNameAvailability.Taken -> stringResource(Res.string.household_name_taken)
        HouseholdNameAvailability.Invalid -> stringResource(Res.string.household_name_invalid)
    }
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = when (availability) {
                HouseholdNameAvailability.Available -> SharedJourneyColors.MediterraneanTeal
                HouseholdNameAvailability.Checking -> SharedJourneyColors.InkMuted
                else -> SharedJourneyColors.TerracottaOrange
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HomeRenameHouseholdDialog(
    uiState: HomeRenameUiState,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.home_rename_household_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.nameInput,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(Res.string.household_gate_name_label)) },
                    singleLine = true,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                HouseholdNameAvailabilityLabel(uiState.nameAvailability)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !uiState.isSaving &&
                    uiState.nameAvailability == HouseholdNameAvailability.Available,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text(stringResource(Res.string.home_rename_household_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.isSaving) {
                Text(stringResource(Res.string.auth_pending_invites_switch_cancel))
            }
        },
    )
}
