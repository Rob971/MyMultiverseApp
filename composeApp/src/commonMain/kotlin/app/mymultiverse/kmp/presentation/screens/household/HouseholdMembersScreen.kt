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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_add_person
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_cancel
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_confirm_add
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_email_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_email_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_empty
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
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_invite_sent
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_member_added
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_owner_fallback
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_pending_invite_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_pending_invites_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_read_only_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_remove
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_editor
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_owner
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_viewer
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_select_role
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object HouseholdMembersTestTags {
    const val ADD_PERSON_BUTTON = "household_members_add_person"
    const val ADD_PERSON_CONFIRM_BUTTON = "household_members_add_person_confirm"
    const val ADD_PERSON_DIALOG_ERROR = "household_members_add_person_error"
    const val MEMBER_ROW = "household_members_row"
    const val PENDING_INVITE_ROW = "household_members_pending_invite"
    const val TRANSFER_OWNERSHIP_BUTTON = "household_members_transfer_ownership"
}

@Composable
fun HouseholdMembersScreen(
    household: HouseholdContext,
    onBack: () -> Unit,
    screenModel: HouseholdMembersScreenModel = koinInject(),
    authRepository: AuthRepository = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()
    val authState by authRepository.authState.collectAsState()
    val ownerFallback = stringResource(Res.string.sharing_members_owner_fallback)
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = uiState.error?.let { error -> mapErrorMessage(error) }
    val dialogErrorMessage = uiState.dialogError?.let { error -> mapErrorMessage(error) }
    val successMessage = uiState.successMessageKey?.let { success ->
        when (success) {
            HouseholdMembersSuccess.InviteSent -> stringResource(
                Res.string.sharing_members_invite_sent,
                uiState.invitedEmailForSuccess.orEmpty(),
            )
            HouseholdMembersSuccess.MemberAdded -> stringResource(Res.string.sharing_members_member_added)
            HouseholdMembersSuccess.OwnershipTransferred ->
                stringResource(
                    Res.string.sharing_members_transfer_success,
                    uiState.transferredToDisplayName.orEmpty(),
                )
        }
    }
    val currentUserId = (authState as? AuthState.Authenticated)?.user?.id

    LaunchedEffect(household.id, household.ownerId, household.ownerDisplayName, currentUserId) {
        screenModel.bindHousehold(
            spaceId = household.id,
            ownerId = household.ownerId,
            ownerDisplayName = household.ownerDisplayName ?: ownerFallback,
            currentUserId = currentUserId,
        )
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
                CircularProgressIndicator()
                Text(
                    text = stringResource(Res.string.sharing_members_loading),
                    modifier = Modifier.padding(top = 12.dp),
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
                        color = SharedJourneyColors.InkDeep.copy(alpha = 0.75f),
                    )
                }
                if (!uiState.canManageMembers) {
                    item {
                        Text(
                            text = stringResource(Res.string.sharing_members_read_only_hint),
                            color = SharedJourneyColors.InkDeep.copy(alpha = 0.65f),
                        )
                    }
                }
                if (uiState.canManageMembers) {
                    item {
                        Button(
                            onClick = screenModel::openAddPersonDialog,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(HouseholdMembersTestTags.ADD_PERSON_BUTTON),
                        ) {
                            Text(stringResource(Res.string.sharing_members_add_person))
                        }
                    }
                }
                if (uiState.outboundInvites.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.sharing_members_pending_invites_title),
                            color = SharedJourneyColors.InkDeep,
                        )
                    }
                    items(uiState.outboundInvites, key = { it.id }) { invite ->
                        PendingInviteRow(invite = invite)
                    }
                }
                if (uiState.members.isEmpty()) {
                    item {
                        Text(stringResource(Res.string.sharing_members_empty))
                    }
                } else {
                    items(uiState.members, key = { it.id }) { member ->
                        MemberRow(
                            member = member,
                            canManage = uiState.canManageMembers,
                            onRemove = { screenModel.removeMember(member.id, household.id) },
                        )
                    }
                }
                if (uiState.showOwnerTransferHint) {
                    item {
                        Text(
                            text = stringResource(Res.string.sharing_members_owner_transfer_required),
                            color = SharedJourneyColors.InkDeep.copy(alpha = 0.65f),
                        )
                    }
                }
                if (uiState.canTransferOwnership) {
                    item {
                        Button(
                            onClick = screenModel::openTransferDialog,
                            enabled = !uiState.isTransferring,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(HouseholdMembersTestTags.TRANSFER_OWNERSHIP_BUTTON),
                        ) {
                            Text(stringResource(Res.string.sharing_members_transfer_ownership))
                        }
                    }
                }
                if (uiState.canLeave) {
                    item {
                        OutlinedButton(
                            onClick = screenModel::requestLeave,
                            enabled = !uiState.isLeaving,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.sharing_members_leave))
                        }
                    }
                }
                if (uiState.canDissolve) {
                    item {
                        OutlinedButton(
                            onClick = screenModel::requestDissolve,
                            enabled = !uiState.isLeaving,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.sharing_members_dissolve))
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

    if (uiState.showAddPersonDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissDialogs,
            title = { Text(stringResource(Res.string.sharing_members_add_person)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.emailInput,
                        onValueChange = screenModel::onEmailChange,
                        label = { Text(stringResource(Res.string.sharing_members_email_label)) },
                        placeholder = { Text(stringResource(Res.string.sharing_members_email_hint)) },
                        singleLine = true,
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.selectedRole == SpaceMemberRole.Editor,
                            onClick = { screenModel.onRoleChange(SpaceMemberRole.Editor) },
                            label = { Text(stringResource(Res.string.sharing_members_role_editor)) },
                        )
                        FilterChip(
                            selected = uiState.selectedRole == SpaceMemberRole.Viewer,
                            onClick = { screenModel.onRoleChange(SpaceMemberRole.Viewer) },
                            label = { Text(stringResource(Res.string.sharing_members_role_viewer)) },
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { screenModel.submitAddPerson(household.id) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.testTag(HouseholdMembersTestTags.ADD_PERSON_CONFIRM_BUTTON),
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp,
                        )
                    }
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
                Button(
                    onClick = screenModel::confirmLeaveOrDissolve,
                    enabled = !uiState.isLeaving,
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

    if (uiState.showTransferDialog) {
        val selectedId = uiState.selectedTransferMemberId
        val selectedMember = uiState.transferCandidates.find { it.referenceId == selectedId }
        AlertDialog(
            onDismissRequest = screenModel::dismissTransferDialog,
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
                Button(
                    onClick = { screenModel.confirmTransferOwnership(household.id) },
                    enabled = !uiState.isTransferring && selectedId != null,
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
    member: SpaceMember,
    canManage: Boolean,
    onRemove: () -> Unit,
) {
    val roleLabel = when (member.role) {
        SpaceMemberRole.Owner -> stringResource(Res.string.sharing_members_role_owner)
        SpaceMemberRole.Editor -> stringResource(Res.string.sharing_members_role_editor)
        SpaceMemberRole.Viewer -> stringResource(Res.string.sharing_members_role_viewer)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${HouseholdMembersTestTags.MEMBER_ROW}_${member.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = member.displayName)
            Text(
                text = stringResource(Res.string.sharing_members_role_label, roleLabel),
                color = SharedJourneyColors.InkDeep.copy(alpha = 0.65f),
            )
        }
        if (canManage && member.role != SpaceMemberRole.Owner) {
            TextButton(onClick = onRemove) {
                Text(stringResource(Res.string.sharing_members_remove))
            }
        }
    }
}

@Composable
private fun PendingInviteRow(invite: SpaceInvite) {
    val roleLabel = when (invite.role) {
        SpaceMemberRole.Owner -> stringResource(Res.string.sharing_members_role_owner)
        SpaceMemberRole.Editor -> stringResource(Res.string.sharing_members_role_editor)
        SpaceMemberRole.Viewer -> stringResource(Res.string.sharing_members_role_viewer)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${HouseholdMembersTestTags.PENDING_INVITE_ROW}_${invite.id}"),
    ) {
        Text(text = invite.email)
        Text(
            text = stringResource(Res.string.sharing_members_pending_invite_label, roleLabel),
            color = SharedJourneyColors.InkDeep.copy(alpha = 0.65f),
        )
    }
}
