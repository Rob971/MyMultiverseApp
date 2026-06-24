package app.mymultiverse.kmp.presentation.screens.household

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import app.mymultiverse.kmp.presentation.components.JourneySecondaryButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvite
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMember
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.sharing.canAssignAdminRole
import app.mymultiverse.kmp.domain.sharing.canChangeRoleOf
import app.mymultiverse.kmp.domain.sharing.canRemoveMember
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.data.invite.InviteRedirectUrls
import app.mymultiverse.kmp.domain.platform.PersonalDataExporter
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsCardSurface
import app.mymultiverse.kmp.presentation.components.HouseholdRoleBadge
import app.mymultiverse.kmp.presentation.components.HouseholdRoleSelector
import app.mymultiverse.kmp.presentation.components.householdRoleLabel
import app.mymultiverse.kmp.presentation.components.JourneyEmptyState
import app.mymultiverse.kmp.presentation.components.JourneyIcon
import app.mymultiverse.kmp.presentation.components.JourneyIconButton
import app.mymultiverse.kmp.presentation.components.JourneyPrimaryButton
import app.mymultiverse.kmp.presentation.components.JourneyTextField
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
import app.mymultiverse.kmp.presentation.theme.AppIconRole
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.content_more_options
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_add_dependent
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_by_email
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_by_email_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_chooser_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_add_dependent
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_add_person
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_cancel
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_confirm_add
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_dependent_added
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_dependent_confirm
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_dependent_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_dependent_name_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_email_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_email_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_solo_body
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_solo_cta
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_solo_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_cannot_add_self
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_email_required
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_generic
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_invitee_household_already_active
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_insufficient_role
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_member_already_exists
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_not_configured
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_dissolve
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_dissolve_confirm_message
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_dissolve_confirm_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_member_limit
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_owner_transfer_required
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_leave
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_leave_confirm_message
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_leave_confirm_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_transfer_confirm
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_transfer_confirm_message
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_transfer_ownership
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_transfer_pick_member
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_transfer_success
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_transfer_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_transfer_failed
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_transfer_target
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_leave_gdpr_note
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_owner_transfer_required
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_share_action
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_share_message
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_share_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_sent
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_kind_dependent
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_member_added
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_more_actions
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_owner_fallback
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_pending_invite_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_pending_invites_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_read_only_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_remove
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_change_role
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_promote_admin_confirm_message
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_promote_admin_confirm_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_admin
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_editor
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_updated
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_owner
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_viewer
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_select_role
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object HouseholdMembersTestTags {
    const val INVITE_BUTTON = "household_members_invite"
    /** @deprecated Use [INVITE_BUTTON] */
    const val ADD_PERSON_BUTTON = INVITE_BUTTON
    const val INVITE_CHOOSER = "household_members_invite_chooser"
    const val INVITE_BY_EMAIL_OPTION = "household_members_invite_by_email"
    const val INVITE_ADD_DEPENDENT_OPTION = "household_members_invite_add_dependent"
    const val ADD_PERSON_CONFIRM_BUTTON = "household_members_add_person_confirm"
    const val ADD_PERSON_DIALOG_ERROR = "household_members_add_person_error"
    const val MEMBER_ROW = "household_members_row"
    const val PENDING_INVITE_ROW = "household_members_pending_invite"
    const val TRANSFER_OWNERSHIP_BUTTON = "household_members_transfer_ownership"
    const val TRANSFER_DIALOG = "household_members_transfer_dialog"
    const val ADD_DEPENDANT_BUTTON = "household_members_add_dependant"
    const val SOLO_EMPTY_STATE = "household_members_solo_empty_state"
    const val ROLE_CHANGE_CONFIRM_BUTTON = "household_members_role_change_confirm"
    const val PROMOTE_ADMIN_CONFIRM_BUTTON = "household_members_promote_admin_confirm"
    const val PENDING_INVITE_SHARE = "household_members_pending_invite_share"
    const val MEMBER_ROW_OVERFLOW = "household_members_row_overflow"
    const val MEMBER_CHANGE_ROLE_MENU = "household_members_change_role_menu"
    const val MEMBER_REMOVE_MENU = "household_members_remove_menu"
    const val HOUSEHOLD_ACTIONS_OVERFLOW = "household_members_household_actions_overflow"
    const val HOUSEHOLD_TRANSFER_MENU = "household_members_transfer_menu"
    const val HOUSEHOLD_LEAVE_MENU = "household_members_leave_menu"
    const val HOUSEHOLD_DISSOLVE_MENU = "household_members_dissolve_menu"
    const val PENDING_INVITE_OVERFLOW = "household_members_pending_invite_overflow"
}

