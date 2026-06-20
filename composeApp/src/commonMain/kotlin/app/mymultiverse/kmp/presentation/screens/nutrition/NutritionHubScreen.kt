package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_hub_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.NutritionSyncStatusBanner
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.WeekContextBanner
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

object NutritionHubTestTags {
    const val GROCERY_CARD = "nutrition_hub_grocery"
    const val MEAL_PLAN_CARD = "nutrition_hub_meal_plan"
}

@Composable
fun NutritionHubScreen(
    householdName: String,
    enabledFeatures: Set<NutritionSharingFeature>,
    onBack: () -> Unit,
    onOpenSection: (NutritionSection) -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val grocery by screenModel.groceryItems.collectAsState()
    val mealPlan by screenModel.mealPlan.collectAsState()
    val syncStatus by screenModel.syncStatus.collectAsState()
    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )

    val groceryProgress = NutritionHubSummary.groceryProgress(grocery)
    val groceryStatus = groceryProgress?.let { progress ->
        stringResource(
            Res.string.nutrition_grocery_progress,
            progress.checked,
            progress.total,
        )
    }

    val plannedMealProgress = NutritionHubSummary.mealPlanProgress(mealPlan.days)
    val mealPlanStatus = stringResource(
        Res.string.nutrition_meal_plan_progress,
        plannedMealProgress.plannedSlots,
        plannedMealProgress.totalSlots,
    )

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_hub_title),
        subtitle = householdName,
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.screenContentArea(padding),
            contentPadding = screenListPadding(),
            verticalArrangement = Arrangement.spacedBy(ScreenLayout.sectionSpacing),
        ) {
            item {
                WeekContextBanner(weekLabel = weekLabel)
            }

            if (syncStatus != NutritionSyncStatus.Idle) {
                item {
                    NutritionSyncStatusBanner(status = syncStatus)
                }
            }

            if (NutritionSharingFeature.Grocery in enabledFeatures) {
                item {
                    FamilyLogisticCard(
                        title = stringResource(Res.string.nutrition_grocery_title),
                        description = stringResource(Res.string.nutrition_grocery_description),
                        accentColor = SharedJourneyColors.SageSoft,
                        icon = AppIcons.ShoppingCart,
                        statusLine = groceryStatus,
                        modifier = Modifier.testTag(NutritionHubTestTags.GROCERY_CARD),
                        onClick = { onOpenSection(NutritionSection.Grocery) },
                    )
                }
            }

            if (NutritionSharingFeature.MealPlan in enabledFeatures) {
                item {
                    FamilyLogisticCard(
                        title = stringResource(Res.string.nutrition_meal_plan_title),
                        description = stringResource(Res.string.nutrition_meal_plan_description),
                        accentColor = SharedJourneyColors.TerracottaOrange,
                        icon = AppIcons.DateRange,
                        statusLine = mealPlanStatus,
                        modifier = Modifier.testTag(NutritionHubTestTags.MEAL_PLAN_CARD),
                        onClick = { onOpenSection(NutritionSection.MealPlan) },
                    )
                }
            }
        }
    }
}
