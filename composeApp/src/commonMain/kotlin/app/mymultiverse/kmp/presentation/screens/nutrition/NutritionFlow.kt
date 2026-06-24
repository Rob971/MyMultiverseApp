package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.mymultiverse.kmp.presentation.components.JourneyLoadingContent
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_entry_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_hub_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun NutritionFlow(
    household: HouseholdContext?,
    section: NutritionSection,
    initialAiMode: NutritionAiMode? = null,
    onBack: () -> Unit,
    onOpenSection: (NutritionSection, NutritionAiMode?) -> Unit,
    onHouseholdSelected: (HouseholdContext) -> Unit,
    embeddedInTabs: Boolean = false,
) {
    if (household == null) {
        if (embeddedInTabs) {
            NutritionEntryAwaitingHousehold(
                onBack = onBack,
                showBackButton = false,
            )
            return
        }
        NutritionEntryGate(
            onBack = onBack,
            onReady = onHouseholdSelected,
            showBackButton = true,
        )
        return
    }

    val nutritionScreenModel = koinInject<NutritionScreenModel>()
    val showBack = !embeddedInTabs
    var aiSheetVisible by rememberSaveable { mutableStateOf(false) }
    var aiSheetLaunch by remember {
        mutableStateOf(AiHelperLaunchContext(mode = NutritionAiMode.MealPlan))
    }

    fun openAiSheet(context: AiHelperLaunchContext) {
        aiSheetLaunch = context
        aiSheetVisible = true
    }

    fun dismissAiSheet() {
        aiSheetVisible = false
        nutritionScreenModel.resetAiState()
    }

    @Composable
    fun AiHelperSheetHost() {
        AiHelperSheet(
            visible = aiSheetVisible,
            launchContext = aiSheetLaunch,
            onDismiss = ::dismissAiSheet,
            onApplied = ::dismissAiSheet,
            screenModel = nutritionScreenModel,
        )
    }

    when (section) {
        NutritionSection.Hub -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household.id)
            }
            NutritionHubScreen(
                householdName = household.name,
                enabledFeatures = household.nutritionFeatures,
                onBack = onBack,
                onOpenSection = { target -> onOpenSection(target, null) },
            )
        }

        NutritionSection.Grocery -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household.id)
            }
            GroceryShoppingScreen(
                onBack = onBack,
                embeddedInTabs = embeddedInTabs,
            )
        }

        NutritionSection.MealPlan -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household.id)
            }
            WeeklyMealPlanScreen(
                onBack = onBack,
                onOpenSection = { target, mode -> onOpenSection(target, mode) },
                onOpenAiSheet = ::openAiSheet,
                showBackButton = showBack,
            )
            AiHelperSheetHost()
        }

        NutritionSection.AiAdvice -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household.id)
            }
            // Fallback / deep-link only — default flows use AiHelperSheetHost on Plan and Groceries.
            NutritionAiAdviceScreen(
                onBack = onBack,
                initialMode = initialAiMode,
            )
        }
    }
}

@Composable
private fun NutritionEntryAwaitingHousehold(
    onBack: () -> Unit,
    showBackButton: Boolean,
) {
    NutritionScaffold(
        title = stringResource(Res.string.nutrition_hub_title),
        onBack = onBack,
        showBackButton = showBackButton,
    ) { padding ->
        JourneyLoadingContent(
            message = stringResource(Res.string.nutrition_entry_loading),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            containerTestTag = NutritionEntryTestTags.LOADING,
        )
    }
}
