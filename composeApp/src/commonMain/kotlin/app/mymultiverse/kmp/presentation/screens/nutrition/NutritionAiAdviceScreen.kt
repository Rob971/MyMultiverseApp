package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_ask_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_empty_question
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_error
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_question_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_allergy
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_protein
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_veggies
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestions_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_try_again
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

object NutritionAiTestTags {
    const val QUESTION_FIELD = "nutrition_ai_question"
    const val ASK_BUTTON = "nutrition_ai_ask_button"
    const val ANSWER_CARD = "nutrition_ai_answer"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NutritionAiAdviceScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val aiState by screenModel.aiState.collectAsState()
    var question by rememberSaveable { mutableStateOf("") }
    val isLoading = aiState is NutritionAiState.Loading
    val suggestions = listOf(
        stringResource(Res.string.nutrition_ai_suggestion_protein),
        stringResource(Res.string.nutrition_ai_suggestion_veggies),
        stringResource(Res.string.nutrition_ai_suggestion_allergy),
    )

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_ai_title),
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.screenContentArea(padding),
            contentPadding = screenListPadding(),
            verticalArrangement = Arrangement.spacedBy(ScreenLayout.sectionSpacing),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.nutrition_ai_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SharedJourneyColors.InkMuted,
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.nutrition_ai_suggestions_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = SharedJourneyColors.InkDeep,
                    fontWeight = FontWeight.SemiBold,
                )
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            label = suggestion,
                            enabled = !isLoading,
                            onClick = { question = suggestion },
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(NutritionAiTestTags.QUESTION_FIELD),
                    placeholder = { Text(stringResource(Res.string.nutrition_ai_question_hint)) },
                    shape = RoundedCornerShape(16.dp),
                    minLines = 4,
                    enabled = !isLoading,
                )
            }

            item {
                Button(
                    onClick = { screenModel.askNutritionAdvice(question) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(NutritionAiTestTags.ASK_BUTTON),
                    enabled = question.isNotBlank() && !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SharedJourneyColors.SunDrenchedWhite,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(Res.string.nutrition_ai_ask_button))
                    }
                }
            }

            when (val state = aiState) {
                NutritionAiState.Idle -> Unit
                NutritionAiState.Loading -> {
                    item {
                        Text(
                            text = stringResource(Res.string.nutrition_ai_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SharedJourneyColors.InkMuted,
                        )
                    }
                }
                is NutritionAiState.Answer -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(NutritionAiTestTags.ANSWER_CARD),
                            shape = RoundedCornerShape(24.dp),
                            color = SharedJourneyColors.GlassWhite,
                        ) {
                            Text(
                                text = state.text,
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = SharedJourneyColors.InkDeep,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                screenModel.resetAiState()
                                question = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(Res.string.nutrition_ai_try_again))
                        }
                    }
                }
                is NutritionAiState.Error -> {
                    item {
                        Text(
                            text = if (state.message == "empty_question") {
                                stringResource(Res.string.nutrition_ai_empty_question)
                            } else {
                                stringResource(Res.string.nutrition_ai_error)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = SharedJourneyColors.TerracottaOrange,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val chipModifier = if (enabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Surface(
        modifier = chipModifier,
        shape = RoundedCornerShape(20.dp),
        color = SharedJourneyColors.GlassTerracotta,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            SharedJourneyColors.TerracottaOrange.copy(alpha = 0.3f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = SharedJourneyColors.MediterraneanTeal,
        )
    }
}
