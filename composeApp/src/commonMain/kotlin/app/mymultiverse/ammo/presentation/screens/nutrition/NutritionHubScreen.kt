package app.mymultiverse.ammo.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_grocery_description
import ammo.composeapp.generated.resources.nutrition_grocery_progress
import ammo.composeapp.generated.resources.nutrition_grocery_title
import ammo.composeapp.generated.resources.nutrition_hub_title
import ammo.composeapp.generated.resources.nutrition_meal_plan_description
import ammo.composeapp.generated.resources.nutrition_meal_plan_progress
import ammo.composeapp.generated.resources.nutrition_meal_plan_title
import ammo.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.ammo.domain.nutrition.NutritionHubSummary
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.components.FamilyLogisticCard
import app.mymultiverse.ammo.presentation.components.NutritionScaffold
import app.mymultiverse.ammo.presentation.components.NutritionSyncStatusBanner
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.WeekContextBanner
import app.mymultiverse.ammo.presentation.components.screenContentArea
import app.mymultiverse.ammo.presentation.components.screenListPadding
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import app.mymultiverse.ammo.presentation.navigation.NutritionSection
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
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
                        icon = AppIcons.GroceryList,
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
                        icon = AppIcons.MealPlan,
                        statusLine = mealPlanStatus,
                        modifier = Modifier.testTag(NutritionHubTestTags.MEAL_PLAN_CARD),
                        onClick = { onOpenSection(NutritionSection.MealPlan) },
                    )
                }
            }
        }
    }
}
