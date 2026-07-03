package app.mymultiverse.ammo.presentation.screens.nutrition

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import app.mymultiverse.ammo.presentation.components.JourneyIcon
import app.mymultiverse.ammo.presentation.components.JourneyIconButton
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
import androidx.compose.runtime.key
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.nutrition.MealPlanDayEntry
import app.mymultiverse.ammo.domain.nutrition.MealPlanDayOrdering
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealPlanPresentation
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode
import app.mymultiverse.ammo.domain.nutrition.NutritionHubSummary
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.ammo.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.ammo.presentation.components.JourneyEmptyState
import app.mymultiverse.ammo.presentation.components.NutritionFeatureKind
import app.mymultiverse.ammo.presentation.components.JourneySnackbarHost
import app.mymultiverse.ammo.presentation.components.MealPlanDayCard
import app.mymultiverse.ammo.presentation.components.MealPlanEmptyStateTestTags
import app.mymultiverse.ammo.presentation.components.MealPlanPartnerNudgeTestTags
import app.mymultiverse.ammo.presentation.components.PartnerNudgeCard
import app.mymultiverse.ammo.presentation.components.NutritionProgressChip
import app.mymultiverse.ammo.presentation.components.NutritionScaffold
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.WeekSelectorBanner
import app.mymultiverse.ammo.presentation.components.nutritionDayLabel
import app.mymultiverse.ammo.presentation.components.screenContentArea
import app.mymultiverse.ammo.presentation.components.screenListPadding
import app.mymultiverse.ammo.presentation.navigation.NutritionSection
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.components.MealPlanTestTags
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_grocery_cancel_edit
import ammo.composeapp.generated.resources.nutrition_meal_clear_field
import ammo.composeapp.generated.resources.nutrition_meal_clear_week
import ammo.composeapp.generated.resources.nutrition_meal_clear_week_confirm_body
import ammo.composeapp.generated.resources.nutrition_meal_clear_week_confirm_title
import ammo.composeapp.generated.resources.nutrition_meal_copy_to_lunch
import ammo.composeapp.generated.resources.nutrition_meal_dinner
import ammo.composeapp.generated.resources.nutrition_meal_generate_grocery
import ammo.composeapp.generated.resources.nutrition_meal_grocery_added
import ammo.composeapp.generated.resources.nutrition_meal_grocery_error
import ammo.composeapp.generated.resources.nutrition_meal_grocery_none_new
import ammo.composeapp.generated.resources.nutrition_meal_lunch
import ammo.composeapp.generated.resources.nutrition_meal_plan_collapse_day
import ammo.composeapp.generated.resources.nutrition_meal_plan_daily_hint
import ammo.composeapp.generated.resources.nutrition_meal_plan_empty_body
import ammo.composeapp.generated.resources.nutrition_meal_plan_empty_cta_manual
import ammo.composeapp.generated.resources.nutrition_meal_plan_empty_title
import ammo.composeapp.generated.resources.nutrition_meal_plan_expand_day
import ammo.composeapp.generated.resources.nutrition_meal_plan_not_planned
import ammo.composeapp.generated.resources.nutrition_meal_plan_partner_nudge_action
import ammo.composeapp.generated.resources.nutrition_meal_plan_partner_nudge_body
import ammo.composeapp.generated.resources.nutrition_meal_plan_partner_nudge_cooldown
import ammo.composeapp.generated.resources.nutrition_meal_plan_partner_nudge_error
import ammo.composeapp.generated.resources.nutrition_meal_plan_partner_nudge_success
import ammo.composeapp.generated.resources.nutrition_meal_plan_partner_nudge_title
import ammo.composeapp.generated.resources.nutrition_meal_plan_partner_nudge_toolbar
import ammo.composeapp.generated.resources.nutrition_meal_plan_progress
import ammo.composeapp.generated.resources.nutrition_meal_plan_section_daily
import ammo.composeapp.generated.resources.nutrition_meal_plan_section_days
import ammo.composeapp.generated.resources.nutrition_meal_plan_section_today
import ammo.composeapp.generated.resources.nutrition_meal_plan_section_week
import ammo.composeapp.generated.resources.nutrition_meal_plan_title
import ammo.composeapp.generated.resources.nutrition_meal_slot_unplanned
import ammo.composeapp.generated.resources.nutrition_meal_suggest_quick_ai
import ammo.composeapp.generated.resources.nutrition_ai_criteria_quick_meal
import ammo.composeapp.generated.resources.nutrition_today
import ammo.composeapp.generated.resources.nutrition_week_label
import ammo.composeapp.generated.resources.nutrition_week_next
import ammo.composeapp.generated.resources.nutrition_week_previous
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
    val languageManager = koinInject<LanguageManager>()
    val currentLanguage by languageManager.currentLanguage.collectAsState()

    key(currentLanguage) {
        WeeklyMealPlanScreenContent(
            onBack = onBack,
            onOpenSection = onOpenSection,
            onOpenAiSheet = onOpenAiSheet,
            showBackButton = showBackButton,
            screenModel = screenModel,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyMealPlanScreenContent(
    onBack: () -> Unit,
    onOpenSection: (NutritionSection, NutritionAiMode?) -> Unit,
    onOpenAiSheet: (AiHelperLaunchContext) -> Unit,
    showBackButton: Boolean,
    screenModel: NutritionScreenModel,
) {
    val mealPlan by screenModel.mealPlan.collectAsState()
    val canWrite by screenModel.canWriteHouseholdData.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()
    val showPartnerNudge by screenModel.showMealPlanPartnerNudge.collectAsState()
    val isNudgingMealPlanPartners by screenModel.isNudgingMealPlanPartners.collectAsState()
    val mealPlanPartnerNudgeResult by screenModel.mealPlanPartnerNudgeResult.collectAsState()
    val mealGroceryLoading by screenModel.mealGroceryLoading.collectAsState()
    val mealGroceryResult by screenModel.mealGroceryResult.collectAsState()
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
    val progressLabel = stringResource(
        Res.string.nutrition_meal_plan_progress,
        mealProgress.plannedSlots,
        mealProgress.totalSlots,
    )
    val dailySectionTitle = stringResource(Res.string.nutrition_meal_plan_section_daily)
    val dailySectionHint = stringResource(Res.string.nutrition_meal_plan_daily_hint)

    val generateGroceryLabel = stringResource(Res.string.nutrition_meal_generate_grocery)
    val lunchLabel = stringResource(Res.string.nutrition_meal_lunch)
    val dinnerLabel = stringResource(Res.string.nutrition_meal_dinner)
    val suggestQuickMealLabel = stringResource(Res.string.nutrition_meal_suggest_quick_ai)
    val groceryNoneNew = stringResource(Res.string.nutrition_meal_grocery_none_new)
    val groceryError = stringResource(Res.string.nutrition_meal_grocery_error)

    val partnerNudgeTitle = stringResource(Res.string.nutrition_meal_plan_partner_nudge_title)
    val partnerNudgeBody = stringResource(Res.string.nutrition_meal_plan_partner_nudge_body)
    val partnerNudgeAction = stringResource(Res.string.nutrition_meal_plan_partner_nudge_action)
    val partnerNudgeToolbar = stringResource(Res.string.nutrition_meal_plan_partner_nudge_toolbar)
    val partnerNudgeSuccess = stringResource(Res.string.nutrition_meal_plan_partner_nudge_success)
    val partnerNudgeCooldown = stringResource(Res.string.nutrition_meal_plan_partner_nudge_cooldown)
    val partnerNudgeError = stringResource(Res.string.nutrition_meal_plan_partner_nudge_error)

    val partnerNudgeSnackbarMessage = when (mealPlanPartnerNudgeResult) {
        NutritionScreenModel.MealPlanPartnerNudgeResult.Success -> partnerNudgeSuccess
        NutritionScreenModel.MealPlanPartnerNudgeResult.Cooldown -> partnerNudgeCooldown
        NutritionScreenModel.MealPlanPartnerNudgeResult.Error -> partnerNudgeError
        null -> null
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

    LaunchedEffect(mealGrocerySnackbarMessage) {
        val message = mealGrocerySnackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeMealGroceryResult()
    }

    LaunchedEffect(partnerNudgeSnackbarMessage) {
        val message = partnerNudgeSnackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeMealPlanPartnerNudgeResult()
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

    fun loadingSlotFor(dayIndex: Int): MealSlot? {
        val request = mealGroceryLoading ?: return null
        return if (request.dayIndex == dayIndex) request.slot else null
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

    fun scrollToDailySection() {
        scope.launch {
            var targetIndex = 2
            if (!canWrite) targetIndex++
            if (isEmptyWeek) targetIndex++
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
            if (showPartnerNudge) {
                JourneyIconButton(
                    onClick = screenModel::nudgePartnersToUpdateMealPlan,
                    enabled = !isNudgingMealPlanPartners,
                    modifier = Modifier.testTag(MealPlanPartnerNudgeTestTags.TOOLBAR),
                ) {
                    JourneyIcon(
                        role = AppIconRole.NotifyPartners,
                        contentDescription = partnerNudgeToolbar,
                    )
                }
            }
            if (canWrite) {
                JourneyIconButton(onClick = { showOverflowMenu = true }) {
                    JourneyIcon(
                        role = AppIconRole.ChromeOverflow,
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
                    item(key = "week-selector") {
                        WeekSelectorBanner(
                            weekLabel = weekLabel,
                            canGoToPreviousWeek = screenModel.canGoToPreviousWeek,
                            canGoToNextWeek = screenModel.canGoToNextWeek,
                            previousWeekLabel = previousWeekLabel,
                            nextWeekLabel = nextWeekLabel,
                            onPreviousWeek = { screenModel.selectWeekOffset(weekOffset - 1) },
                            onNextWeek = { screenModel.selectWeekOffset(weekOffset + 1) },
                        )
                    }

                    item(key = "week-overview") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(MealPlanTestTags.WEEK_OVERVIEW),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            NutritionProgressChip(
                                label = progressLabel,
                                progress = mealProgress.plannedSlots.toFloat() / mealProgress.totalSlots,
                                accentColor = JourneySemanticColors.brandTerracotta(),
                            )
                        }
                    }

                    if (showPartnerNudge) {
                        item(key = "partner-nudge") {
                            PartnerNudgeCard(
                                title = partnerNudgeTitle,
                                body = partnerNudgeBody,
                                actionLabel = partnerNudgeAction,
                                onNudgePartners = screenModel::nudgePartnersToUpdateMealPlan,
                                loading = isNudgingMealPlanPartners,
                                accentColor = JourneySemanticColors.brandTerracotta(),
                                rootTestTag = MealPlanPartnerNudgeTestTags.ROOT,
                                actionTestTag = MealPlanPartnerNudgeTestTags.ACTION,
                            )
                        }
                    }

                    if (!canWrite) {
                        item(key = "viewer-notice") {
                            HouseholdViewerReadOnlyNotice()
                        }
                    }

                    if (isEmptyWeek) {
                        item(key = "empty-state") {
                            JourneyEmptyState(
                                title = stringResource(Res.string.nutrition_meal_plan_empty_title),
                                body = stringResource(Res.string.nutrition_meal_plan_empty_body),
                                nutritionFeature = NutritionFeatureKind.MealPlan,
                                primaryActionLabel = stringResource(
                                    Res.string.nutrition_meal_plan_empty_cta_manual,
                                ),
                                onPrimaryAction = ::scrollToDailySection,
                                testTag = MealPlanEmptyStateTestTags.ROOT,
                                primaryActionTestTag = MealPlanEmptyStateTestTags.ADD_MANUALLY,
                            )
                        }
                    }

                    item(key = "daily-section-header") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            FamilyLogisticsSectionHeader(
                                title = dailySectionTitle,
                                titleModifier = Modifier.testTag(MealPlanTestTags.DAILY_SECTION),
                            )
                            Text(
                                text = dailySectionHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = JourneySemanticColors.inkMuted(),
                                modifier = Modifier.padding(horizontal = 4.dp),
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
                            item(key = "today-header") {
                                FamilyLogisticsSectionHeader(
                                    title = stringResource(Res.string.nutrition_meal_plan_section_today),
                                )
                            }
                            item(key = "today-card") {
                                dayCard(todayEntry, isToday = true, initiallyExpanded = true)
                            }
                        }

                        if (upcomingEntries.isNotEmpty()) {
                            item(key = "upcoming-header") {
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
