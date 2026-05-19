package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_ask_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_empty_question
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_error
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_question_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_title
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@Composable
fun NutritionAiAdviceScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val aiState by screenModel.aiState.collectAsState()
    var question by rememberSaveable { mutableStateOf("") }
    val isLoading = aiState is NutritionAiState.Loading

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_ai_title),
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.nutrition_ai_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SharedJourneyColors.InkMuted,
                )
            }

            item {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(Res.string.nutrition_ai_question_hint)) },
                    shape = RoundedCornerShape(16.dp),
                    minLines = 4,
                    enabled = !isLoading,
                )
            }

            item {
                Button(
                    onClick = { screenModel.askNutritionAdvice(question) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = question.isNotBlank() && !isLoading,
                ) {
                    Text(stringResource(Res.string.nutrition_ai_ask_button))
                }
            }

            when (val state = aiState) {
                NutritionAiState.Idle -> Unit
                NutritionAiState.Loading -> {
                    item {
                        CircularProgressIndicator(
                            color = SharedJourneyColors.MediterraneanTeal,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
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
                            modifier = Modifier.fillMaxWidth(),
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
