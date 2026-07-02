package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors

object GroceryPartnerNudgeTestTags {
    const val ROOT = "grocery_partner_nudge"
    const val ACTION = "grocery_partner_nudge_action"
}

@Composable
fun GroceryPartnerNudgeCard(
    title: String,
    body: String,
    actionLabel: String,
    onNudgePartners: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    FamilyLogisticsCardSurface(
        modifier = modifier.testTag(GroceryPartnerNudgeTestTags.ROOT),
        accentColor = SharedJourneyColors.MediterraneanTeal,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
            )
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
                    .testTag(GroceryPartnerNudgeTestTags.ACTION),
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
