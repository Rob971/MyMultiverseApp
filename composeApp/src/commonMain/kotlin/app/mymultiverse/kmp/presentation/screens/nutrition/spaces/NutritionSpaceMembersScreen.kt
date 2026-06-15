package app.mymultiverse.kmp.presentation.screens.nutrition.spaces

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.sharing.GroupLifecycle
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberKind
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.navigation.NutritionSpaceContext
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_add_group
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_add_person
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_cancel
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_confirm_add
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_create_group_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_email_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_email_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_empty
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_email_not_found
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_email_required
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_generic
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_group_name_required
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_error_not_configured
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_group_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_group_lifecycle_event
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_group_lifecycle_persistent
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_group_name_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_group_pick_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_person_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_remove
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object NutritionSpaceMembersTestTags {
    const val ADD_PERSON_BUTTON = "nutrition_members_add_person"
    const val ADD_GROUP_BUTTON = "nutrition_members_add_group"
    const val MEMBER_ROW = "nutrition_members_row"
}

@Composable
fun NutritionSpaceMembersScreen(
    space: NutritionSpaceContext,
    onBack: () -> Unit,
    screenModel: NutritionSpaceMembersScreenModel = koinInject(),
) {
    val uiState by screenModel.uiState.collectAsState()
    val errorMessage = uiState.error?.let { error ->
        when (error) {
            SpaceMembersError.Generic -> stringResource(Res.string.sharing_members_error_generic)
            SpaceMembersError.EmailRequired -> stringResource(Res.string.sharing_members_error_email_required)
            SpaceMembersError.EmailNotFound -> stringResource(Res.string.sharing_members_error_email_not_found)
            SpaceMembersError.GroupNameRequired -> stringResource(Res.string.sharing_members_error_group_name_required)
            SpaceMembersError.NotConfigured -> stringResource(Res.string.sharing_members_error_not_configured)
        }
    }

    LaunchedEffect(space.id) {
        screenModel.bindSpace(space.id)
    }

    NutritionScaffold(
        title = stringResource(Res.string.sharing_members_title),
        subtitle = space.name,
        onBack = onBack,
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
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = screenModel::openAddPersonDialog,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(NutritionSpaceMembersTestTags.ADD_PERSON_BUTTON),
                        ) {
                            Text(stringResource(Res.string.sharing_members_add_person))
                        }
                        OutlinedButton(
                            onClick = screenModel::openAddGroupDialog,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(NutritionSpaceMembersTestTags.ADD_GROUP_BUTTON),
                        ) {
                            Text(stringResource(Res.string.sharing_members_add_group))
                        }
                    }
                }
                item {
                    OutlinedButton(
                        onClick = screenModel::openCreateGroupDialog,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.sharing_members_create_group_title))
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
                            onRemove = { screenModel.removeMember(member.id) },
                        )
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
                OutlinedTextField(
                    value = uiState.emailInput,
                    onValueChange = screenModel::onEmailChange,
                    label = { Text(stringResource(Res.string.sharing_members_email_label)) },
                    placeholder = { Text(stringResource(Res.string.sharing_members_email_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = { screenModel.submitAddPerson(space.id) },
                    enabled = !uiState.isSaving,
                ) {
                    Text(stringResource(Res.string.sharing_members_confirm_add))
                }
            },
            dismissButton = {
                TextButton(onClick = screenModel::dismissDialogs) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    if (uiState.showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissDialogs,
            title = { Text(stringResource(Res.string.sharing_members_create_group_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.groupNameInput,
                        onValueChange = screenModel::onGroupNameChange,
                        label = { Text(stringResource(Res.string.sharing_members_group_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.groupLifecycle == GroupLifecycle.Persistent,
                            onClick = { screenModel.onGroupLifecycleChange(GroupLifecycle.Persistent) },
                            label = { Text(stringResource(Res.string.sharing_members_group_lifecycle_persistent)) },
                        )
                        FilterChip(
                            selected = uiState.groupLifecycle == GroupLifecycle.Event,
                            onClick = { screenModel.onGroupLifecycleChange(GroupLifecycle.Event) },
                            label = { Text(stringResource(Res.string.sharing_members_group_lifecycle_event)) },
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { screenModel.submitCreateGroup(space.id) },
                    enabled = !uiState.isSaving,
                ) {
                    Text(stringResource(Res.string.sharing_members_confirm_add))
                }
            },
            dismissButton = {
                TextButton(onClick = screenModel::dismissDialogs) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }

    if (uiState.showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = screenModel::dismissDialogs,
            title = { Text(stringResource(Res.string.sharing_members_group_pick_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (uiState.groups.isEmpty()) {
                        Text(stringResource(Res.string.sharing_members_empty))
                    } else {
                        uiState.groups.forEach { group ->
                            OutlinedButton(
                                onClick = {
                                    screenModel.addExistingGroupToSpace(space.id, group.id)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSaving,
                            ) {
                                Text(group.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = screenModel::dismissDialogs) {
                    Text(stringResource(Res.string.sharing_members_cancel))
                }
            },
        )
    }
}

@Composable
private fun MemberRow(
    member: SpaceMember,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("${NutritionSpaceMembersTestTags.MEMBER_ROW}_${member.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = member.displayName)
            Text(
                text = when (member.kind) {
                    SpaceMemberKind.Person -> stringResource(Res.string.sharing_members_person_label)
                    SpaceMemberKind.Group -> stringResource(Res.string.sharing_members_group_label)
                },
                color = SharedJourneyColors.InkDeep.copy(alpha = 0.65f),
            )
        }
        TextButton(onClick = onRemove) {
            Text(stringResource(Res.string.sharing_members_remove))
        }
    }
}
