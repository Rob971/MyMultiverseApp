package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import app.mymultiverse.kmp.presentation.components.JourneyPrimaryButton
import app.mymultiverse.kmp.presentation.components.JourneyTertiaryButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.model.sharing.HouseholdGateError
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsCardSurface
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.HomeFirstWinChecklistCard
import app.mymultiverse.kmp.presentation.components.HomeHouseholdButton
import app.mymultiverse.kmp.presentation.components.JourneyBanner
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
    const val NUTRITION_CTA = "home_nutrition_cta"
    const val THIS_WEEK_SECTION = "home_this_week_section"
    const val HOUSEHOLD_CARD = "home_household_card"
    const val SIGN_OUT_BUTTON = "home_sign_out_button"
    const val EXPORT_DATA_BUTTON = "home_export_personal_data_button"
    const val DELETE_ACCOUNT_BUTTON = "home_delete_account_button"
    const val APP_VERSION_LABEL = "home_app_version_label"
    const val LOADING_INDICATOR = "home_loading_indicator"
    const val GREETING_LINE = "home_greeting_line"
    const val INSPIRATION_LINE = "home_inspiration_line"
    const val SETTINGS_BUTTON = "home_settings_button"
    const val ONBOARDING_LOADING = "home_onboarding_loading"
    const val ONBOARDING_ERROR = "home_onboarding_error"
    const val ONBOARDING_RETRY_BUTTON = "home_onboarding_retry"
    const val ONBOARDING_CREATE_NAME_FIELD = "home_onboarding_create_name"
    const val ONBOARDING_CREATE_BUTTON = "home_onboarding_create_button"
    const val ONBOARDING_REFRESH_INVITES = "home_onboarding_refresh_invites"
    const val ONBOARDING_WAIT_FOR_INVITE = "home_onboarding_wait_for_invite"
    const val ONBOARDING_CREATE_INVITE_HINT = "home_onboarding_create_invite_hint"
    const val ONBOARDING_NAME_HINT = "home_onboarding_name_hint"
    const val POST_CREATE_INVITE_SNACKBAR = "home_post_create_invite_snackbar"
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
    val nutritionSummary by screenModel.nutritionSummary.collectAsState()
    val firstWinChecklist by screenModel.firstWinChecklist.collectAsState()
    val pendingInvites by screenModel.pendingInvites.collectAsState()
    val inviteActionMessage by screenModel.inviteActionMessage.collectAsState()
    val postCreateInvitePrompt by screenModel.postCreateInvitePrompt.collectAsState()
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
        postCreateInvitePrompt = postCreateInvitePrompt,
        inviteAcceptState = inviteAcceptState,
        pendingInvites = pendingInvites,
        sessionEmail = sessionEmail,
        personalDataExportMessage = personalDataExportMessage,
        deleteAccountMessage = deleteAccountMessage,
        onClearInviteActionMessage = screenModel::clearInviteActionMessage,
        onClearPostCreateInvitePrompt = screenModel::clearPostCreateInvitePrompt,
        onOpenHouseholdMembers = onOpenHouseholdMembers,
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
                JourneyPrimaryButton(onClick = screenModel::confirmLeaveAndAccept) {
                    Text(stringResource(Res.string.auth_pending_invites_switch_confirm))
                }
            },
            dismissButton = {
                JourneyTertiaryButton(
                    onClick = screenModel::dismissSwitchHouseholdPrompt,
                    label = stringResource(Res.string.auth_pending_invites_switch_cancel),
                )
            },
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissDeleteAccountDialog,
            title = { Text(stringResource(Res.string.home_delete_account_confirm_title)) },
            text = { Text(stringResource(Res.string.home_delete_account_confirm_message)) },
            confirmButton = {
                JourneyPrimaryButton(onClick = screenModel::confirmDeleteAccount) {
                    Text(stringResource(Res.string.home_delete_account_confirm_action))
                }
            },
            dismissButton = {
                JourneyTertiaryButton(
                    onClick = screenModel::dismissDeleteAccountDialog,
                    label = stringResource(Res.string.auth_pending_invites_switch_cancel),
                )
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
                    isRefreshing = isRefreshing,
                    modifier = Modifier.padding(padding),
                )
            }
        }

        HomePhase.Welcome -> {
            var showAccountSheet by remember { mutableStateOf(false) }
            if (renameUiState.isVisible) {
                HomeRenameHouseholdDialog(
                    uiState = renameUiState,
                    onNameChange = screenModel::onRenameHouseholdNameChange,
                    onDismiss = screenModel::dismissRenameHouseholdDialog,
                    onConfirm = screenModel::confirmRenameHousehold,
                )
            }
            HomeAccountSheet(
                visible = showAccountSheet,
                onDismiss = { showAccountSheet = false },
                onSignOut = screenModel::signOut,
                onExportPersonalData = screenModel::exportPersonalData,
                onDeleteAccount = screenModel::requestDeleteAccount,
            )
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    HomeTopBar(onOpenSettings = { showAccountSheet = true })
                },
                containerColor = Color.Transparent,
            ) { padding ->
                HomeWelcomeContent(
                    greeting = greeting,
                    userDisplayName = userDisplayName,
                    householdName = household?.name,
                    canRenameHousehold = canRenameHousehold,
                    onRenameHousehold = screenModel::openRenameHouseholdDialog,
                    nutritionSummary = nutritionSummary,
                    firstWinChecklist = firstWinChecklist,
                    onOpenNutrition = onOpenNutrition,
                    onOpenHouseholdMembers = onOpenHouseholdMembers,
                    onDismissFirstWinChecklist = screenModel::dismissFirstWinChecklist,
                    isRefreshing = isRefreshing,
                    onRefresh = screenModel::refresh,
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
    postCreateInvitePrompt: PostCreateInvitePrompt?,
    inviteAcceptState: InviteJoinAcceptState,
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite>,
    sessionEmail: String,
    personalDataExportMessage: PersonalDataExportMessage?,
    deleteAccountMessage: DeleteAccountMessage?,
    onClearInviteActionMessage: () -> Unit,
    onClearPostCreateInvitePrompt: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
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
    val postCreateInviteMessage = postCreateInvitePrompt?.householdName
        ?.let { name -> stringResource(Res.string.home_household_created_snackbar, name) }
    val postCreateInviteAction = stringResource(Res.string.home_household_created_invite_action)

    LaunchedEffect(postCreateInviteMessage) {
        postCreateInviteMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = postCreateInviteAction,
            )
            onClearPostCreateInvitePrompt()
            if (result == SnackbarResult.ActionPerformed) {
                onOpenHouseholdMembers()
            }
        }
    }

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
private fun HomeTopBar(
    onSignOut: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
) {
    val settingsDescription = stringResource(Res.string.home_settings_button)
    TopAppBar(
        title = { },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            onOpenSettings?.let { openSettings ->
                IconButton(
                    onClick = openSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(HomeTestTags.SETTINGS_BUTTON)
                        .semantics { contentDescription = settingsDescription },
                ) {
                    Icon(
                        imageVector = AppIcons.MoreVert,
                        contentDescription = null,
                        tint = SharedJourneyColors.InkDeep,
                    )
                }
            }
            onSignOut?.let { signOut ->
                TextButton(
                    onClick = signOut,
                    modifier = Modifier.testTag(HomeTestTags.SIGN_OUT_BUTTON),
                ) {
                    Text(stringResource(Res.string.auth_sign_out))
                }
            }
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
            JourneyPrimaryButton(
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

@OptIn(ExperimentalMaterial3Api::class)
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
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val hasPendingInvites = pendingInvites.isNotEmpty()
    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefreshInvites,
        state = pullRefreshState,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
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

            FamilyLogisticsSectionHeader(
                title = stringResource(Res.string.home_onboarding_join_title),
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

            FamilyLogisticsSectionHeader(
                title = stringResource(Res.string.household_gate_create_title),
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

        Text(
            text = stringResource(Res.string.home_onboarding_name_hint),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
            modifier = Modifier.fillMaxWidth(),
        )

        val canCreate = onboardingUiState.householdNameInput.isNotBlank() &&
            !onboardingUiState.isCreating &&
            onboardingUiState.nameAvailability == HouseholdNameAvailability.Available

        JourneyPrimaryButton(
            onClick = onCreate,
            enabled = canCreate,
            isLoading = onboardingUiState.isCreating,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomeTestTags.ONBOARDING_CREATE_BUTTON),
        ) {
            Text(stringResource(Res.string.household_gate_create_button))
        }

        Text(
            text = stringResource(Res.string.home_onboarding_create_invite_hint),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomeTestTags.ONBOARDING_CREATE_INVITE_HINT),
        )

        Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeWelcomeContent(
    greeting: Greeting?,
    userDisplayName: String?,
    householdName: String?,
    canRenameHousehold: Boolean,
    onRenameHousehold: () -> Unit,
    nutritionSummary: HomeNutritionSummary?,
    firstWinChecklist: HomeFirstWinChecklistUiState = HomeFirstWinChecklistUiState(),
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenNutrition: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
    onDismissFirstWinChecklist: () -> Unit = {},
    greetingHour: Int? = null,
    modifier: Modifier = Modifier,
) {
    val greetingSelection = HomeGreetingSelection.select(
        userDisplayName = userDisplayName,
        hour = greetingHour ?: currentLocalHour(),
    )
    val greetingLine = homeGreetingSupportingLine(greetingSelection)
    val inspirationLine = when (val line = HomeInspirationLine.select(greeting)) {
        HomeInspirationLine.Loading -> stringResource(Res.string.home_greeting_loading)
        is HomeInspirationLine.Ready -> line.text
    }
    val showInspirationLoading = greeting == null
    val thisWeekStatusLine = homeThisWeekStatusLine(nutritionSummary)
    val hasNutritionProgress = nutritionSummary?.let { summary ->
        summary.plannedMealSlots > 0 || summary.groceryProgress?.total?.let { it > 0 } == true
    } == true
    val nutritionCtaLabel = if (hasNutritionProgress) {
        stringResource(Res.string.home_nutrition_continue_planning)
    } else {
        stringResource(Res.string.home_nutrition_get_started)
    }

    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullRefreshState,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding(),
            contentPadding = screenListPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                JourneyBanner(
                    headline = greetingLine,
                    supportingLine = null,
                    description = inspirationLine,
                    headlineTestTag = HomeTestTags.GREETING_LINE,
                    descriptionTestTag = if (showInspirationLoading) {
                        HomeTestTags.LOADING_INDICATOR
                    } else {
                        HomeTestTags.INSPIRATION_LINE
                    },
                )
            }

            if (firstWinChecklist.visible) {
                item {
                    HomeFirstWinChecklistCard(
                        title = stringResource(Res.string.home_first_win_title),
                        inviteLabel = stringResource(Res.string.home_first_win_invite),
                        nutritionLabel = stringResource(Res.string.home_first_win_nutrition),
                        dismissLabel = stringResource(Res.string.home_first_win_dismiss),
                        inviteComplete = firstWinChecklist.inviteComplete,
                        nutritionComplete = firstWinChecklist.nutritionComplete,
                        onInviteClick = onOpenHouseholdMembers,
                        onNutritionClick = onOpenNutrition,
                        onDismiss = onDismissFirstWinChecklist,
                    )
                }
            }

            item {
                FamilyLogisticsSectionHeader(
                    title = stringResource(Res.string.home_section_this_week),
                    titleModifier = Modifier.testTag(HomeTestTags.THIS_WEEK_SECTION),
                )
            }

            item {
                FamilyLogisticsCardSurface(
                    accentColor = SharedJourneyColors.SageSoft,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.home_logistics_nutrition_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SharedJourneyColors.InkDeep,
                        )
                        Text(
                            text = thisWeekStatusLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SharedJourneyColors.InkMuted,
                        )
                        JourneyPrimaryButton(
                            onClick = onOpenNutrition,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(HomeTestTags.NUTRITION_CTA),
                        ) {
                            Text(nutritionCtaLabel)
                        }
                    }
                }
            }

            if (!householdName.isNullOrBlank()) {
                item {
                    FamilyLogisticsSectionHeader(
                        title = stringResource(Res.string.home_section_household),
                    )
                }
                item {
                    HomeHouseholdButton(
                        householdName = householdName,
                        canManage = canRenameHousehold,
                        onOpenHousehold = onOpenHouseholdMembers,
                        onRenameHousehold = onRenameHousehold,
                        modifier = Modifier.testTag(HomeTestTags.HOUSEHOLD_CARD),
                    )
                }
            }
        }
    }
}

