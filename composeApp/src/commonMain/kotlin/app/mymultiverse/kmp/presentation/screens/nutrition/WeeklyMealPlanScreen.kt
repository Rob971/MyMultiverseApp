package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_today
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.NutritionProgressChip
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.TodayBadge
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@Composable
fun WeeklyMealPlanScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val mealPlan by screenModel.mealPlan.collectAsState()
    val dayLabels = weekDayLabels()
    val todayIndex = remember(screenModel.weekKey) { WeekCalendar.todayIndexInWeek(screenModel.weekKey) }
    val orderedDays = remember(mealPlan, todayIndex) {
        buildOrderedDayEntries(mealPlan.days, todayIndex)
    }
    val plannedDays = mealPlan.days.count { it.lunch.isNotBlank() || it.dinner.isNotBlank() }
    val weekSubtitle = WeekCalendar.formatWeekRange(screenModel.weekKey)

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_meal_plan_title),
        subtitle = weekSubtitle,
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
                NutritionProgressChip(
                    label = stringResource(
                        Res.string.nutrition_meal_plan_progress,
                        plannedDays,
                        WeeklyMealPlan.DAYS_IN_WEEK,
                    ),
                    progress = plannedDays.toFloat() / WeeklyMealPlan.DAYS_IN_WEEK,
                    accentColor = SharedJourneyColors.TerracottaOrange,
                )
            }

            items(orderedDays, key = { it.index }) { entry ->
                DayMealCard(
                    dayLabel = stringResource(dayLabels[entry.index]),
                    day = entry.day,
                    isToday = entry.index == todayIndex,
                    todayLabel = stringResource(Res.string.nutrition_today),
                    onLunchChange = { lunch -> screenModel.updateMeal(entry.index, lunch = lunch) },
                    onDinnerChange = { dinner -> screenModel.updateMeal(entry.index, dinner = dinner) },
                )
            }
        }
    }
}

private data class DayEntry(val index: Int, val day: DayMeals)

private fun buildOrderedDayEntries(
    days: List<DayMeals>,
    todayIndex: Int?,
): List<DayEntry> {
    val entries = days.mapIndexed { index, day -> DayEntry(index, day) }
    if (todayIndex == null) return entries
    val today = entries[todayIndex]
    return listOf(today) + entries.filterNot { it.index == todayIndex }
}

@Composable
private fun DayMealCard(
    dayLabel: String,
    day: DayMeals,
    isToday: Boolean,
    todayLabel: String,
    onLunchChange: (String) -> Unit,
    onDinnerChange: (String) -> Unit,
) {
    val border = if (isToday) {
        BorderStroke(2.dp, SharedJourneyColors.TerracottaOrange.copy(alpha = 0.6f))
    } else {
        null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SharedJourneyColors.GlassWhite,
        border = border,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SharedJourneyColors.InkDeep,
                )
                if (isToday) {
                    TodayBadge(label = todayLabel)
                }
            }
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
