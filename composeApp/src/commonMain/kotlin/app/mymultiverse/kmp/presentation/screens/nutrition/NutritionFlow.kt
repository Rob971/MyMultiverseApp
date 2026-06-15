package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.navigation.NutritionSpaceContext
import app.mymultiverse.kmp.presentation.screens.nutrition.spaces.NutritionSpaceMembersScreen
import app.mymultiverse.kmp.presentation.screens.nutrition.spaces.NutritionSpacesScreen
import org.koin.compose.koinInject

@Composable
fun NutritionFlow(
    space: NutritionSpaceContext?,
    section: NutritionSection,
    onBack: () -> Unit,
    onOpenSection: (NutritionSection) -> Unit,
    onSpaceSelected: (NutritionSpaceContext) -> Unit,
) {
    val nutritionScreenModel = koinInject<NutritionScreenModel>()

    when (section) {
        NutritionSection.Spaces -> NutritionSpacesScreen(
            onBack = onBack,
            onSpaceSelected = onSpaceSelected,
        )

        NutritionSection.Members -> {
            val activeSpace = requireNotNull(space) { "Members screen requires an active sharing space" }
            NutritionSpaceMembersScreen(
                space = activeSpace,
                onBack = onBack,
            )
        }

        NutritionSection.Hub -> {
            val activeSpace = requireNotNull(space) { "Nutrition hub requires an active sharing space" }
            LaunchedEffect(activeSpace.id) {
                nutritionScreenModel.activateSpace(activeSpace.id)
            }
            NutritionHubScreen(
                spaceName = activeSpace.name,
                enabledFeatures = activeSpace.features,
                onBack = onBack,
                onOpenSection = onOpenSection,
            )
        }

        NutritionSection.Grocery -> GroceryShoppingScreen(onBack = onBack)
        NutritionSection.MealPlan -> WeeklyMealPlanScreen(onBack = onBack)
        NutritionSection.AiAdvice -> NutritionAiAdviceScreen(onBack = onBack)
    }
}
