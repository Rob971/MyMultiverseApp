package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.sharing_members_role_admin
import ammo.composeapp.generated.resources.sharing_members_role_admin_description
import ammo.composeapp.generated.resources.sharing_members_role_editor
import ammo.composeapp.generated.resources.sharing_members_role_editor_description
import ammo.composeapp.generated.resources.sharing_members_role_viewer
import ammo.composeapp.generated.resources.sharing_members_role_viewer_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun HouseholdRoleSelector(
    selectedRole: HouseholdMemberRole,
    onRoleSelected: (HouseholdMemberRole) -> Unit,
    showAdminOption: Boolean,
    modifier: Modifier = Modifier,
) {
    val options = buildList {
        add(
            Triple(
                HouseholdMemberRole.Admin,
                stringResource(Res.string.sharing_members_role_admin),
                stringResource(Res.string.sharing_members_role_admin_description),
            ),
        )
        add(
            Triple(
                HouseholdMemberRole.Editor,
                stringResource(Res.string.sharing_members_role_editor),
                stringResource(Res.string.sharing_members_role_editor_description),
            ),
        )
        add(
            Triple(
                HouseholdMemberRole.Viewer,
                stringResource(Res.string.sharing_members_role_viewer),
                stringResource(Res.string.sharing_members_role_viewer_description),
            ),
        )
    }.filter { (role, _, _) -> role != HouseholdMemberRole.Admin || showAdminOption }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (role, title, description) ->
            val selected = selectedRole == role
            val colors = householdRoleBadgeColors(role = role)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        onClick = { onRoleSelected(role) },
                        role = Role.RadioButton,
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RadioButton(
                    selected = selected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = colors.content,
                        unselectedColor = JourneySemanticColors.inkMuted(),
                    ),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    HouseholdRoleBadge(role = role)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = JourneySemanticColors.inkMuted(),
                    )
                }
            }
        }
    }
}
