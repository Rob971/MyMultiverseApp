package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.presentation.navigation.HouseholdContext
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import org.koin.compose.koinInject

@Composable
fun NutritionFlow(
    household: HouseholdContext?,
    section: NutritionSection,
    initialAiMode: NutritionAiMode? = null,
    onBack: () -> Unit,
    onOpenSection: (NutritionSection, NutritionAiMode?) -> Unit,
    onHouseholdSelected: (HouseholdContext) -> Unit,
) {
    if (household == null) {
        NutritionEntryGate(
            onBack = onBack,
            onReady = onHouseholdSelected,
        )
        return
    }

    val nutritionScreenModel = koinInject<NutritionScreenModel>()

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
            GroceryShoppingScreen(onBack = onBack)
        }

        NutritionSection.MealPlan -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household.id)
            }
            WeeklyMealPlanScreen(
                onBack = onBack,
                onOpenSection = { target, mode -> onOpenSection(target, mode) },
            )
        }

        NutritionSection.AiAdvice -> {
            LaunchedEffect(household.id) {
                nutritionScreenModel.activateHousehold(household.id)
            }
            NutritionAiAdviceScreen(
                onBack = onBack,
                initialMode = initialAiMode,
            )
        }
    }
}
