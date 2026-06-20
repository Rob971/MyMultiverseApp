package app.mymultiverse.kmp.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.mymultiverse.kmp.presentation.theme.AppIcons

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
    JourneyEmptyState(
        title = title,
        body = body,
        icon = AppIcons.DateRange,
        primaryActionLabel = planWithAiLabel,
        onPrimaryAction = onPlanWithAi,
        primaryActionTestTag = MealPlanEmptyStateTestTags.PLAN_WITH_AI,
        secondaryActionLabel = addManuallyLabel,
        onSecondaryAction = onAddManually,
        secondaryActionTestTag = MealPlanEmptyStateTestTags.ADD_MANUALLY,
        modifier = modifier.testTag(MealPlanEmptyStateTestTags.ROOT),
    )
}
