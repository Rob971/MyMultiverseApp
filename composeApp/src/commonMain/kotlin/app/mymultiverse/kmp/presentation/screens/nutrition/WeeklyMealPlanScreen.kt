package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import app.mymultiverse.kmp.presentation.components.JourneyIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.nutrition.MealPlanDayEntry
import app.mymultiverse.kmp.domain.nutrition.MealPlanDayOrdering
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.MealPlanPresentation
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.domain.nutrition.NutritionHubSummary
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.AiGrocerySuggestionsSection
import app.mymultiverse.kmp.presentation.components.PantryCheckSection
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.kmp.presentation.components.JourneySnackbarHost
import app.mymultiverse.kmp.presentation.components.JourneySecondaryButton
import app.mymultiverse.kmp.presentation.components.MealPlanDayCard
import app.mymultiverse.kmp.presentation.components.MealPlanEmptyAiChip
import app.mymultiverse.kmp.presentation.components.MealPlanEmptyState
import app.mymultiverse.kmp.presentation.components.MealPlanEmptyStateTestTags
import app.mymultiverse.kmp.presentation.components.MealPlanTestTags
import app.mymultiverse.kmp.presentation.components.NutritionFeatureHeader
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.nutritionDayLabel
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.navigation.NutritionSection
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_none
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_summary
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_suggestions_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_cancel_edit
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_clear_field
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_clear_week
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_clear_week_confirm_body
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_clear_week_confirm_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_copy_to_lunch
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_dinner
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_generate_all_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_generate_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_added
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_bulk_added
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_bulk_error
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_bulk_none
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_error
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_grocery_none_new
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_pantry_check_adopt_remaining
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_pantry_check_subtitle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_pantry_check_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_lunch
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_collapse_day
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_empty_body
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_empty_cta_ai
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_empty_cta_manual
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_empty_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_expand_day
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_not_planned
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_plan_with_ai
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_budget_plan
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_protein_plan
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_veggies
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_criteria_quick_meal
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_suggest_quick_ai
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_section_days
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_section_today
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_section_week
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_slot_unplanned
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_today
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_next
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_previous
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyMealPlanScreen(
    onBack: () -> Unit,
    onOpenSection: (NutritionSection, NutritionAiMode?) -> Unit,
    onOpenAiSheet: (AiHelperLaunchContext) -> Unit = {},
    showBackButton: Boolean = true,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val mealPlan by screenModel.mealPlan.collectAsState()
    val aiGrocery by screenModel.aiGroceryItems.collectAsState()
    val shoppingAi = remember(aiGrocery) { aiGrocery.filterNot { it.isPantryCheck } }
    val pantryCheckItems = remember(aiGrocery) { aiGrocery.filter { it.isPantryCheck } }
    val canWrite by screenModel.canWriteHouseholdData.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()
    val mealGroceryLoading by screenModel.mealGroceryLoading.collectAsState()
    val mealGroceryResult by screenModel.mealGroceryResult.collectAsState()
    val bulkGroceryLoading by screenModel.bulkMealGroceryLoading.collectAsState()
    val bulkGroceryResult by screenModel.bulkMealGroceryResult.collectAsState()
    val adoptAllResult by screenModel.adoptAllGroceryResult.collectAsState()
    val weekOffset by screenModel.weekOffset.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()
    var showClearWeekDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val todayIndex = remember(screenModel.weekKey) { WeekCalendar.todayIndexInWeek(screenModel.weekKey) }
    val orderedDays = remember(mealPlan, todayIndex) {
        MealPlanDayOrdering.orderDaysForDisplay(mealPlan.days, todayIndex)
    }
    val mealProgress = remember(mealPlan.days) { NutritionHubSummary.mealPlanProgress(mealPlan.days) }
    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )
    val previousWeekLabel = stringResource(Res.string.nutrition_week_previous)
    val nextWeekLabel = stringResource(Res.string.nutrition_week_next)

    val generateGroceryLabel = stringResource(Res.string.nutrition_meal_generate_grocery)
    val generateAllGroceryLabel = stringResource(Res.string.nutrition_meal_generate_all_grocery)
    val lunchLabel = stringResource(Res.string.nutrition_meal_lunch)
    val dinnerLabel = stringResource(Res.string.nutrition_meal_dinner)
    val suggestQuickMealLabel = stringResource(Res.string.nutrition_meal_suggest_quick_ai)
    val proteinPlanChipLabel = stringResource(Res.string.nutrition_ai_suggestion_protein_plan)
    val budgetPlanChipLabel = stringResource(Res.string.nutrition_ai_suggestion_budget_plan)
    val veggiePlanChipLabel = stringResource(Res.string.nutrition_ai_suggestion_veggies)
    val groceryNoneNew = stringResource(Res.string.nutrition_meal_grocery_none_new)
    val groceryError = stringResource(Res.string.nutrition_meal_grocery_error)
    val adoptAllSummary = adoptAllResult?.let { count ->
        if (count == 0) {
            stringResource(Res.string.nutrition_ai_adopt_all_grocery_none)
        } else {
            stringResource(Res.string.nutrition_ai_adopt_all_grocery_summary, count)
        }
    }

    val mealGrocerySnackbarMessage = mealGroceryResult?.let { result ->
        val slotLabel = when (result.slot) {
            MealSlot.Lunch -> lunchLabel
            MealSlot.Dinner -> dinnerLabel
        }
        when {
            result.isError -> groceryError
            result.itemCount == 0 -> groceryNoneNew
            else -> stringResource(
                Res.string.nutrition_meal_grocery_added,
                result.itemCount,
                result.dayLabel,
                slotLabel,
            )
        }
    }

    val bulkGrocerySnackbarMessage = bulkGroceryResult?.let { result ->
        when {
            result.isError && result.addedCount == 0 -> stringResource(Res.string.nutrition_meal_grocery_bulk_error)
            result.addedCount == 0 -> stringResource(Res.string.nutrition_meal_grocery_bulk_none)
            else -> stringResource(
                Res.string.nutrition_meal_grocery_bulk_added,
                result.addedCount,
                result.mealsProcessed,
            )
        }
    }

    LaunchedEffect(mealGrocerySnackbarMessage) {
        val message = mealGrocerySnackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeMealGroceryResult()
    }

    LaunchedEffect(bulkGrocerySnackbarMessage) {
        val message = bulkGrocerySnackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeBulkMealGroceryResult()
    }

    LaunchedEffect(adoptAllSummary) {
        val message = adoptAllSummary ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeAdoptAllGroceryResult()
    }

    val todayEntry = todayIndex?.let { index -> orderedDays.firstOrNull { it.index == index } }
    val upcomingEntries = if (todayIndex != null) {
        orderedDays.filter { it.index != todayIndex }
    } else {
        orderedDays
    }
    val daysSectionTitle = if (todayIndex != null) {
        stringResource(Res.string.nutrition_meal_plan_section_week)
    } else {
        stringResource(Res.string.nutrition_meal_plan_section_days)
    }
    val isEmptyWeek = mealProgress.plannedSlots == 0
    val hasPlannedMeals = mealProgress.plannedSlots > 0

    fun loadingSlotFor(dayIndex: Int): MealSlot? {
        val request = mealGroceryLoading ?: return null
        return if (request.dayIndex == dayIndex) request.slot else null
    }

    fun openAiMealPlan(initialCriteria: String = "") {
        onOpenAiSheet(
            AiHelperLaunchContext(
                mode = NutritionAiMode.MealPlan,
                initialCriteria = initialCriteria,
            ),
        )
    }

    fun openSuggestQuickMeal(dayIndex: Int, criteria: String, slot: MealSlot) {
        onOpenAiSheet(
            AiHelperLaunchContext(
                mode = NutritionAiMode.MealPlan,
                mealPlanScope = MealPlanGenerationScope.SingleDay(dayIndex),
                initialCriteria = criteria,
                targetMealSlot = slot,
                offerIngredientsAfterApply = true,
            ),
        )
    }

    fun scrollToDays() {
        scope.launch {
            val targetIndex = if (todayEntry != null) 4 else 3
            listState.animateScrollToItem(targetIndex.coerceAtMost(listState.layoutInfo.totalItemsCount - 1))
        }
    }

    @Composable
    fun dayCard(entry: MealPlanDayEntry, isToday: Boolean, initiallyExpanded: Boolean) {
        val entryDayLabel = nutritionDayLabel(entry.index)
        val quickLunchCriteria = stringResource(
            Res.string.nutrition_ai_criteria_quick_meal,
            lunchLabel,
        )
        val quickDinnerCriteria = stringResource(
            Res.string.nutrition_ai_criteria_quick_meal,
            dinnerLabel,
        )
        val canCopyToTomorrow = MealPlanPresentation.tomorrowIndex(entry.index) != null &&
            entry.day.dinner.isNotBlank()
        MealPlanDayCard(
            dayIndex = entry.index,
            dayLabel = entryDayLabel,
            day = entry.day,
            weekDays = mealPlan.days,
            isToday = isToday,
            todayLabel = stringResource(Res.string.nutrition_today),
            lunchLabel = lunchLabel,
            dinnerLabel = dinnerLabel,
            notPlannedLabel = stringResource(Res.string.nutrition_meal_plan_not_planned),
            unplannedSlotLabel = stringResource(Res.string.nutrition_meal_slot_unplanned),
            expandDayLabel = stringResource(Res.string.nutrition_meal_plan_expand_day),
            collapseDayLabel = stringResource(Res.string.nutrition_meal_plan_collapse_day),
            generateGroceryLabel = generateGroceryLabel,
            copyToTomorrowLabel = stringResource(Res.string.nutrition_meal_copy_to_lunch),
            clearFieldLabel = stringResource(Res.string.nutrition_meal_clear_field),
            onLunchChange = { lunch -> screenModel.updateMeal(entry.index, lunch = lunch) },
            onDinnerChange = { dinner -> screenModel.updateMeal(entry.index, dinner = dinner) },
            onGenerateGroceryForMeal = { slot ->
                screenModel.generateGroceryForMeal(
                    dayIndex = entry.index,
                    slot = slot,
                    dayLabel = entryDayLabel,
                )
            },
            onCopyToTomorrowLunch = if (canCopyToTomorrow && canWrite) {
                { screenModel.copyDinnerToTomorrowLunch(entry.index) }
            } else {
                null
            },
            onClearLunch = if (canWrite) {
                { screenModel.clearMealSlot(entry.index, MealSlot.Lunch) }
            } else {
                null
            },
            onClearDinner = if (canWrite) {
                { screenModel.clearMealSlot(entry.index, MealSlot.Dinner) }
            } else {
                null
            },
            loadingMeal = loadingSlotFor(entry.index),
            readOnly = !canWrite,
            initiallyExpanded = initiallyExpanded,
            suggestQuickMealLabel = if (canWrite) suggestQuickMealLabel else null,
            onSuggestQuickMeal = if (canWrite) {
                { slot ->
                    val criteria = when (slot) {
                        MealSlot.Lunch -> quickLunchCriteria
                        MealSlot.Dinner -> quickDinnerCriteria
                    }
                    openSuggestQuickMeal(entry.index, criteria, slot)
                }
            } else {
                null
            },
        )
    }

    if (showClearWeekDialog) {
        AlertDialog(
            onDismissRequest = { showClearWeekDialog = false },
            title = { Text(stringResource(Res.string.nutrition_meal_clear_week_confirm_title)) },
            text = { Text(stringResource(Res.string.nutrition_meal_clear_week_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        screenModel.clearMealPlanWeek()
                        showClearWeekDialog = false
                    },
                ) {
                    Text(stringResource(Res.string.nutrition_meal_clear_week))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearWeekDialog = false }) {
                    Text(stringResource(Res.string.nutrition_grocery_cancel_edit))
                }
            },
        )
    }

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_meal_plan_title),
        onBack = onBack,
        showBackButton = showBackButton,
        snackbarHost = { JourneySnackbarHost(hostState = snackbarHostState) },
        actions = {
            if (canWrite) {
                JourneyIconButton(onClick = { showOverflowMenu = true }) {
                    Icon(
                        imageVector = AppIcons.MoreVert,
                        contentDescription = stringResource(Res.string.nutrition_meal_clear_week),
                    )
                }
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.nutrition_meal_clear_week)) },
                        onClick = {
                            showOverflowMenu = false
                            showClearWeekDialog = true
                        },
                        modifier = Modifier.testTag(MealPlanTestTags.CLEAR_WEEK),
                    )
                }
            }
        },
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.screenContentArea(padding)) {
            val isWideLayout = maxWidth >= ScreenLayout.expandedMinWidth

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = screenModel::refresh,
            state = pullRefreshState,
            modifier = Modifier.fillMaxWidth(),
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(MealPlanTestTags.SCROLL_LIST),
            state = listState,
            contentPadding = screenListPadding(),
            verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
        ) {
            item {
                NutritionFeatureHeader(
                    weekLabel = weekLabel,
                    description = stringResource(Res.string.nutrition_meal_plan_description),
                    icon = AppIcons.DateRange,
                    accentColor = JourneySemanticColors.brandTerracotta(),
                    progressLabel = stringResource(
                        Res.string.nutrition_meal_plan_progress,
                        mealProgress.plannedSlots,
                        mealProgress.totalSlots,
                    ),
                    progress = mealProgress.plannedSlots.toFloat() / mealProgress.totalSlots,
                    canGoToPreviousWeek = screenModel.canGoToPreviousWeek,
                    canGoToNextWeek = screenModel.canGoToNextWeek,
                    previousWeekLabel = previousWeekLabel,
                    nextWeekLabel = nextWeekLabel,
                    onPreviousWeek = { screenModel.selectWeekOffset(weekOffset - 1) },
                    onNextWeek = { screenModel.selectWeekOffset(weekOffset + 1) },
                    planWithAiLabel = stringResource(Res.string.nutrition_meal_plan_plan_with_ai),
                    onPlanWithAi = ::openAiMealPlan,
                    showPlanWithAi = canWrite && mealProgress.plannedSlots < mealProgress.totalSlots / 2,
                    planWithAiTestTag = MealPlanTestTags.PLAN_WITH_AI,
                )
            }

            if (!canWrite) {
                item {
                    HouseholdViewerReadOnlyNotice()
                }
            }

            if (isEmptyWeek) {
                item {
                    MealPlanEmptyState(
                        title = stringResource(Res.string.nutrition_meal_plan_empty_title),
                        body = stringResource(Res.string.nutrition_meal_plan_empty_body),
                        planWithAiLabel = stringResource(Res.string.nutrition_meal_plan_empty_cta_ai),
                        addManuallyLabel = stringResource(Res.string.nutrition_meal_plan_empty_cta_manual),
                        onPlanWithAi = { openAiMealPlan() },
                        onAddManually = ::scrollToDays,
                        aiChips = listOf(
                            MealPlanEmptyAiChip(
                                label = proteinPlanChipLabel,
                                testTag = MealPlanEmptyStateTestTags.CHIP_PROTEIN,
                                onClick = { openAiMealPlan(proteinPlanChipLabel) },
                            ),
                            MealPlanEmptyAiChip(
                                label = budgetPlanChipLabel,
                                testTag = MealPlanEmptyStateTestTags.CHIP_BUDGET,
                                onClick = { openAiMealPlan(budgetPlanChipLabel) },
                            ),
                            MealPlanEmptyAiChip(
                                label = veggiePlanChipLabel,
                                testTag = MealPlanEmptyStateTestTags.CHIP_VEGGIES,
                                onClick = { openAiMealPlan(veggiePlanChipLabel) },
                            ),
                        ),
                    )
                }
            }

            if (hasPlannedMeals && canWrite) {
                item {
                    JourneySecondaryButton(
                        onClick = { screenModel.generateGroceryForAllPlannedMeals() },
                        enabled = !bulkGroceryLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(MealPlanTestTags.GENERATE_ALL_GROCERY),
                    ) {
                        if (bulkGroceryLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                        Text(generateAllGroceryLabel)
                    }
                }
            }

            if (pantryCheckItems.isNotEmpty()) {
                item(key = "pantry-check") {
                    PantryCheckSection(
                        title = stringResource(Res.string.nutrition_pantry_check_title),
                        subtitle = stringResource(Res.string.nutrition_pantry_check_subtitle),
                        items = pantryCheckItems,
                        onMarkHave = { item -> screenModel.dismissPantryCheckItem(item.id) },
                        adoptRemainingLabel = stringResource(Res.string.nutrition_pantry_check_adopt_remaining),
                        onAdoptRemaining = { screenModel.adoptRemainingPantryItems() },
                        enabled = canWrite,
                    )
                }
            }

            if (shoppingAi.isNotEmpty()) {
                item(key = "ai-suggestions") {
                    AiGrocerySuggestionsSection(
                        title = stringResource(Res.string.nutrition_ai_grocery_suggestions_title),
                        items = shoppingAi,
                        adoptAllLabel = stringResource(Res.string.nutrition_ai_adopt_all_grocery),
                        onAdoptAll = { screenModel.adoptAllAiGrocerySuggestions() },
                        onAdopt = { suggestion -> screenModel.adoptAiGrocerySuggestion(suggestion.id) },
                        enabled = canWrite,
                    )
                }
            }

            if (isWideLayout && todayEntry != null && upcomingEntries.isNotEmpty()) {
                item(key = "wide-days") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ScreenLayout.horizontalPadding),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
                        ) {
                            FamilyLogisticsSectionHeader(
                                title = stringResource(Res.string.nutrition_meal_plan_section_today),
                            )
                            dayCard(todayEntry, isToday = true, initiallyExpanded = true)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
                        ) {
                            FamilyLogisticsSectionHeader(title = daysSectionTitle)
                            upcomingEntries.forEach { entry ->
                                dayCard(entry, isToday = false, initiallyExpanded = false)
                            }
                        }
                    }
                }
            } else {
                if (todayEntry != null) {
                    item {
                        FamilyLogisticsSectionHeader(
                            title = stringResource(Res.string.nutrition_meal_plan_section_today),
                        )
                    }
                    item {
                        dayCard(todayEntry, isToday = true, initiallyExpanded = true)
                    }
                }

                if (upcomingEntries.isNotEmpty()) {
                    item {
                        FamilyLogisticsSectionHeader(title = daysSectionTitle)
                    }
                    items(upcomingEntries, key = { it.index }) { entry ->
                        dayCard(entry, isToday = false, initiallyExpanded = false)
                    }
                }
            }
        }
        }
        }
    }
}
