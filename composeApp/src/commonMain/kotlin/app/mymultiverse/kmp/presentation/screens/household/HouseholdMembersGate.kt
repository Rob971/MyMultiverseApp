package app.mymultiverse.kmp.presentation.screens.household

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
import kmpvoyagercleanarchitecture.composeapp.generated.resources.sharing_members_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

object HouseholdMembersEntryTestTags {
    const val LOADING = "household_members_entry_loading"
    const val ERROR = "household_members_entry_error"
    const val RETRY_BUTTON = "household_members_entry_retry"
}

@Composable
fun HouseholdMembersGate(
    onBack: () -> Unit,
    onReady: (HouseholdContext) -> Unit,
    entryScreenModel: HouseholdMembersEntryScreenModel = koinInject(),
) {
    val state by entryScreenModel.state.collectAsState()

    LaunchedEffect(entryScreenModel) {
        entryScreenModel.ensureHousehold()
    }

    LaunchedEffect(state) {
        if (state is HouseholdMembersEntryState.Ready) {
            onReady((state as HouseholdMembersEntryState.Ready).household)
        }
    }

    NutritionScaffold(
        title = stringResource(Res.string.sharing_members_title),
        onBack = onBack,
    ) { padding ->
        when (val current = state) {
            HouseholdMembersEntryState.Loading -> {
                JourneyLoadingContent(
                    message = stringResource(Res.string.nutrition_entry_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    containerTestTag = HouseholdMembersEntryTestTags.LOADING,
                )
            }

            is HouseholdMembersEntryState.Error -> {
                val message = when (current.error) {
                    HouseholdMembersEntryError.Generic -> stringResource(Res.string.nutrition_entry_error_generic)
                    HouseholdMembersEntryError.NotConfigured -> stringResource(Res.string.nutrition_entry_error_not_configured)
                }
                JourneyErrorContent(
                    message = message,
                    retryLabel = stringResource(Res.string.nutrition_entry_retry),
                    onRetry = entryScreenModel::ensureHousehold,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    containerTestTag = HouseholdMembersEntryTestTags.ERROR,
                    retryButtonTestTag = HouseholdMembersEntryTestTags.RETRY_BUTTON,
                )
            }

            is HouseholdMembersEntryState.Ready -> Unit
        }
    }
}
