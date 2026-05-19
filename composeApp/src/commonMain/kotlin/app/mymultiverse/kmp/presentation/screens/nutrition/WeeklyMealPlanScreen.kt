package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_friday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_monday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_saturday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_sunday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_thursday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_tuesday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_day_wednesday
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_dinner
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_lunch
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@Composable
fun WeeklyMealPlanScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val mealPlan by screenModel.mealPlan.collectAsState()
    val dayLabels = weekDayLabels()

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_meal_plan_title),
        onBack = onBack,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.nutrition_week_label, screenModel.weekKey),
                    style = MaterialTheme.typography.labelLarge,
                    color = SharedJourneyColors.MediterraneanTeal,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            itemsIndexed(mealPlan.days) { index, day ->
                DayMealCard(
                    dayLabel = stringResource(dayLabels[index]),
                    day = day,
                    onLunchChange = { lunch -> screenModel.updateMeal(index, lunch = lunch) },
                    onDinnerChange = { dinner -> screenModel.updateMeal(index, dinner = dinner) },
                )
            }
        }
    }
}

@Composable
private fun DayMealCard(
    dayLabel: String,
    day: DayMeals,
    onLunchChange: (String) -> Unit,
    onDinnerChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SharedJourneyColors.GlassWhite,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SharedJourneyColors.InkDeep,
            )
            OutlinedTextField(
                value = day.lunch,
                onValueChange = onLunchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.nutrition_meal_lunch)) },
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
            )
            OutlinedTextField(
                value = day.dinner,
                onValueChange = onDinnerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.nutrition_meal_dinner)) },
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
            )
        }
    }
}

@Composable
private fun weekDayLabels(): List<StringResource> = listOf(
    Res.string.nutrition_day_monday,
    Res.string.nutrition_day_tuesday,
    Res.string.nutrition_day_wednesday,
    Res.string.nutrition_day_thursday,
    Res.string.nutrition_day_friday,
    Res.string.nutrition_day_saturday,
    Res.string.nutrition_day_sunday,
)
