package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object MealPlanEmptyStateTestTags {
    const val ROOT = "meal_plan_empty_state"
    const val PLAN_WITH_AI = "meal_plan_empty_plan_with_ai"
    const val ADD_MANUALLY = "meal_plan_empty_add_manually"
}

@Composable
fun MealPlanEmptyState(
    title: String,
    body: String,
    planWithAiLabel: String,
    addManuallyLabel: String,
    onPlanWithAi: () -> Unit,
    onAddManually: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyStateCard(
        message = title,
        icon = AppIcons.DateRange,
        modifier = modifier.testTag(MealPlanEmptyStateTestTags.ROOT),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = SharedJourneyColors.InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Button(
            onClick = onPlanWithAi,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(MealPlanEmptyStateTestTags.PLAN_WITH_AI),
        ) {
            Text(planWithAiLabel)
        }
        OutlinedButton(
            onClick = onAddManually,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(MealPlanEmptyStateTestTags.ADD_MANUALLY),
        ) {
            Text(addManuallyLabel)
        }
    }
}
