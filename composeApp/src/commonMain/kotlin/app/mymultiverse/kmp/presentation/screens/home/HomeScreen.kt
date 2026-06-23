package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import app.mymultiverse.kmp.presentation.components.JourneyErrorContent
import app.mymultiverse.kmp.presentation.components.JourneyIconButton
import app.mymultiverse.kmp.presentation.components.JourneyLoadingContent
import app.mymultiverse.kmp.presentation.components.JourneyPrimaryButton
import app.mymultiverse.kmp.presentation.components.JourneyTertiaryButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import app.mymultiverse.kmp.presentation.components.JourneyTextField
import app.mymultiverse.kmp.presentation.components.JourneyTextFieldDefaults
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
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.JourneyBanner
import app.mymultiverse.kmp.presentation.components.PendingInvitesCard
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.auth.avatarInitials
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.presentation.components.UserAvatarButton
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import app.mymultiverse.kmp.presentation.invite.InviteJoinAcceptState
import app.mymultiverse.kmp.presentation.invite.InviteJoinFlowCoordinator
import app.mymultiverse.kmp.presentation.screens.household.InviteActionMessage
import org.koin.compose.koinInject

object HomeTestTags {
    const val TONIGHT_DINNER_CARD = "home_tonight_dinner_card"
    const val TONIGHT_DINNER_MEAL = "home_tonight_dinner_meal"
    const val ONBOARDING_QUICK_CREATE = "home_onboarding_quick_create"
    /** @deprecated Today hero actions — use [HomePrimaryActionsTestTags.PLAN] */
    const val NUTRITION_CTA = "home_hero_plan"
    /** @deprecated Today hero actions — use [HomePrimaryActionsTestTags.GROCERY] */
    const val GROCERY_EMPTY_CTA = "home_hero_grocery"
    /** @deprecated Family hub moved to account sheet — use [HomeAccountSheetTestTags.FAMILY_HUB] */
    const val HOUSEHOLD_CARD = "home_account_family_hub"
    const val SIGN_OUT_BUTTON = "home_sign_out_button"
    const val EXPORT_DATA_BUTTON = "home_export_personal_data_button"
    const val DELETE_ACCOUNT_BUTTON = "home_delete_account_button"
    const val APP_VERSION_LABEL = "home_app_version_label"
    const val LOADING_INDICATOR = "home_loading_indicator"
    const val GREETING_LINE = "home_greeting_line"
    const val INSPIRATION_LINE = "home_inspiration_line"
    const val DAILY_MEAL_PLAN_BLOCK = "home_daily_meal_plan_block"
    const val DAILY_TAB_TODAY = "home_daily_tab_today"
    const val DAILY_TAB_THIS_WEEK = "home_daily_tab_this_week"
    const val UPDATE_LIST_ROW = "home_update_list_row"
    const val WEEK_MEAL_PROGRESS_LINE = "home_week_meal_progress_line"
    const val WEEK_GROCERY_PROGRESS_LINE = "home_week_grocery_progress_line"
    const val WEEK_CONTEXT_BANNER = "home_week_context_banner"
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
    onOpenMealPlan: () -> Unit,
    onOpenGrocery: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
    embeddedInMainTabs: Boolean = false,
    modifier: Modifier = Modifier,
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
    val weekPlanNudge by screenModel.weekPlanNudge.collectAsState()
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
    val avatarInitials = (authState as? AuthState.Authenticated)?.user?.avatarInitials() ?: "?"

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
            Scaffold(
                topBar = { HomeTopBar(onSignOut = screenModel::signOut) },
                containerColor = Color.Transparent,
            ) { padding ->
                JourneyLoadingContent(
                    message = stringResource(Res.string.household_gate_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    containerTestTag = HomeTestTags.ONBOARDING_LOADING,
                )
            }
        }