@Composable
private fun homeThisWeekStatusLine(nutritionSummary: HomeNutritionSummary?): String {
    val emptyLabel = stringResource(Res.string.home_nutrition_get_started)
    if (nutritionSummary == null) {
        return emptyLabel
    }
    val groceryProgress = nutritionSummary.groceryProgress
    if (groceryProgress == null && nutritionSummary.plannedMealSlots == 0) {
        return emptyLabel
    }
    return buildList {
        groceryProgress?.let { progress ->
            add(
                stringResource(
                    Res.string.nutrition_grocery_progress,
                    progress.checked,
                    progress.total,
                ),
            )
        }
        add(
            stringResource(
                Res.string.nutrition_meal_plan_progress,
                nutritionSummary.plannedMealSlots,
                NutritionHubSummary.MEAL_SLOTS_PER_WEEK,
            ),
        )
    }.joinToString(" · ")
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
            JourneyPrimaryButton(
                onClick = onConfirm,
                enabled = !uiState.isSaving &&
                    uiState.nameAvailability == HouseholdNameAvailability.Available,
                isLoading = uiState.isSaving,
            ) {
                Text(stringResource(Res.string.home_rename_household_save))
            }
        },
        dismissButton = {
            JourneyTertiaryButton(
                onClick = onDismiss,
                enabled = !uiState.isSaving,
                label = stringResource(Res.string.auth_pending_invites_switch_cancel),
            )
        },
    )
}
