package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_readonly_note
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_result_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_dinner
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_generate_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_added
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_error
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_none_new
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_lunch
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_collapse_day
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_expand_day
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_not_planned
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_section_today
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_section_week
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_today
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.MealPlanDayOrdering
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.AiReadOnlyGroceryList
import app.mymultiverse.kmp.presentation.components.nutritionDayLabel
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.MealPlanDayCard
import app.mymultiverse.kmp.presentation.components.NutritionFeatureHeader
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@Composable
fun WeeklyMealPlanScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val mealPlan by screenModel.mealPlan.collectAsState()
    val aiGrocery by screenModel.aiGroceryItems.collectAsState()
    val mealGroceryLoading by screenModel.mealGroceryLoading.collectAsState()
    val mealGroceryResult by screenModel.mealGroceryResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val todayIndex = remember(screenModel.weekKey) { WeekCalendar.todayIndexInWeek(screenModel.weekKey) }
    val orderedDays = remember(mealPlan, todayIndex) {
        MealPlanDayOrdering.orderDaysForDisplay(mealPlan.days, todayIndex)
    }
    val plannedDays = NutritionHubSummary.plannedDaysCount(mealPlan.days)
    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )

    val generateGroceryLabel = stringResource(Res.string.nutrition_meal_generate_grocery)
    val lunchLabel = stringResource(Res.string.nutrition_meal_lunch)
    val dinnerLabel = stringResource(Res.string.nutrition_meal_dinner)
    val groceryAddedFormat = stringResource(Res.string.nutrition_meal_grocery_added)
    val groceryNoneNew = stringResource(Res.string.nutrition_meal_grocery_none_new)
    val groceryError = stringResource(Res.string.nutrition_meal_grocery_error)

    LaunchedEffect(mealGroceryResult) {
        val result = mealGroceryResult ?: return@LaunchedEffect
        val slotLabel = when (result.slot) {
            MealSlot.Lunch -> lunchLabel
            MealSlot.Dinner -> dinnerLabel
        }
        val message = when {
            result.isError -> groceryError
            result.itemCount == 0 -> groceryNoneNew
            else -> groceryAddedFormat
                .replace("%1\$d", result.itemCount.toString())
                .replace("%2\$s", result.dayLabel)
                .replace("%3\$s", slotLabel)
        }
        snackbarHostState.showSnackbar(message)
        screenModel.consumeMealGroceryResult()
    }

    val todayEntry = todayIndex?.let { index -> orderedDays.firstOrNull { it.index == index } }
    val upcomingEntries = if (todayIndex != null) {
        orderedDays.filter { it.index != todayIndex }
    } else {
        orderedDays
    }

    fun loadingSlotFor(dayIndex: Int): MealSlot? {
        val request = mealGroceryLoading ?: return null
        return if (request.dayIndex == dayIndex) request.slot else null
    }

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_meal_plan_title),
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.screenContentArea(padding),
            contentPadding = screenListPadding(),
            verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
        ) {
            item {
                NutritionFeatureHeader(
                    weekLabel = weekLabel,
                    description = stringResource(Res.string.nutrition_meal_plan_description),
                    icon = AppIcons.DateRange,
                    accentColor = SharedJourneyColors.TerracottaOrange,
                    progressLabel = stringResource(
                        Res.string.nutrition_meal_plan_progress,
                        plannedDays,
                        WeeklyMealPlan.DAYS_IN_WEEK,
                    ),
                    progress = plannedDays.toFloat() / WeeklyMealPlan.DAYS_IN_WEEK,
                )
            }

            if (aiGrocery.isNotEmpty()) {
                item {
                    AiReadOnlyGroceryList(
                        items = aiGrocery,
                        title = stringResource(Res.string.nutrition_ai_grocery_result_title),
                        subtitle = stringResource(Res.string.nutrition_ai_grocery_readonly_note),
                    )
                }
            }

            if (todayEntry != null) {
                item {
                    FamilyLogisticsSectionHeader(
                        title = stringResource(Res.string.nutrition_meal_plan_section_today),
                    )
                }
                item {
                    val todayDayLabel = nutritionDayLabel(todayEntry.index)
                    MealPlanDayCard(
                        dayIndex = todayEntry.index,
                        dayLabel = todayDayLabel,
                        day = todayEntry.day,
                        isToday = true,
                        todayLabel = stringResource(Res.string.nutrition_today),
                        lunchLabel = lunchLabel,
                        dinnerLabel = dinnerLabel,
                        notPlannedLabel = stringResource(Res.string.nutrition_meal_plan_not_planned),
                        expandDayLabel = stringResource(Res.string.nutrition_meal_plan_expand_day),
                        collapseDayLabel = stringResource(Res.string.nutrition_meal_plan_collapse_day),
                        generateGroceryLabel = generateGroceryLabel,
                        onLunchChange = { lunch ->
                            screenModel.updateMeal(todayEntry.index, lunch = lunch)
                        },
                        onDinnerChange = { dinner ->
                            screenModel.updateMeal(todayEntry.index, dinner = dinner)
                        },
                        onGenerateGroceryForMeal = { slot ->
                            screenModel.generateGroceryForMeal(
                                dayIndex = todayEntry.index,
                                slot = slot,
                                dayLabel = todayDayLabel,
                            )
                        },
                        loadingMeal = loadingSlotFor(todayEntry.index),
                        initiallyExpanded = true,
                    )
                }
            }

            if (upcomingEntries.isNotEmpty()) {
                item {
                    FamilyLogisticsSectionHeader(
                        title = stringResource(Res.string.nutrition_meal_plan_section_week),
                    )
                }
                items(upcomingEntries, key = { it.index }) { entry ->
                    val entryDayLabel = nutritionDayLabel(entry.index)
                    MealPlanDayCard(
                        dayIndex = entry.index,
                        dayLabel = entryDayLabel,
                        day = entry.day,
                        isToday = false,
                        todayLabel = stringResource(Res.string.nutrition_today),
                        lunchLabel = lunchLabel,
                        dinnerLabel = dinnerLabel,
                        notPlannedLabel = stringResource(Res.string.nutrition_meal_plan_not_planned),
                        expandDayLabel = stringResource(Res.string.nutrition_meal_plan_expand_day),
                        collapseDayLabel = stringResource(Res.string.nutrition_meal_plan_collapse_day),
                        generateGroceryLabel = generateGroceryLabel,
                        onLunchChange = { lunch -> screenModel.updateMeal(entry.index, lunch = lunch) },
                        onDinnerChange = { dinner -> screenModel.updateMeal(entry.index, dinner = dinner) },
                        onGenerateGroceryForMeal = { slot ->
                            screenModel.generateGroceryForMeal(
                                dayIndex = entry.index,
                                slot = slot,
                                dayLabel = entryDayLabel,
                            )
                        },
                        loadingMeal = loadingSlotFor(entry.index),
                        initiallyExpanded = false,
                    )
                }
            }
        }
    }
}
