package app.mymultiverse.ammo.presentation.screens.nutrition

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
import app.mymultiverse.ammo.presentation.components.JourneyLoadingContent
import app.mymultiverse.ammo.presentation.components.NutritionScaffold
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext
import app.mymultiverse.ammo.presentation.navigation.NutritionSection
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_entry_loading
import ammo.composeapp.generated.resources.nutrition_hub_title
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
    modifier: Modifier = Modifier,
) {
    if (household == null) {
        if (embeddedInTabs) {
            NutritionEntryAwaitingHousehold(
                onBack = onBack,
                showBackButton = false,
                modifier = modifier,
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
                nutritionScreenModel.activateHousehold(household)
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
                nutritionScreenModel.activateHousehold(household)
            }
            GroceryShoppingScreen(
                onBack = onBack,
                embeddedInTabs = embeddedInTabs,
                modifier = modifier,
            )
        }

        NutritionSection.MealPlan -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household)
            }
            WeeklyMealPlanScreen(
                onBack = onBack,
                onOpenSection = { target, mode -> onOpenSection(target, mode) },
                onOpenAiSheet = ::openAiSheet,
                showBackButton = showBack,
                embeddedInTabs = embeddedInTabs,
                modifier = modifier,
            )
            AiHelperSheetHost()
        }

        NutritionSection.AiAdvice -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household)
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
    modifier: Modifier = Modifier,
) {
    NutritionScaffold(
        title = stringResource(Res.string.nutrition_hub_title),
        onBack = onBack,
        showBackButton = showBackButton,
        modifier = modifier.fillMaxSize(),
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
