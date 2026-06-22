package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors

object HomeFirstWinChecklistTestTags {
    const val ROOT = "home_first_win_checklist"
    const val PLAN_ACTION = "home_first_win_plan_action"
    const val INVITE_ACTION = "home_first_win_invite_action"
    const val DISMISS = "home_first_win_dismiss"
}

@Composable
fun HomeFirstWinChecklistCard(
    title: String,
    planTitle: String,
    planActionLabel: String,
    inviteTitle: String,
    inviteActionLabel: String,
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
        accentColor = JourneySemanticColors.brandTeal(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JourneySemanticColors.inkDeep(),
                    modifier = Modifier.weight(1f),
                )
                JourneyIconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .testTag(HomeFirstWinChecklistTestTags.DISMISS)
                        .semantics { contentDescription = dismissLabel },
                ) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = null,
                        tint = JourneySemanticColors.inkMuted(),
                    )
                }
            }

            HomeFirstWinActionRow(
                title = planTitle,
                actionLabel = planActionLabel,
                complete = nutritionComplete,
                onClick = onNutritionClick,
                testTag = HomeFirstWinChecklistTestTags.PLAN_ACTION,
                primary = true,
            )
            HomeFirstWinActionRow(
                title = inviteTitle,
                actionLabel = inviteActionLabel,
                complete = inviteComplete,
                onClick = onInviteClick,
                testTag = HomeFirstWinChecklistTestTags.INVITE_ACTION,
                primary = false,
            )
        }
    }
}

@Composable
private fun HomeFirstWinActionRow(
    title: String,
    actionLabel: String,
    complete: Boolean,
    onClick: () -> Unit,
    testTag: String,
    primary: Boolean,
) {
    val titleColor = if (complete) {
        JourneySemanticColors.inkMuted()
    } else {
        JourneySemanticColors.inkDeep()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (complete) {
                Icon(
                    imageVector = AppIcons.CheckCircle,
                    contentDescription = null,
                    tint = JourneySemanticColors.brandTeal(),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                modifier = Modifier.weight(1f),
            )
        }
        if (!complete) {
            if (primary) {
                JourneyPrimaryButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                ) {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(actionLabel)
                }
            } else {
                JourneySecondaryButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag),
                ) {
                    Icon(
                        imageVector = AppIcons.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(actionLabel)
                }
            }
        }
    }
}
