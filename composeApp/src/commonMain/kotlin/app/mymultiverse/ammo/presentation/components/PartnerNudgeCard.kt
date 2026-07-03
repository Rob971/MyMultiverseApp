package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors

object GroceryPartnerNudgeTestTags {
    const val ROOT = "grocery_partner_nudge"
    const val ACTION = "grocery_partner_nudge_action"
}

object MealPlanPartnerNudgeTestTags {
    const val ROOT = "meal_plan_partner_nudge"
    const val ACTION = "meal_plan_partner_nudge_action"
    const val TOOLBAR = "meal_plan_partner_nudge_toolbar"
}

@Composable
fun PartnerNudgeCard(
    title: String,
    body: String,
    actionLabel: String,
    onNudgePartners: () -> Unit,
    loading: Boolean,
    accentColor: Color,
    rootTestTag: String,
    actionTestTag: String,
    modifier: Modifier = Modifier,
) {
    FamilyLogisticsCardSurface(
        modifier = modifier.testTag(rootTestTag),
        accentColor = accentColor,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JourneyIcon(
                    role = AppIconRole.NotifyPartners,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = accentColor,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JourneySemanticColors.inkDeep(),
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = JourneySemanticColors.inkMuted(),
            )
            JourneyPrimaryButton(
                onClick = onNudgePartners,
                enabled = !loading,
                isLoading = loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(actionTestTag),
            ) {
                JourneyButtonLabel(
                    text = actionLabel,
                    icon = AppIcons.Notifications,
                    role = AppIconRole.OnAccent,
                    useContentColor = true,
                )
            }
        }
    }
}
