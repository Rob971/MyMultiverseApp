package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.domain.nutrition.NutritionAiMode
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.AiReadOnlyGroceryList
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsCardSurface
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsDesign
import app.mymultiverse.kmp.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.kmp.presentation.components.JourneyEmptyState
import app.mymultiverse.kmp.presentation.components.JourneyTextField
import app.mymultiverse.kmp.presentation.components.JourneyPrimaryButton
import app.mymultiverse.kmp.presentation.components.JourneyTertiaryButton
import app.mymultiverse.kmp.presentation.components.NutritionFeatureHeader
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.nutritionDayLabel
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_apply_meal_plan
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_clear_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_criteria_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_empty_question
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_error
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_generate_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_cleared
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_result_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_saved_readonly_note
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_summary
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_ingredients_step_body
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_ingredients_step_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_ingredients_step_skip
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_ingredients_step_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_none
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_summary
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_idle_body
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_idle_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_loading
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_meal_plan_result_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_meal_plan_summary_full_week
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_meal_plan_summary_single_day
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_mode_advice
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_mode_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_mode_meal_plan
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_more_options
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_scope_full_week
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_scope_today
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_allergy
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_budget_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_budget_plan
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_protein
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_protein_plan
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_veggie_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestion_veggies
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_suggestions_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_try_again
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_undo_action
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_dinner
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_lunch
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_meal_plan_preview_line
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NutritionAiAssistantContent(
    modifier: Modifier = Modifier,
    initialMode: NutritionAiMode? = null,
    launchContext: AiHelperLaunchContext? = null,
    compact: Boolean = false,
    accentColor: Color = SharedJourneyColors.TerracottaOrange,
    screenModel: NutritionScreenModel,
    snackbarHostState: SnackbarHostState? = null,
    onMealPlanApplied: () -> Unit = {},
) {
    val aiState by screenModel.aiState.collectAsState()
    val aiGrocery by screenModel.aiGroceryItems.collectAsState()
    val canWrite by screenModel.canWriteHouseholdData.collectAsState()
    val mealGroceryLoading by screenModel.mealGroceryLoading.collectAsState()
    val adoptAllResult by screenModel.adoptAllGroceryResult.collectAsState()
    val resolvedMode = launchContext?.mode ?: initialMode ?: NutritionAiMode.Advice
    var criteria by remember(launchContext?.initialCriteria) {
        mutableStateOf(launchContext?.initialCriteria.orEmpty())
    }
    var mode by remember(resolvedMode) { mutableStateOf(resolvedMode) }
    var mealPlanScope by remember(launchContext?.mealPlanScope) {
        mutableStateOf(launchContext?.mealPlanScope ?: MealPlanGenerationScope.FullWeek)
    }
    val isLoading = aiState is NutritionAiState.Loading
    val inputsEnabled = canWrite && !isLoading
    val chipFirstSheet = compact && launchContext != null
    val modeLocked = chipFirstSheet
    var showMoreOptions by rememberSaveable(chipFirstSheet) {
        mutableStateOf(!chipFirstSheet)
    }
    val todayIndex = remember(screenModel.weekKey) { WeekCalendar.todayIndexInWeek(screenModel.weekKey) }
    val coroutineScope = rememberCoroutineScope()
    val localSnackbar = remember { SnackbarHostState() }
    val snackbar = snackbarHostState ?: localSnackbar
    var mealPlanApplyRequested by remember { mutableStateOf(false) }
    var showIngredientsStep by remember { mutableStateOf(false) }
    val scopeLocked = launchContext?.mealPlanScope is MealPlanGenerationScope.SingleDay
    val ingredientsAfterApplyFlow = chipFirstSheet &&
        launchContext?.offerIngredientsAfterApply == true &&
        launchContext.targetMealSlot != null &&
        launchContext.mealPlanScope is MealPlanGenerationScope.SingleDay

    val adoptAllNoneMessage = stringResource(Res.string.nutrition_ai_adopt_all_grocery_none)
    val adoptAllSummaryMessage = adoptAllResult?.let { count ->
        if (count == 0) {
            adoptAllNoneMessage
        } else {
            stringResource(Res.string.nutrition_ai_adopt_all_grocery_summary, count)
        }
    }

    fun finishIngredientsStep() {
        showIngredientsStep = false
        onMealPlanApplied()
    }

    LaunchedEffect(launchContext) {
        if (launchContext != null) {
            mode = launchContext.mode
            mealPlanScope = launchContext.mealPlanScope
            criteria = launchContext.initialCriteria
            if (chipFirstSheet && launchContext.initialCriteria.isNotBlank()) {
                screenModel.runAiAssistant(
                    mode = launchContext.mode,
                    criteria = launchContext.initialCriteria,
                    mealPlanScope = launchContext.mealPlanScope,
                )
            }
        }
    }

    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )

    val suggestions = when (mode) {
        NutritionAiMode.Advice -> listOf(
            stringResource(Res.string.nutrition_ai_suggestion_protein),
            stringResource(Res.string.nutrition_ai_suggestion_veggies),
            stringResource(Res.string.nutrition_ai_suggestion_allergy),
        )
        NutritionAiMode.GroceryList -> listOf(
            stringResource(Res.string.nutrition_ai_suggestion_protein),
            stringResource(Res.string.nutrition_ai_suggestion_veggie_grocery),
            stringResource(Res.string.nutrition_ai_suggestion_budget_grocery),
        )
        NutritionAiMode.MealPlan -> listOf(
            stringResource(Res.string.nutrition_ai_suggestion_protein_plan),
            stringResource(Res.string.nutrition_ai_suggestion_budget_plan),
            stringResource(Res.string.nutrition_ai_suggestion_allergy),
        )
    }

    fun generate() {
        screenModel.runAiAssistant(
            mode = mode,
            criteria = criteria,
            mealPlanScope = mealPlanScope,
        )
    }

    val undoLabel = stringResource(Res.string.nutrition_grocery_undo_action)
    val aiGroceryClearedMessage = stringResource(Res.string.nutrition_ai_grocery_cleared)

    fun clearAiGroceryWithUndo(resetStateOnDismiss: Boolean) {
        val snapshot = screenModel.clearAiGrocery()
        if (snapshot.isEmpty()) return
        coroutineScope.launch {
            val result = snackbar.showSnackbar(
                message = aiGroceryClearedMessage,
                actionLabel = undoLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                screenModel.restoreAiGroceryItems(snapshot)
            } else if (resetStateOnDismiss) {
                screenModel.resetAiState()
            }
        }
    }

    LaunchedEffect(aiState, mealPlanApplyRequested) {
        if (mealPlanApplyRequested && aiState is NutritionAiState.Idle) {
            mealPlanApplyRequested = false
            onMealPlanApplied()
        }
    }

    LaunchedEffect(adoptAllSummaryMessage, showIngredientsStep) {
        if (!showIngredientsStep) return@LaunchedEffect
        val message = adoptAllSummaryMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(message)
        screenModel.consumeAdoptAllGroceryResult()
        finishIngredientsStep()
    }

    LazyColumn(
        modifier = modifier.testTag(NutritionAiTestTags.SCROLL_LIST),
        contentPadding = screenListPadding(),
        verticalArrangement = Arrangement.spacedBy(ScreenLayout.sectionSpacing),
    ) {
        if (!compact) {
            item {
                NutritionFeatureHeader(
                    weekLabel = weekLabel,
                    description = stringResource(Res.string.nutrition_ai_description),
                    icon = AppIcons.Sparkles,
                    accentColor = accentColor,
                    progressLabel = stringResource(Res.string.nutrition_ai_suggestions_title),
                    progress = when (mode) {
                        NutritionAiMode.Advice -> 0.33f
                        NutritionAiMode.GroceryList -> 0.66f
                        NutritionAiMode.MealPlan -> 1f
                    },
                )
            }
        } else {
            item {
                Text(
                    text = stringResource(Res.string.nutrition_ai_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JourneySemanticColors.inkDeep(),
                )
                Text(
                    text = stringResource(Res.string.nutrition_ai_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneySemanticColors.inkMuted(),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        if (!canWrite) {
            item {
                HouseholdViewerReadOnlyNotice()
            }
        }

        if (!modeLocked) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ModeChip(
                        label = stringResource(Res.string.nutrition_ai_mode_advice),
                        selected = mode == NutritionAiMode.Advice,
                        enabled = inputsEnabled,
                        accentColor = accentColor,
                        modifier = Modifier.testTag(NutritionAiTestTags.MODE_ADVICE),
                        onClick = { mode = NutritionAiMode.Advice },
                    )
                    ModeChip(
                        label = stringResource(Res.string.nutrition_ai_mode_grocery),
                        selected = mode == NutritionAiMode.GroceryList,
                        enabled = inputsEnabled,
                        accentColor = accentColor,
                        modifier = Modifier.testTag(NutritionAiTestTags.MODE_GROCERY),
                        onClick = { mode = NutritionAiMode.GroceryList },
                    )
                    ModeChip(
                        label = stringResource(Res.string.nutrition_ai_mode_meal_plan),
                        selected = mode == NutritionAiMode.MealPlan,
                        enabled = inputsEnabled,
                        accentColor = accentColor,
                        modifier = Modifier.testTag(NutritionAiTestTags.MODE_MEAL_PLAN),
                        onClick = { mode = NutritionAiMode.MealPlan },
                    )
                }
            }
        }

        if (mode == NutritionAiMode.MealPlan && todayIndex != null && !scopeLocked) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ModeChip(
                        label = stringResource(Res.string.nutrition_ai_scope_full_week),
                        selected = mealPlanScope is MealPlanGenerationScope.FullWeek,
                        enabled = inputsEnabled,
                        accentColor = accentColor,
                        modifier = Modifier.testTag(NutritionAiTestTags.SCOPE_FULL_WEEK),
                        onClick = { mealPlanScope = MealPlanGenerationScope.FullWeek },
                    )
                    ModeChip(
                        label = stringResource(Res.string.nutrition_ai_scope_today),
                        selected = mealPlanScope is MealPlanGenerationScope.SingleDay,
                        enabled = inputsEnabled,
                        accentColor = accentColor,
                        modifier = Modifier.testTag(NutritionAiTestTags.SCOPE_TODAY),
                        onClick = { mealPlanScope = MealPlanGenerationScope.SingleDay(todayIndex) },
                    )
                }
            }
        }

        item {
            Text(
                text = stringResource(Res.string.nutrition_ai_suggestions_title),
                style = MaterialTheme.typography.labelLarge,
                color = JourneySemanticColors.inkDeep(),
                fontWeight = FontWeight.SemiBold,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        label = suggestion,
                        enabled = inputsEnabled,
                        onClick = {
                            criteria = suggestion
                            if (chipFirstSheet) {
                                generate()
                            }
                        },
                    )
                }
            }
        }

        if (chipFirstSheet && !showMoreOptions) {
            item {
                JourneyTertiaryButton(
                    onClick = { showMoreOptions = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(NutritionAiTestTags.MORE_OPTIONS_TOGGLE),
                    label = stringResource(Res.string.nutrition_ai_more_options),
                )
            }
        }

        if (!chipFirstSheet || showMoreOptions) {
            item {
                JourneyTextField(
                    value = criteria,
                    onValueChange = { criteria = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(NutritionAiTestTags.CRITERIA_FIELD),
                    placeholder = { Text(stringResource(Res.string.nutrition_ai_criteria_hint)) },
                    singleLine = false,
                    minLines = if (compact) 2 else 3,
                    maxLines = if (compact) 3 else 5,
                    enabled = inputsEnabled,
                    focusAccentColor = accentColor,
                )
            }

            item {
                Button(
                    onClick = { generate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(NutritionAiTestTags.GENERATE_BUTTON),
                    enabled = canWrite && criteria.isNotBlank() && !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SharedJourneyColors.SunDrenchedWhite,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(Res.string.nutrition_ai_generate_button))
                    }
                }
            }
        }

        if (aiGrocery.isNotEmpty() && mode != NutritionAiMode.GroceryList && !showIngredientsStep) {
            item {
                AiReadOnlyGroceryList(
                    items = aiGrocery,
                    title = stringResource(Res.string.nutrition_ai_grocery_result_title),
                    subtitle = stringResource(Res.string.nutrition_ai_grocery_saved_readonly_note),
                )
            }
        }

        if (showIngredientsStep) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(NutritionAiTestTags.INGREDIENTS_STEP),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.nutrition_ai_ingredients_step_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = JourneySemanticColors.inkDeep(),
                    )
                    Text(
                        text = stringResource(Res.string.nutrition_ai_ingredients_step_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
                    )
                    if (mealGroceryLoading != null) {
                        Text(
                            text = stringResource(Res.string.nutrition_ai_ingredients_step_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = JourneySemanticColors.inkMuted(),
                        )
                    } else if (aiGrocery.isNotEmpty()) {
                        AiReadOnlyGroceryList(
                            items = aiGrocery,
                            title = stringResource(Res.string.nutrition_ai_grocery_result_title),
                            subtitle = stringResource(Res.string.nutrition_ai_grocery_saved_readonly_note),
                        )
                        JourneyPrimaryButton(
                            onClick = { screenModel.adoptAllAiGrocerySuggestions() },
                            enabled = canWrite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(NutritionAiTestTags.INGREDIENTS_ADD_ALL),
                        ) {
                            Text(stringResource(Res.string.nutrition_ai_adopt_all_grocery))
                        }
                    }
                    JourneyTertiaryButton(
                        onClick = { finishIngredientsStep() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(NutritionAiTestTags.INGREDIENTS_SKIP),
                        label = stringResource(Res.string.nutrition_ai_ingredients_step_skip),
                    )
                }
            }
        } else when (val state = aiState) {
            NutritionAiState.Idle -> {
                item {
                    JourneyEmptyState(
                        title = stringResource(Res.string.nutrition_ai_idle_title),
                        body = stringResource(Res.string.nutrition_ai_idle_body),
                        icon = AppIcons.Sparkles,
                        testTag = NutritionAiTestTags.IDLE_EMPTY,
                    )
                }
            }
            NutritionAiState.Loading -> {
                item {
                    Text(
                        text = stringResource(Res.string.nutrition_ai_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
                    )
                }
            }
            is NutritionAiState.Advice -> {
                item {
                    AiResultCard(
                        title = stringResource(Res.string.nutrition_ai_mode_advice),
                        body = state.text,
                        accentColor = accentColor,
                        testTag = NutritionAiTestTags.ANSWER_CARD,
                    )
                }
                item { ResetButton { screenModel.resetAiState(); criteria = "" } }
            }
            is NutritionAiState.GroceryList -> {
                item {
                    Text(
                        text = stringResource(
                            Res.string.nutrition_ai_grocery_summary,
                            state.itemCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
                    )
                }
                item {
                    AiReadOnlyGroceryList(
                        items = aiGrocery,
                        title = stringResource(Res.string.nutrition_ai_grocery_result_title),
                        subtitle = stringResource(Res.string.nutrition_ai_grocery_saved_readonly_note),
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { clearAiGroceryWithUndo(resetStateOnDismiss = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(NutritionAiTestTags.CLEAR_AI_GROCERY_BUTTON),
                    ) {
                        Text(stringResource(Res.string.nutrition_ai_clear_grocery))
                    }
                }
                item { ResetButton { screenModel.resetAiState(); criteria = "" } }
            }
            is NutritionAiState.MealPlanPreview -> {
                item {
                    val summaryText = when (val scope = state.scope) {
                        is MealPlanGenerationScope.FullWeek ->
                            stringResource(Res.string.nutrition_ai_meal_plan_summary_full_week)
                        is MealPlanGenerationScope.SingleDay ->
                            stringResource(
                                Res.string.nutrition_ai_meal_plan_summary_single_day,
                                nutritionDayLabel(scope.dayIndex),
                            )
                    }
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
                    )
                }
                item {
                    Text(
                        text = stringResource(Res.string.nutrition_ai_meal_plan_result_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = JourneySemanticColors.inkDeep(),
                    )
                }
                val daysToShow = when (state.scope) {
                    is MealPlanGenerationScope.FullWeek -> state.plan.days.indices.toList()
                    is MealPlanGenerationScope.SingleDay -> listOf(state.scope.dayIndex)
                }
                items(daysToShow, key = { it }) { dayIndex ->
                    val day = state.plan.days[dayIndex]
                    MealPlanPreviewCard(
                        dayIndex = dayIndex,
                        lunch = day.lunch,
                        dinner = day.dinner,
                        modifier = Modifier.testTag(
                            "${NutritionAiTestTags.MEAL_PLAN_PREVIEW_ROW_PREFIX}$dayIndex",
                        ),
                    )
                }
                item {
                    Button(
                        onClick = {
                            if (ingredientsAfterApplyFlow) {
                                val ctx = launchContext ?: return@Button
                                val dayIndex =
                                    (ctx.mealPlanScope as MealPlanGenerationScope.SingleDay).dayIndex
                                val slot = ctx.targetMealSlot ?: return@Button
                                coroutineScope.launch {
                                    screenModel.applyPreviewedMealPlanAndAwait()
                                    showIngredientsStep = true
                                    screenModel.generateGroceryForMealSilent(dayIndex, slot)
                                }
                            } else {
                                mealPlanApplyRequested = true
                                screenModel.applyPreviewedMealPlan()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(NutritionAiTestTags.APPLY_MEAL_PLAN_BUTTON),
                        enabled = canWrite,
                    ) {
                        Text(stringResource(Res.string.nutrition_ai_apply_meal_plan))
                    }
                }
                item { ResetButton { screenModel.resetAiState(); criteria = "" } }
            }
            is NutritionAiState.Error -> {
                item {
                    Text(
                        text = when (state.message) {
                            "empty_question", "empty_criteria" ->
                                stringResource(Res.string.nutrition_ai_empty_question)
                            else -> stringResource(Res.string.nutrition_ai_error)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = SharedJourneyColors.TerracottaOrange,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiResultCard(
    title: String,
    body: String,
    accentColor: Color,
    testTag: String,
) {
    FamilyLogisticsCardSurface(accentColor = accentColor) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag(testTag),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = JourneySemanticColors.inkDeep(),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun MealPlanPreviewCard(
    dayIndex: Int,
    lunch: String,
    dinner: String,
    modifier: Modifier = Modifier,
) {
    val dayLabel = nutritionDayLabel(dayIndex)
    FamilyLogisticsCardSurface {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
            )
            Text(
                text = stringResource(
                    Res.string.nutrition_meal_plan_preview_line,
                    stringResource(Res.string.nutrition_meal_lunch),
                    lunch,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = JourneySemanticColors.inkDeep(),
            )
            Text(
                text = stringResource(
                    Res.string.nutrition_meal_plan_preview_line,
                    stringResource(Res.string.nutrition_meal_dinner),
                    dinner,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = JourneySemanticColors.inkDeep(),
            )
        }
    }
}

@Composable
private fun ResetButton(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(Res.string.nutrition_ai_try_again))
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = if (selected) {
        accentColor to SharedJourneyColors.SunDrenchedWhite
    } else {
        JourneySemanticColors.cardSurface() to accentColor
    }
    Surface(
        modifier = modifier
            .semantics { this.selected = selected }
            .clickable(
                enabled = enabled,
                role = Role.Button,
            ) { onClick() },
        shape = FamilyLogisticsDesign.fieldShape,
        color = colors.first.copy(alpha = if (selected) 1f else 0.12f),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.35f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) SharedJourneyColors.SunDrenchedWhite else colors.second,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(
            enabled = enabled,
            role = Role.Button,
        ) { onClick() },
        shape = FamilyLogisticsDesign.fieldShape,
        color = SharedJourneyColors.GlassTerracotta,
        border = BorderStroke(
            1.dp,
            SharedJourneyColors.TerracottaOrange.copy(alpha = 0.3f),
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = SharedJourneyColors.TerracottaOrange,
        )
    }
}
