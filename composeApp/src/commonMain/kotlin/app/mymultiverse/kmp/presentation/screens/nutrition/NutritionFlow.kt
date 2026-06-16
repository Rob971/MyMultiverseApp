package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.navigation.NutritionSpaceContext
import app.mymultiverse.kmp.presentation.screens.nutrition.spaces.NutritionSpaceMembersScreen
import org.koin.compose.koinInject

@Composable
fun NutritionFlow(
    space: NutritionSpaceContext?,
    section: NutritionSection,
    onBack: () -> Unit,
    onOpenSection: (NutritionSection) -> Unit,
    onSpaceSelected: (NutritionSpaceContext) -> Unit,
) {
    if (space == null) {
        NutritionEntryGate(
            onBack = onBack,
            onReady = onSpaceSelected,
        )
        return
    }

    val nutritionScreenModel = koinInject<NutritionScreenModel>()

    when (section) {
        NutritionSection.Members -> {
            NutritionSpaceMembersScreen(
                space = space,
                onBack = onBack,
            )
        }

        NutritionSection.Hub -> {
            LaunchedEffect(space.id) {
                nutritionScreenModel.activateSpace(space.id)
            }
            NutritionHubScreen(
                spaceName = space.name,
                enabledFeatures = space.features,
                onBack = onBack,
                onOpenSection = onOpenSection,
            )
        }

        NutritionSection.Grocery -> {
            LaunchedEffect(space.id) {
                nutritionScreenModel.activateSpace(space.id)
            }
            GroceryShoppingScreen(onBack = onBack)
        }

        NutritionSection.MealPlan -> {
            LaunchedEffect(space.id) {
                nutritionScreenModel.activateSpace(space.id)
            }
            WeeklyMealPlanScreen(onBack = onBack)
        }

        NutritionSection.AiAdvice -> {
            LaunchedEffect(space.id) {
                nutritionScreenModel.activateSpace(space.id)
            }
            NutritionAiAdviceScreen(onBack = onBack)
        }
    }
}
