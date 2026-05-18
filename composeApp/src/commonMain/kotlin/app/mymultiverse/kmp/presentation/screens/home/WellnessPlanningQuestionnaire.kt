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
import androidx.compose.material3.Surface
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
fun WellnessPlanningQuestionnaire(
    conflictTopic: String,
    onConflictTopicChange: (String) -> Unit,
    conflictDraft: String,
    onConflictDraftChange: (String) -> Unit,
    partnerLoveLanguage: String,
    onPartnerLoveLanguageChange: (String) -> Unit,
    availableTime: String,
    onAvailableTimeChange: (String) -> Unit,
    budget: String,
    onBudgetChange: (String) -> Unit,
    energyLevel: String,
    onEnergyLevelChange: (String) -> Unit,
    stressLevel: String,
    onStressLevelChange: (String) -> Unit,
    connectionLevel: String,
    onConnectionLevelChange: (String) -> Unit,
    weeklyDrain: String,
    onWeeklyDrainChange: (String) -> Unit,
    dateNightDuration: String,
    onDateNightDurationChange: (String) -> Unit,
    dateNightLikes: String,
    onDateNightLikesChange: (String) -> Unit,
    dateNightAvoids: String,
    onDateNightAvoidsChange: (String) -> Unit,
    onGenerateCouplesWellnessPlan: () -> Unit,
) {
    val loveLanguageOptions = wellnessLoveLanguageOptions()
    val timeOptions = wellnessTimeOptions()
    val budgetOptions = wellnessBudgetOptions()
    val drainOptions = wellnessDrainOptions()
    val durationOptions = wellnessDurationOptions()
    val canGeneratePlan = listOf(
        conflictTopic,
        conflictDraft,
        partnerLoveLanguage,
        availableTime,
        budget,
        energyLevel,
        stressLevel,
        connectionLevel,
        weeklyDrain,
        dateNightDuration,
        dateNightLikes,
        dateNightAvoids,
    ).any { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SharedJourneyColors.GlassWhite, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(Res.string.wellness_q_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )
        Text(
            stringResource(Res.string.wellness_q_desc),
            style = MaterialTheme.typography.bodySmall,
            color = SharedJourneyColors.InkMuted,
        )

        LocalizedQuestionBlock(
            title = Res.string.wellness_q_conflict_title,
            description = Res.string.wellness_q_conflict_desc,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = conflictTopic,
                    onValueChange = onConflictTopicChange,
                    placeholder = { Text(stringResource(Res.string.wellness_q_conflict_topic_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = wellnessTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                )
                TextField(
                    value = conflictDraft,
                    onValueChange = onConflictDraftChange,
                    placeholder = { Text(stringResource(Res.string.wellness_q_conflict_draft_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = wellnessTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        LocalizedQuestionBlock(
            title = Res.string.wellness_q_love_language_title,
            description = Res.string.wellness_q_love_language_desc,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LocalizedOptionChips(loveLanguageOptions, partnerLoveLanguage, onPartnerLoveLanguageChange)
                LocalizedOptionChips(timeOptions, availableTime, onAvailableTimeChange)
                LocalizedOptionChips(budgetOptions, budget, onBudgetChange)
            }
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        Text(
            stringResource(Res.string.wellness_q_temp_check_title),
            style = MaterialTheme.typography.titleMedium,
            color = SharedJourneyColors.MediterraneanTeal,
            fontWeight = FontWeight.Black,
        )
        LocalizedWellnessSlider(Res.string.wellness_q_energy_level, energyLevel, onEnergyLevelChange)
        LocalizedWellnessSlider(Res.string.wellness_q_stress_level, stressLevel, onStressLevelChange)
        LocalizedWellnessSlider(Res.string.wellness_q_connection_level, connectionLevel, onConnectionLevelChange)
        LocalizedQuestionBlock(
            title = Res.string.wellness_q_drain_title,
            description = Res.string.wellness_q_drain_desc,
        ) {
            LocalizedOptionChips(drainOptions, weeklyDrain, onWeeklyDrainChange)
        }

        HorizontalDivider(color = SharedJourneyColors.ParchmentWarm)

        LocalizedQuestionBlock(
            title = Res.string.wellness_q_date_night_title,
            description = Res.string.wellness_q_date_night_desc,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LocalizedOptionChips(durationOptions, dateNightDuration, onDateNightDurationChange)
                TextField(
                    value = dateNightLikes,
                    onValueChange = onDateNightLikesChange,
                    placeholder = { Text(stringResource(Res.string.wellness_q_date_night_likes_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = wellnessTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                )
                TextField(
                    value = dateNightAvoids,
                    onValueChange = onDateNightAvoidsChange,
                    placeholder = { Text(stringResource(Res.string.wellness_q_date_night_avoids_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = wellnessTextFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                stringResource(Res.string.wellness_q_golden_rule),
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.MediterraneanTeal,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(14.dp),
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = onGenerateCouplesWellnessPlan,
            enabled = canGeneratePlan,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SharedJourneyColors.MediterraneanTeal),
        ) {
            Icon(AppIcons.Sparkles, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                stringResource(Res.string.wellness_q_generate_button),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun wellnessTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
    unfocusedContainerColor = SharedJourneyColors.SunDrenchedWhite,
    focusedIndicatorColor = SharedJourneyColors.MediterraneanTeal,
    unfocusedIndicatorColor = Color.Transparent,
)