        is HomePhase.Error -> {
            val message = when (phase.cause) {
                HouseholdGateError.Generic -> stringResource(Res.string.household_gate_error_generic)
                HouseholdGateError.NotConfigured -> stringResource(Res.string.household_gate_error_not_configured)
                HouseholdGateError.AlreadyActive -> stringResource(Res.string.household_gate_error_already_active)
                HouseholdGateError.HouseholdRequired -> stringResource(Res.string.household_gate_error_generic)
            }
            Scaffold(
                topBar = { HomeTopBar(onSignOut = screenModel::signOut) },
                containerColor = Color.Transparent,
            ) { padding ->
                JourneyErrorContent(
                    message = message,
                    retryLabel = stringResource(Res.string.household_gate_retry),
                    onRetry = { screenModel.refreshMembership(forceLoadingState = true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    containerTestTag = HomeTestTags.ONBOARDING_ERROR,
                    retryButtonTestTag = HomeTestTags.ONBOARDING_RETRY_BUTTON,
                )
            }
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
                    suggestedQuickCreateName = screenModel.suggestedDefaultHouseholdName(),
                    onNameChange = screenModel::onHouseholdNameChange,
                    onQuickCreate = screenModel::quickCreateHousehold,
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
                householdName = household?.name,
                canRenameHousehold = canRenameHousehold,
                onDismiss = { showAccountSheet = false },
                onOpenHouseholdMembers = onOpenHouseholdMembers,
                onRenameHousehold = screenModel::openRenameHouseholdDialog,
                onSignOut = screenModel::signOut,
                onExportPersonalData = screenModel::exportPersonalData,
                onDeleteAccount = screenModel::requestDeleteAccount,
            )
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    HomeTopBar(
                        onOpenSettings = { showAccountSheet = true },
                        avatarInitials = avatarInitials,
                    )
                },
                // Transparent so NapolitanBackground / MaterialTheme.background shows through on Android.
                containerColor = Color.Transparent,
            ) { padding ->
                HomeWelcomeContent(
                    greeting = greeting,
                    userDisplayName = userDisplayName,
                    nutritionSummary = nutritionSummary,
                    weekPlanNudge = weekPlanNudge,
                    onOpenMealPlan = onOpenMealPlan,
                    onOpenGrocery = onOpenGrocery,
                    onOpenHouseholdMembers = onOpenHouseholdMembers,
                    onDismissWeekPlanNudge = screenModel::dismissWeekPlanNudge,
                    isRefreshing = isRefreshing,
                    onRefresh = screenModel::refresh,
                    embeddedInMainTabs = embeddedInMainTabs,
                    modifier = Modifier.padding(padding).then(modifier),
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
    avatarInitials: String? = null,
) {
    val settingsDescription = stringResource(Res.string.home_settings_button)
    TopAppBar(
        title = { },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            onOpenSettings?.let { openSettings ->
                UserAvatarButton(
                    initials = avatarInitials ?: "?",
                    contentDescription = settingsDescription,
                    onClick = openSettings,
                    modifier = Modifier.testTag(HomeTestTags.SETTINGS_BUTTON),
                    showPersonFallback = avatarInitials == null || avatarInitials == "?",
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeOnboardingContent(
    onboardingUiState: HomeOnboardingUiState,
    pendingInvites: List<app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite>,
    sessionEmail: String,
    suggestedQuickCreateName: String?,
    onNameChange: (String) -> Unit,
    onQuickCreate: () -> Unit,
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

            if (!suggestedQuickCreateName.isNullOrBlank()) {
                JourneyPrimaryButton(
                    onClick = onQuickCreate,
                    enabled = !onboardingUiState.isCreating,
                    isLoading = onboardingUiState.isCreating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(HomeTestTags.ONBOARDING_QUICK_CREATE),
                ) {
                    Text(
                        stringResource(
                            Res.string.home_onboarding_quick_create,
                            suggestedQuickCreateName,
                        ),
                    )
                }
            }

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
                        color = JourneySemanticColors.inkDeep(),
                    )
                    Text(
                        text = stringResource(Res.string.home_onboarding_wait_for_invite_body, sessionEmail),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
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
                HorizontalDivider(color = JourneySemanticColors.inkMuted().copy(alpha = 0.25f))
                Text(
                    text = stringResource(Res.string.household_gate_create_divider),
                    style = MaterialTheme.typography.labelLarge,
                    color = JourneySemanticColors.inkMuted(),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }

            FamilyLogisticsSectionHeader(
                title = stringResource(Res.string.household_gate_create_title),
            )

            JourneyTextField(
                value = onboardingUiState.householdNameInput,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeTestTags.ONBOARDING_CREATE_NAME_FIELD),
                label = { Text(stringResource(Res.string.household_gate_name_label)) },
                enabled = !onboardingUiState.isCreating,
                isError = onboardingUiState.nameAvailability == HouseholdNameAvailability.Taken ||
                    onboardingUiState.nameAvailability == HouseholdNameAvailability.Invalid,
                supportingText = householdNameAvailabilitySupportingText(
                    onboardingUiState.nameAvailability,
                ),
            )

        Text(
            text = stringResource(Res.string.home_onboarding_name_hint),
            style = MaterialTheme.typography.bodySmall,
            color = JourneySemanticColors.inkMuted(),
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
            color = JourneySemanticColors.inkMuted(),
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
    nutritionSummary: HomeNutritionSummary?,
    weekPlanNudge: HomeWeekPlanNudgeUiState = HomeWeekPlanNudgeUiState(),
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenMealPlan: () -> Unit,
    onOpenGrocery: () -> Unit,
    onOpenHouseholdMembers: () -> Unit,
    onDismissWeekPlanNudge: () -> Unit = {},
    embeddedInMainTabs: Boolean = false,
    greetingHour: Int? = null,
    modifier: Modifier = Modifier,
) {
    val greetingSelection = HomeGreetingSelection.select(
        userDisplayName = userDisplayName,
        hour = greetingHour ?: currentLocalHour(),
    )
    val greetingLine = homeGreetingSupportingLine(greetingSelection)
    val showGreetingLoading = greeting == null
    val loadingLine = stringResource(Res.string.home_greeting_loading)

    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullRefreshState,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .then(
                    if (embeddedInMainTabs) {
                        Modifier.imePadding()
                    } else {
                        Modifier.navigationBarsPadding().imePadding()
                    },
                )
                .padding(screenListPadding()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = greetingLine,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = JourneySemanticColors.inkDeep(),
                    modifier = Modifier.testTag(HomeTestTags.GREETING_LINE),
                )
                if (showGreetingLoading) {
                    Text(
                        text = loadingLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
                        modifier = Modifier.testTag(HomeTestTags.LOADING_INDICATOR),
                    )
                }
            }

            HomeDailyHubCircularActions(
                onOpenMealPlan = onOpenMealPlan,
                onOpenGrocery = onOpenGrocery,
            )

            HomeDailyMealPlanBlock(
                nutritionSummary = nutritionSummary,
                onOpenMealPlan = onOpenMealPlan,
                onOpenGrocery = onOpenGrocery,
            )
        }
    }
}

@Composable
private fun householdNameAvailabilitySupportingText(
    availability: HouseholdNameAvailability,
): (@Composable () -> Unit)? {
    val message = when (availability) {
        HouseholdNameAvailability.Checking -> stringResource(Res.string.household_name_checking)
        HouseholdNameAvailability.Available -> stringResource(Res.string.household_name_available)
        HouseholdNameAvailability.Taken -> stringResource(Res.string.household_name_taken)
        HouseholdNameAvailability.Invalid -> stringResource(Res.string.household_name_invalid)
        HouseholdNameAvailability.Unknown -> null
    } ?: return null
    return {
        Text(
            text = message,
            color = when (availability) {
                HouseholdNameAvailability.Available -> SharedJourneyColors.MediterraneanTeal
                HouseholdNameAvailability.Checking -> JourneySemanticColors.inkMuted()
                else -> SharedJourneyColors.TerracottaOrange
            },
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
            Column(verticalArrangement = Arrangement.spacedBy(JourneyTextFieldDefaults.fieldSpacing)) {
                JourneyTextField(
                    value = uiState.nameInput,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(Res.string.household_gate_name_label)) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameAvailability == HouseholdNameAvailability.Taken ||
                        uiState.nameAvailability == HouseholdNameAvailability.Invalid,
                    supportingText = householdNameAvailabilitySupportingText(uiState.nameAvailability),
                )
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
