package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

data class MealPlanEmptyAiChip(
    val label: String,
    val testTag: String,
    val onClick: () -> Unit,
)

object MealPlanEmptyStateTestTags {
    const val ROOT = "meal_plan_empty_state"
    const val PLAN_WITH_AI = "meal_plan_empty_plan_with_ai"
    const val ADD_MANUALLY = "meal_plan_empty_add_manually"
    const val CHIP_PROTEIN = "meal_plan_empty_chip_protein"
    const val CHIP_BUDGET = "meal_plan_empty_chip_budget"
    const val CHIP_VEGGIES = "meal_plan_empty_chip_veggies"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealPlanEmptyState(
    title: String,
    body: String,
    planWithAiLabel: String,
    addManuallyLabel: String,
    onPlanWithAi: () -> Unit,
    onAddManually: () -> Unit,
    modifier: Modifier = Modifier,
    aiChips: List<MealPlanEmptyAiChip> = emptyList(),
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
        extraContent = if (aiChips.isNotEmpty()) {
            {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    aiChips.forEach { chip ->
                        MealPlanEmptyAiChipSurface(
                            label = chip.label,
                            enabled = true,
                            onClick = chip.onClick,
                            modifier = Modifier.testTag(chip.testTag),
                        )
                    }
                }
            }
        } else {
            null
        },
    )
}

@Composable
private fun MealPlanEmptyAiChipSurface(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(
            enabled = enabled,
            role = Role.Button,
        ) { onClick() },
        shape = FamilyLogisticsDesign.fieldShape,
        color = SharedJourneyColors.GlassTerracotta,
        border = BorderStroke(
            1.dp,
            SharedJourneyColors.AiReadOnlyAccent.copy(alpha = 0.35f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = SharedJourneyColors.AiReadOnlyAccent,
        )
    }
}
