package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_hub_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_hub_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.FamilyLogisticCard
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.WeekContextBanner
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@Composable
fun NutritionHubScreen(
    onBack: () -> Unit,
    onOpenSection: (NutritionSection) -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val grocery by screenModel.groceryItems.collectAsState()
    val mealPlan by screenModel.mealPlan.collectAsState()
    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )

    val groceryStatus = if (grocery.isEmpty()) {
        null
    } else {
        stringResource(
            Res.string.nutrition_grocery_progress,
            grocery.count { it.isChecked },
            grocery.size,
        )
    }

    val plannedDays = mealPlan.days.count { day ->
        day.lunch.isNotBlank() || day.dinner.isNotBlank()
    }
    val mealPlanStatus = stringResource(
        Res.string.nutrition_meal_plan_progress,
        plannedDays,
        WeeklyMealPlan.DAYS_IN_WEEK,
    )

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_hub_title),
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                WeekContextBanner(weekLabel = weekLabel)
            }

            item {
                Text(
                    text = stringResource(Res.string.nutrition_hub_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SharedJourneyColors.InkMuted,
                    fontWeight = FontWeight.Medium,
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.nutrition_grocery_title),
                    description = stringResource(Res.string.nutrition_grocery_description),
                    accentColor = SharedJourneyColors.SageSoft,
                    icon = AppIcons.Restaurant,
                    statusLine = groceryStatus,
                    onClick = { onOpenSection(NutritionSection.Grocery) },
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.nutrition_meal_plan_title),
                    description = stringResource(Res.string.nutrition_meal_plan_description),
                    accentColor = SharedJourneyColors.TerracottaOrange,
                    icon = AppIcons.DateRange,
                    statusLine = mealPlanStatus,
                    onClick = { onOpenSection(NutritionSection.MealPlan) },
                )
            }

            item {
                FamilyLogisticCard(
                    title = stringResource(Res.string.nutrition_ai_title),
                    description = stringResource(Res.string.nutrition_ai_description),
                    accentColor = SharedJourneyColors.MediterraneanTeal,
                    icon = AppIcons.Sparkles,
                    onClick = { onOpenSection(NutritionSection.AiAdvice) },
                )
            }
        }
    }
}