@Composable
fun HouseholdMembersScreen(
    household: HouseholdContext,
    onBack: () -> Unit,
    screenModel: HouseholdMembersScreenModel = koinInject(),
    authRepository: AuthRepository = koinInject(),
    personalDataExporter: PersonalDataExporter = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val ownerFallback = stringResource(Res.string.sharing_members_owner_fallback)
    val personMemberCount = uiState.members.count { it.kind == HouseholdMemberKind.Person }
    val showSoloInviteEmpty = !uiState.isLoading &&
        personMemberCount <= 1 &&
        uiState.outboundInvites.isEmpty()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingPromoteAdminConfirm by remember { mutableStateOf(false) }
    val errorMessage = uiState.error?.let { error -> mapErrorMessage(error) }
    val dialogErrorMessage = uiState.dialogError?.let { error -> mapErrorMessage(error) }
    val successMessage = uiState.successMessageKey?.let { success ->
        when (success) {
            HouseholdMembersSuccess.InviteSent -> stringResource(
                Res.string.sharing_members_invite_sent,
                uiState.invitedEmailForSuccess.orEmpty(),
            )
            HouseholdMembersSuccess.MemberAdded -> stringResource(Res.string.sharing_members_member_added)
            HouseholdMembersSuccess.DependantAdded -> stringResource(Res.string.sharing_members_dependent_added)
            HouseholdMembersSuccess.OwnershipTransferred ->
                stringResource(
                    Res.string.sharing_members_transfer_success,
                    uiState.transferredToDisplayName.orEmpty(),
                )
            HouseholdMembersSuccess.RoleUpdated ->
                stringResource(Res.string.sharing_members_role_updated)
        }
    }
    val currentUserId = (authState as? AuthState.Authenticated)?.user?.id

    val shareTitle = stringResource(Res.string.sharing_members_invite_share_title)
    val pendingSharePayload = uiState.pendingInviteShare
    val pendingShareMessage = pendingSharePayload?.let { payload ->
        stringResource(
            Res.string.sharing_members_invite_share_message,
            payload.householdName,
            InviteRedirectUrls.buildHttps(payload.inviteToken),
        )
    }

    LaunchedEffect(household.id, household.ownerId, household.ownerDisplayName, currentUserId) {
        screenModel.bindHousehold(
            householdId = household.id,
            householdName = household.name,
            ownerId = household.ownerId,
            ownerDisplayName = household.ownerDisplayName ?: ownerFallback,
            currentUserId = currentUserId,
        )
    }

    LaunchedEffect(pendingShareMessage, shareTitle) {
        val message = pendingShareMessage ?: return@LaunchedEffect
        personalDataExporter.shareText(shareTitle, message)
        screenModel.consumePendingInviteShare()
    }

    LaunchedEffect(successMessage) {
        val message = successMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.clearSuccessMessage()
    }

    NutritionScaffold(
        title = stringResource(Res.string.sharing_members_title),
        subtitle = household.name,
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading && uiState.members.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().screenContentArea(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = JourneySemanticColors.brandTeal())
                Text(
                    text = stringResource(Res.string.sharing_members_loading),
                    modifier = Modifier.padding(top = 12.dp),
                    color = JourneySemanticColors.inkMuted(),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().screenContentArea(padding),
                contentPadding = screenListPadding(),
                verticalArrangement = Arrangement.spacedBy(ScreenLayout.sectionSpacing),
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.sharing_members_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkSecondary(),
                    )
                }
                if (!uiState.canManageMembers) {
                    item {
                        Text(
                            text = stringResource(Res.string.sharing_members_read_only_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = JourneySemanticColors.inkMuted(),
                        )
                    }
                }
                if (uiState.canManageMembers) {
                    item {
                        JourneyPrimaryButton(
                            onClick = screenModel::openInviteChooser,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(HouseholdMembersTestTags.INVITE_BUTTON),
                        ) {
                            Text(stringResource(Res.string.sharing_members_invite))
                        }
                    }
                }
                if (uiState.outboundInvites.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.sharing_members_pending_invites_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = JourneySemanticColors.inkDeep(),
                        )
                    }
                    items(uiState.outboundInvites, key = { it.id }) { invite ->
                        val shareMessage = invite.inviteToken?.let { token ->
                            stringResource(
                                Res.string.sharing_members_invite_share_message,
                                household.name,
                                InviteRedirectUrls.buildHttps(token),
                            )
                        }
                        PendingInviteRow(
                            invite = invite,
                            shareActionLabel = stringResource(Res.string.sharing_members_invite_share_action),
                            onShare = shareMessage?.let { message ->
                                { personalDataExporter.shareText(shareTitle, message) }
                            },
                        )
                    }
                }
                if (showSoloInviteEmpty && uiState.canManageMembers) {
                    item {
                        JourneyEmptyState(
                            title = stringResource(Res.string.sharing_members_solo_title),
                            body = stringResource(Res.string.sharing_members_solo_body),
                            icon = AppIcons.Person,
                            primaryActionLabel = stringResource(Res.string.sharing_members_invite),
                            onPrimaryAction = screenModel::openInviteChooser,
                            testTag = HouseholdMembersTestTags.SOLO_EMPTY_STATE,
                        )
                    }
                }
                if (uiState.members.isNotEmpty()) {
                    items(uiState.members, key = { it.id }) { member ->
                        MemberRow(
                            member = member,
                            actorRole = uiState.currentUserRole,
                            canManage = uiState.canManageMembers,
                            onRemove = { screenModel.removeMember(member, household.id) },
                            onChangeRole = { screenModel.openRoleChangeDialog(member) },
                        )
                    }
                }
                if (uiState.showOwnerTransferHint) {
                    item {
                        Text(
                            text = stringResource(Res.string.sharing_members_owner_transfer_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = JourneySemanticColors.inkMuted(),
                        )
                    }
                }
                if (uiState.canTransferOwnership || uiState.canLeave || uiState.canDissolve) {
                    item {
                        var householdMenuExpanded by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            JourneyIconButton(
                                onClick = { householdMenuExpanded = true },
                                modifier = Modifier.testTag(HouseholdMembersTestTags.HOUSEHOLD_ACTIONS_OVERFLOW),
                            ) {
                                JourneyIcon(
                                    role = AppIconRole.ChromeOverflow,
                                    contentDescription = stringResource(Res.string.sharing_members_more_actions),
                                    tint = JourneySemanticColors.inkSecondary(),
                                )
                            }
                            DropdownMenu(
                                expanded = householdMenuExpanded,
                                onDismissRequest = { householdMenuExpanded = false },
                            ) {
                                if (uiState.canTransferOwnership) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(stringResource(Res.string.sharing_members_transfer_ownership))
                                        },
                                        onClick = {
                                            householdMenuExpanded = false
                                            screenModel.openTransferDialog()
                                        },
                                        enabled = !uiState.isTransferring,
                                        modifier = Modifier.testTag(HouseholdMembersTestTags.HOUSEHOLD_TRANSFER_MENU),
                                    )
                                }
                                if (uiState.canLeave) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.sharing_members_leave)) },
                                        onClick = {
                                            householdMenuExpanded = false
                                            screenModel.requestLeave()
                                        },
                                        enabled = !uiState.isLeaving,
                                        modifier = Modifier.testTag(HouseholdMembersTestTags.HOUSEHOLD_LEAVE_MENU),
                                    )
                                }
                                if (uiState.canDissolve) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.sharing_members_dissolve)) },
                                        onClick = {
                                            householdMenuExpanded = false
                                            screenModel.requestDissolve()
                                        },
                                        enabled = !uiState.isLeaving,
                                        modifier = Modifier.testTag(HouseholdMembersTestTags.HOUSEHOLD_DISSOLVE_MENU),
                                    )
                                }
                            }
                        }
                    }
                }
                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage,
                            color = SharedJourneyColors.TerracottaOrange,
                        )
                    }
                }
            }
        }
    }

    if (uiState.showInviteChooserDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissInviteChooser,
            modifier = Modifier.testTag(HouseholdMembersTestTags.INVITE_CHOOSER),
            title = { Text(stringResource(Res.string.sharing_members_invite_chooser_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    JourneyPrimaryButton(
                        onClick = screenModel::openAddPersonDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(HouseholdMembersTestTags.INVITE_BY_EMAIL_OPTION),
                    ) {
                        Text(stringResource(Res.string.sharing_members_invite_by_email))
                    }
                    Text(
                        text = stringResource(Res.string.sharing_members_invite_by_email_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = JourneySemanticColors.inkMuted(),
                    )
                    JourneySecondaryButton(
                        onClick = screenModel::openAddDependantDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(HouseholdMembersTestTags.INVITE_ADD_DEPENDENT_OPTION),
                    ) {
                        Text(stringResource(Res.string.sharing_members_invite_add_dependent))
                    }
                    Text(
                        text = stringResource(Res.string.sharing_members_dependent_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = JourneySemanticColors.inkMuted(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = screenModel::dismissInviteChooser) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    if (uiState.showAddPersonDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissDialogs,
            title = { Text(stringResource(Res.string.sharing_members_add_person)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    JourneyTextField(
                        value = uiState.emailInput,
                        onValueChange = screenModel::onEmailChange,
                        label = { Text(stringResource(Res.string.sharing_members_email_label)) },
                        placeholder = { Text(stringResource(Res.string.sharing_members_email_hint)) },
                        enabled = !uiState.isSaving,
                        isError = dialogErrorMessage != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (dialogErrorMessage != null) {
                        Text(
                            text = dialogErrorMessage,
                            color = SharedJourneyColors.TerracottaOrange,
                            modifier = Modifier.testTag(HouseholdMembersTestTags.ADD_PERSON_DIALOG_ERROR),
                        )
                    }
                    Text(stringResource(Res.string.sharing_members_select_role))
                    HouseholdRoleSelector(
                        selectedRole = uiState.selectedRole,
                        onRoleSelected = screenModel::onRoleChange,
                        showAdminOption = uiState.currentUserRole?.canAssignAdminRole() == true,
                    )
                }
            },
            confirmButton = {
                JourneyPrimaryButton(
                    onClick = { screenModel.submitAddPerson(household.id) },
                    enabled = !uiState.isSaving,
                    isLoading = uiState.isSaving,
                    modifier = Modifier.testTag(HouseholdMembersTestTags.ADD_PERSON_CONFIRM_BUTTON),
                ) {
                    Text(stringResource(Res.string.sharing_members_confirm_add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = screenModel::dismissDialogs,
                    enabled = !uiState.isSaving,
                ) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    uiState.pendingLeaveAction?.let { action ->
        val title = when (action) {
            HouseholdMembersLeaveAction.Leave ->
                stringResource(Res.string.sharing_members_leave_confirm_title)
            HouseholdMembersLeaveAction.Dissolve ->
                stringResource(Res.string.sharing_members_dissolve_confirm_title)
        }
        val message = when (action) {
            HouseholdMembersLeaveAction.Leave ->
                stringResource(Res.string.sharing_members_leave_confirm_message, household.name)
            HouseholdMembersLeaveAction.Dissolve ->
                stringResource(Res.string.sharing_members_dissolve_confirm_message, household.name)
        }
        val confirmLabel = when (action) {
            HouseholdMembersLeaveAction.Leave -> stringResource(Res.string.sharing_members_leave)
            HouseholdMembersLeaveAction.Dissolve -> stringResource(Res.string.sharing_members_dissolve)
        }
        AlertDialog(
            onDismissRequest = screenModel::dismissLeaveDissolve,
            title = { Text(title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(message)
                    if (action == HouseholdMembersLeaveAction.Leave) {
                        Text(stringResource(Res.string.sharing_members_leave_gdpr_note))
                    }
                }
            },
            confirmButton = {
                JourneyPrimaryButton(
                    onClick = screenModel::confirmLeaveOrDissolve,
                    enabled = !uiState.isLeaving,
                    isLoading = uiState.isLeaving,
                ) {
                    Text(confirmLabel)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = screenModel::dismissLeaveDissolve,
                    enabled = !uiState.isLeaving,
                ) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    if (uiState.showAddDependantDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissAddDependantDialog,
            title = { Text(stringResource(Res.string.sharing_members_add_dependent)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    JourneyTextField(
                        value = uiState.dependantNameInput,
                        onValueChange = screenModel::onDependantNameChange,
                        label = { Text(stringResource(Res.string.sharing_members_dependent_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (dialogErrorMessage != null) {
                        Text(
                            text = dialogErrorMessage,
                            color = SharedJourneyColors.TerracottaOrange,
                        )
                    }
                }
            },
            confirmButton = {
                JourneyPrimaryButton(
                    onClick = { screenModel.submitAddDependant(household.id) },
                    enabled = !uiState.isSaving,
                    isLoading = uiState.isSaving,
                ) {
                    Text(stringResource(Res.string.sharing_members_dependent_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = screenModel::dismissAddDependantDialog) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    if (uiState.showRoleChangeDialog) {
        val target = uiState.roleChangeTarget
        AlertDialog(
            onDismissRequest = screenModel::dismissRoleChangeDialog,
            title = { Text(stringResource(Res.string.sharing_members_change_role)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (target != null) {
                        Text(
                            text = target.displayName,
                            color = JourneySemanticColors.inkDeep(),
                        )
                    }
                    Text(stringResource(Res.string.sharing_members_select_role))
                    HouseholdRoleSelector(
                        selectedRole = uiState.selectedMemberRole,
                        onRoleSelected = screenModel::onMemberRoleChange,
                        showAdminOption = uiState.currentUserRole?.canAssignAdminRole() == true,
                    )
                    if (dialogErrorMessage != null) {
                        Text(
                            text = dialogErrorMessage,
                            color = SharedJourneyColors.TerracottaOrange,
                        )
                    }
                }
            },
            confirmButton = {
                JourneyPrimaryButton(
                    onClick = {
                        val needsPromoteConfirm = uiState.selectedMemberRole == HouseholdMemberRole.Admin &&
                            target?.role != HouseholdMemberRole.Admin
                        if (needsPromoteConfirm) {
                            pendingPromoteAdminConfirm = true
                        } else {
                            screenModel.confirmRoleChange(household.id)
                        }
                    },
                    enabled = !uiState.isUpdatingRole && target != null,
                    isLoading = uiState.isUpdatingRole,
                    modifier = Modifier.testTag(HouseholdMembersTestTags.ROLE_CHANGE_CONFIRM_BUTTON),
                ) {
                    Text(stringResource(Res.string.sharing_members_confirm_add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = screenModel::dismissRoleChangeDialog,
                    enabled = !uiState.isUpdatingRole,
                ) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    if (pendingPromoteAdminConfirm && uiState.roleChangeTarget != null) {
        val targetName = uiState.roleChangeTarget!!.displayName
        AlertDialog(
            onDismissRequest = { pendingPromoteAdminConfirm = false },
            title = { Text(stringResource(Res.string.sharing_members_promote_admin_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.sharing_members_promote_admin_confirm_message,
                        targetName,
                    ),
                )
            },
            confirmButton = {
                JourneyPrimaryButton(
                    onClick = {
                        pendingPromoteAdminConfirm = false
                        screenModel.confirmRoleChange(household.id)
                    },
                    enabled = !uiState.isUpdatingRole,
                    isLoading = uiState.isUpdatingRole,
                    modifier = Modifier.testTag(HouseholdMembersTestTags.PROMOTE_ADMIN_CONFIRM_BUTTON),
                ) {
                    Text(stringResource(Res.string.sharing_members_confirm_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPromoteAdminConfirm = false }) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    if (uiState.showTransferDialog) {
        val selectedId = uiState.selectedTransferMemberId
        val selectedMember = uiState.transferCandidates.find { it.referenceId == selectedId }
        AlertDialog(
            onDismissRequest = screenModel::dismissTransferDialog,
            modifier = Modifier.testTag(HouseholdMembersTestTags.TRANSFER_DIALOG),
            title = { Text(stringResource(Res.string.sharing_members_transfer_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.sharing_members_transfer_pick_member))
                    uiState.transferCandidates.forEach { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = member.referenceId == selectedId,
                                onClick = { screenModel.selectTransferMember(member.referenceId) },
                            )
                            Text(
                                text = member.displayName,
                                modifier = Modifier.padding(start = 8.dp),
                                color = JourneySemanticColors.inkDeep(),
                            )
                        }
                    }
                    if (selectedMember != null) {
                        Text(
                            stringResource(
                                Res.string.sharing_members_transfer_confirm_message,
                                selectedMember.displayName,
                            ),
                        )
                    }
                }
            },
            confirmButton = {
                JourneyPrimaryButton(
                    onClick = { screenModel.confirmTransferOwnership(household.id) },
                    enabled = !uiState.isTransferring && selectedId != null,
                    isLoading = uiState.isTransferring,
                ) {
                    Text(stringResource(Res.string.sharing_members_transfer_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = screenModel::dismissTransferDialog,
                    enabled = !uiState.isTransferring,
                ) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }
}

@Composable
private fun mapErrorMessage(error: HouseholdMembersError): String =
    when (error) {
        HouseholdMembersError.Generic -> stringResource(Res.string.sharing_members_error_generic)
        HouseholdMembersError.EmailRequired -> stringResource(Res.string.sharing_members_error_email_required)
        HouseholdMembersError.NotConfigured -> stringResource(Res.string.sharing_members_error_not_configured)
        HouseholdMembersError.CannotAddSelf -> stringResource(Res.string.sharing_members_error_cannot_add_self)
        HouseholdMembersError.MemberAlreadyExists -> stringResource(Res.string.sharing_members_error_member_already_exists)
        HouseholdMembersError.InsufficientRole -> stringResource(Res.string.sharing_members_error_insufficient_role)
        HouseholdMembersError.InviteeHouseholdAlreadyActive ->
            stringResource(Res.string.sharing_members_error_invitee_household_already_active)
        HouseholdMembersError.MemberLimitReached ->
            stringResource(Res.string.sharing_members_error_member_limit)
        HouseholdMembersError.OwnerMustTransferOrDissolve ->
            stringResource(Res.string.sharing_members_error_owner_transfer_required)
        HouseholdMembersError.InvalidTransferTarget ->
            stringResource(Res.string.sharing_members_error_transfer_target)
        HouseholdMembersError.TransferTargetNotMember ->
            stringResource(Res.string.sharing_members_error_transfer_failed)
    }

@Composable
private fun MemberRow(
    member: HouseholdMember,
    actorRole: HouseholdMemberRole?,
    canManage: Boolean,
    onRemove: () -> Unit,
    onChangeRole: () -> Unit,
) {
    val canChangeRole = canManage &&
        member.role != HouseholdMemberRole.Owner &&
        actorRole?.canChangeRoleOf(member.role) == true
    val canRemove = canManage &&
        member.role != HouseholdMemberRole.Owner &&
        actorRole?.canRemoveMember(member.role) == true
    val showOverflow = canChangeRole || canRemove
    var menuExpanded by remember { mutableStateOf(false) }

    FamilyLogisticsCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${HouseholdMembersTestTags.MEMBER_ROW}_${member.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = member.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = JourneySemanticColors.inkDeep(),
                )
                HouseholdRoleBadge(
                    role = member.role,
                    kind = member.kind,
                )
            }
            if (showOverflow) {
                JourneyIconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("${HouseholdMembersTestTags.MEMBER_ROW_OVERFLOW}_${member.id}"),
                ) {
                    JourneyIcon(
                        role = AppIconRole.ChromeOverflow,
                        contentDescription = stringResource(Res.string.content_more_options),
                        tint = JourneySemanticColors.inkSecondary(),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    if (canChangeRole) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.sharing_members_change_role)) },
                            onClick = {
                                menuExpanded = false
                                onChangeRole()
                            },
                            modifier = Modifier.testTag(HouseholdMembersTestTags.MEMBER_CHANGE_ROLE_MENU),
                        )
                    }
                    if (canRemove) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.sharing_members_remove),
                                    color = JourneySemanticColors.brandTerracotta(),
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onRemove()
                            },
                            modifier = Modifier.testTag(HouseholdMembersTestTags.MEMBER_REMOVE_MENU),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingInviteRow(
    invite: HouseholdInvite,
    shareActionLabel: String,
    onShare: (() -> Unit)?,
) {
    val roleLabel = householdRoleLabel(role = invite.role)
    var menuExpanded by remember { mutableStateOf(false) }

    FamilyLogisticsCardSurface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${HouseholdMembersTestTags.PENDING_INVITE_ROW}_${invite.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = invite.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = JourneySemanticColors.inkDeep(),
                )
                HouseholdRoleBadge(role = invite.role)
                Text(
                    text = stringResource(Res.string.sharing_members_pending_invite_label, roleLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneySemanticColors.inkMuted(),
                )
            }
            if (onShare != null) {
                JourneyIconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("${HouseholdMembersTestTags.PENDING_INVITE_OVERFLOW}_${invite.id}"),
                ) {
                    JourneyIcon(
                        role = AppIconRole.ChromeOverflow,
                        contentDescription = stringResource(Res.string.content_more_options),
                        tint = JourneySemanticColors.inkSecondary(),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(shareActionLabel) },
                        onClick = {
                            menuExpanded = false
                            onShare()
                        },
                        modifier = Modifier.testTag("${HouseholdMembersTestTags.PENDING_INVITE_SHARE}_${invite.id}"),
                    )
                }
            }
        }
    }
}
