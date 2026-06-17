package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_entry_error_generic
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_entry_error_not_configured
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_entry_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_entry_retry
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_hub_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object NutritionEntryTestTags {
    const val LOADING = "nutrition_entry_loading"
    const val ERROR = "nutrition_entry_error"
    const val RETRY_BUTTON = "nutrition_entry_retry"
}

@Composable
fun NutritionEntryGate(
    onBack: () -> Unit,
    onReady: (HouseholdContext) -> Unit,
    screenModel: NutritionEntryScreenModel = koinInject(),
) {
    val state by screenModel.state.collectAsState()

    LaunchedEffect(screenModel) {
        screenModel.ensureHousehold()
    }

    LaunchedEffect(state) {
        if (state is NutritionEntryState.Ready) {
            onReady((state as NutritionEntryState.Ready).household)
        }
    }

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_hub_title),
        onBack = onBack,
    ) { padding ->
        when (val current = state) {
            NutritionEntryState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .testTag(NutritionEntryTestTags.LOADING),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(Res.string.nutrition_entry_loading),
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }

            is NutritionEntryState.Error -> {
                val message = when (current.error) {
                    NutritionEntryError.Generic -> stringResource(Res.string.nutrition_entry_error_generic)
                    NutritionEntryError.NotConfigured -> stringResource(Res.string.nutrition_entry_error_not_configured)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .testTag(NutritionEntryTestTags.ERROR),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = message,
                        color = SharedJourneyColors.TerracottaOrange,
                    )
                    Button(
                        onClick = screenModel::ensureHousehold,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .testTag(NutritionEntryTestTags.RETRY_BUTTON),
                    ) {
                        Text(stringResource(Res.string.nutrition_entry_retry))
                    }
                }
            }

            is NutritionEntryState.Ready -> Unit
        }
    }
}
