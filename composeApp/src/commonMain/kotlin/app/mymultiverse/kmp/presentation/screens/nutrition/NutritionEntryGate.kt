package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.mymultiverse.kmp.presentation.components.JourneyErrorContent
import app.mymultiverse.kmp.presentation.components.JourneyLoadingContent
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
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
                JourneyLoadingContent(
                    message = stringResource(Res.string.nutrition_entry_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    containerTestTag = NutritionEntryTestTags.LOADING,
                )
            }

            is NutritionEntryState.Error -> {
                val message = when (current.error) {
                    NutritionEntryError.Generic -> stringResource(Res.string.nutrition_entry_error_generic)
                    NutritionEntryError.NotConfigured -> stringResource(Res.string.nutrition_entry_error_not_configured)
                }
                JourneyErrorContent(
                    message = message,
                    retryLabel = stringResource(Res.string.nutrition_entry_retry),
                    onRetry = screenModel::ensureHousehold,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    containerTestTag = NutritionEntryTestTags.ERROR,
                    retryButtonTestTag = NutritionEntryTestTags.RETRY_BUTTON,
                )
            }

            is NutritionEntryState.Ready -> Unit
        }
    }
}
