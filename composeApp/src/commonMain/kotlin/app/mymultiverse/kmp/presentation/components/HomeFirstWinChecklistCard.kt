package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object HomeFirstWinChecklistTestTags {
    const val ROOT = "home_first_win_checklist"
    const val INVITE_STEP = "home_first_win_invite_step"
    const val NUTRITION_STEP = "home_first_win_nutrition_step"
    const val DISMISS = "home_first_win_dismiss"
}

@Composable
fun HomeFirstWinChecklistCard(
    title: String,
    inviteLabel: String,
    nutritionLabel: String,
    dismissLabel: String,
    inviteComplete: Boolean,
    nutritionComplete: Boolean,
    onInviteClick: () -> Unit,
    onNutritionClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FamilyLogisticsCardSurface(
        modifier = modifier.testTag(HomeFirstWinChecklistTestTags.ROOT),
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
                color = SharedJourneyColors.InkDeep,
            )
            HomeFirstWinChecklistStepRow(
                label = inviteLabel,
                complete = inviteComplete,
                enabled = !inviteComplete,
                onClick = onInviteClick,
                testTag = HomeFirstWinChecklistTestTags.INVITE_STEP,
            )
            HomeFirstWinChecklistStepRow(
                label = nutritionLabel,
                complete = nutritionComplete,
                enabled = !nutritionComplete,
                onClick = onNutritionClick,
                testTag = HomeFirstWinChecklistTestTags.NUTRITION_STEP,
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag(HomeFirstWinChecklistTestTags.DISMISS),
            ) {
                Text(dismissLabel)
            }
        }
    }
}

@Composable
private fun HomeFirstWinChecklistStepRow(
    label: String,
    complete: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    val icon = if (complete) AppIcons.CheckCircle else AppIcons.RadioButtonUnchecked
    val iconTint = if (complete) {
        SharedJourneyColors.MediterraneanTeal
    } else {
        SharedJourneyColors.InkMuted
    }
    val clickModifier = if (enabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .testTag(testTag),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (complete) SharedJourneyColors.InkMuted else SharedJourneyColors.InkDeep,
        )
    }
}
