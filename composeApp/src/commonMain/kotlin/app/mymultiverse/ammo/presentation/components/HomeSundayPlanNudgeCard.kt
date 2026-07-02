package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors

object HomeSundayPlanNudgeTestTags {
    const val ROOT = "home_sunday_plan_nudge"
    const val ACTION = "home_sunday_plan_nudge_action"
    const val DISMISS = "home_sunday_plan_nudge_dismiss"
}

@Composable
fun HomeSundayPlanNudgeCard(
    title: String,
    body: String,
    actionLabel: String,
    dismissLabel: String,
    onOpenMealPlan: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FamilyLogisticsCardSurface(
        modifier = modifier.testTag(HomeSundayPlanNudgeTestTags.ROOT),
        accentColor = SharedJourneyColors.MediterraneanTeal,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                }
                JourneyIconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .testTag(HomeSundayPlanNudgeTestTags.DISMISS)
                        .semantics { contentDescription = dismissLabel },
                ) {
                    JourneyIcon(
                        role = AppIconRole.ChromeClose,
                        contentDescription = null,
                    )
                }
            }
            JourneyPrimaryButton(
                onClick = onOpenMealPlan,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(HomeSundayPlanNudgeTestTags.ACTION),
            ) {
                JourneyButtonLabel(
                    text = actionLabel,
                    nutritionFeature = NutritionFeatureKind.MealPlan,
                    role = AppIconRole.OnAccent,
                    useContentColor = true,
                )
            }
        }
    }
}
