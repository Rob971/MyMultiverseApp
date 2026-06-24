package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberKind
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_kind_dependent
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_admin
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_editor
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_owner
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_role_viewer
import org.jetbrains.compose.resources.stringResource

data class HouseholdRoleBadgeColors(
    val container: Color,
    val content: Color,
)

@Composable
fun householdRoleBadgeColors(
    role: HouseholdMemberRole,
    kind: HouseholdMemberKind = HouseholdMemberKind.Person,
): HouseholdRoleBadgeColors {
    if (kind == HouseholdMemberKind.Dependant) {
        return HouseholdRoleBadgeColors(
            container = JourneySemanticColors.elevatedSurface(),
            content = JourneySemanticColors.inkMuted(),
        )
    }
    return when (role) {
        HouseholdMemberRole.Owner -> HouseholdRoleBadgeColors(
            container = JourneySemanticColors.brandTerracotta().copy(alpha = 0.18f),
            content = JourneySemanticColors.brandTerracotta(),
        )
        HouseholdMemberRole.Admin -> HouseholdRoleBadgeColors(
            container = JourneySemanticColors.brandTeal().copy(alpha = 0.22f),
            content = JourneySemanticColors.brandTeal(),
        )
        HouseholdMemberRole.Editor -> HouseholdRoleBadgeColors(
            container = JourneySemanticColors.brandTealContainer(),
            content = JourneySemanticColors.inkDeep(),
        )
        HouseholdMemberRole.Viewer -> HouseholdRoleBadgeColors(
            container = JourneySemanticColors.inkMuted().copy(alpha = 0.16f),
            content = JourneySemanticColors.inkMuted(),
        )
    }
}

@Composable
fun householdRoleLabel(
    role: HouseholdMemberRole,
    kind: HouseholdMemberKind = HouseholdMemberKind.Person,
): String =
    when (kind) {
        HouseholdMemberKind.Dependant -> stringResource(Res.string.sharing_members_kind_dependent)
        else -> when (role) {
            HouseholdMemberRole.Owner -> stringResource(Res.string.sharing_members_role_owner)
            HouseholdMemberRole.Admin -> stringResource(Res.string.sharing_members_role_admin)
            HouseholdMemberRole.Editor -> stringResource(Res.string.sharing_members_role_editor)
            HouseholdMemberRole.Viewer -> stringResource(Res.string.sharing_members_role_viewer)
        }
    }

@Composable
fun HouseholdRoleBadge(
    role: HouseholdMemberRole,
    modifier: Modifier = Modifier,
    kind: HouseholdMemberKind = HouseholdMemberKind.Person,
) {
    val colors = householdRoleBadgeColors(role = role, kind = kind)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.container,
    ) {
        Text(
            text = householdRoleLabel(role = role, kind = kind),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.content,
            fontWeight = FontWeight.Medium,
        )
    }
}
