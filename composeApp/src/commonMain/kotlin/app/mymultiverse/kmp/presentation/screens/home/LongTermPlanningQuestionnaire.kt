package app.mymultiverse.kmp.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun LongTermPlanningQuestionnaire(
    milestoneType: String,
    onMilestoneTypeChange: (String) -> Unit,
    roadblock: String,
    onRoadblockChange: (String) -> Unit,
    timeline: String,
    onTimelineChange: (String) -> Unit,
    budgetStyle: String,
    onBudgetStyleChange: (String) -> Unit,
    successDefinition: String,
    onSuccessDefinitionChange: (String) -> Unit,
    onGenerateBlueprint: () -> Unit,
) {
    val milestoneOptions = longTermMilestoneOptions()
    val roadblockOptions = longTermRoadblockOptions()
    val timelineOptions = longTermTimelineOptions()
    val budgetOptions = longTermBudgetStyleOptions()
    val canGenerateBlueprint = milestoneType.isNotBlank() &&
        roadblock.isNotBlank() &&
        timeline.isNotBlank() &&
        budgetStyle.isNotBlank() &&
        successDefinition.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(Res.string.longterm_q_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )
        Text(
            stringResource(Res.string.longterm_q_desc),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
        )

        LocalizedQuestionBlock(
            title = Res.string.longterm_q_milestone_title,
            description = Res.string.longterm_q_milestone_desc,
        ) {
            LocalizedOptionChips(milestoneOptions, milestoneType, onMilestoneTypeChange)
        }

        LocalizedQuestionBlock(
            title = Res.string.longterm_q_roadblock_title,
            description = Res.string.longterm_q_roadblock_desc,
        ) {
            LocalizedOptionChips(roadblockOptions, roadblock, onRoadblockChange)
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        Text(
            stringResource(Res.string.longterm_q_boundaries_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )
        LocalizedQuestionBlock(
            title = Res.string.longterm_q_timeline_title,
            description = Res.string.longterm_q_timeline_desc,
        ) {
            LocalizedOptionChips(timelineOptions, timeline, onTimelineChange)
        }
        LocalizedQuestionBlock(
            title = Res.string.longterm_q_budget_title,
            description = Res.string.longterm_q_budget_desc,
        ) {
            LocalizedOptionChips(budgetOptions, budgetStyle, onBudgetStyleChange)
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        LocalizedQuestionBlock(
            title = Res.string.longterm_q_success_title,
            description = Res.string.longterm_q_success_desc,
        ) {
            TextField(
                value = successDefinition,
                onValueChange = onSuccessDefinitionChange,
                placeholder = { Text(stringResource(Res.string.longterm_q_success_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                colors = longTermTextFieldColors(),
                shape = RoundedCornerShape(16.dp),
            )
        }

        Button(
            onClick = onGenerateBlueprint,
            enabled = canGenerateBlueprint,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal),
        ) {
            Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(Res.string.longterm_q_generate_button),
                fontWeight = FontWeight.Bold,
            )
        }

        if (!canGenerateBlueprint) {
            Text(
                stringResource(Res.string.longterm_q_complete_hint),
                style = MaterialTheme.typography.labelSmall,
                color = SharedJourneyColors.InkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun longTermTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
    unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
    focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
    unfocusedIndicatorColor = Color.Transparent,
)
