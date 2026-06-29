package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.home_household_name_edit
import ammo.composeapp.generated.resources.home_household_open_manage
import ammo.composeapp.generated.resources.home_household_open_view
import org.jetbrains.compose.resources.stringResource

object HomeHouseholdButtonTestTags {
    const val BUTTON = "home_household_button"
    const val EDIT = "home_household_name_edit"
}

@Composable
fun HomeHouseholdButton(
    householdName: String,
    canManage: Boolean,
    onOpenHousehold: () -> Unit,
    onRenameHousehold: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brandTeal = JourneySemanticColors.brandTeal()
    val subtitle = if (canManage) {
        stringResource(Res.string.home_household_open_manage)
    } else {
        stringResource(Res.string.home_household_open_view)
    }
    val accessibilityLabel = "$householdName. $subtitle"

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenHousehold)
            .semantics {
                role = Role.Button
                contentDescription = accessibilityLabel
            },
        shape = RoundedCornerShape(24.dp),
        color = brandTeal.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = householdName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = brandTeal,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneySemanticColors.inkMuted(),
                )
            }
            if (canManage) {
                JourneyIconButton(
                    onClick = onRenameHousehold,
                    modifier = Modifier.testTag(HomeHouseholdButtonTestTags.EDIT),
                ) {
                    JourneyIcon(
                        role = AppIconRole.ActionEdit,
                        contentDescription = stringResource(Res.string.home_household_name_edit),
                        tint = brandTeal,
                    )
                }
            }
            JourneyIcon(
                role = AppIconRole.ChromeChevronRight,
                contentDescription = null,
                tint = brandTeal.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
