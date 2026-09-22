package app.mymultiverse.ammo.presentation.screens.nutrition

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
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
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.nutrition.MealPlanGenerationScope
import app.mymultiverse.ammo.domain.nutrition.MealSlot
import app.mymultiverse.ammo.domain.nutrition.NutritionAiMode
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.components.AiReadOnlyGroceryList
import app.mymultiverse.ammo.presentation.components.FamilyLogisticsCardSurface
import app.mymultiverse.ammo.presentation.components.FamilyLogisticsDesign
import app.mymultiverse.ammo.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.ammo.presentation.components.JourneyEmptyState
import app.mymultiverse.ammo.presentation.components.NutritionFeatureKind
import app.mymultiverse.ammo.presentation.components.JourneyTextField
import app.mymultiverse.ammo.presentation.components.JourneyButtonLabel
import app.mymultiverse.ammo.presentation.components.JourneyPrimaryButton
import app.mymultiverse.ammo.presentation.components.JourneyTertiaryButton
import app.mymultiverse.ammo.presentation.components.rememberFieldScrollIntoViewModifier
import app.mymultiverse.ammo.presentation.components.NutritionFeatureHeader
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.nutritionDayLabel
import app.mymultiverse.ammo.presentation.components.screenListPadding
import app.mymultiverse.ammo.presentation.components.JourneyIcon
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_ai_apply_meal_plan
import ammo.composeapp.generated.resources.nutrition_ai_replace_week
import ammo.composeapp.generated.resources.nutrition_ai_clear_grocery
import ammo.composeapp.generated.resources.nutrition_ai_criteria_hint
import ammo.composeapp.generated.resources.nutrition_ai_description
import ammo.composeapp.generated.resources.nutrition_ai_empty_question
import ammo.composeapp.generated.resources.nutrition_ai_error
import ammo.composeapp.generated.resources.nutrition_ai_error_daily_limit
import ammo.composeapp.generated.resources.nutrition_ai_error_network
import ammo.composeapp.generated.resources.nutrition_ai_error_sign_in
import ammo.composeapp.generated.resources.nutrition_ai_error_unavailable
import ammo.composeapp.generated.resources.nutrition_ai_generating
import ammo.composeapp.generated.resources.nutrition_ai_saved_household
import ammo.composeapp.generated.resources.nutrition_ai_saved_personal
import ammo.composeapp.generated.resources.nutrition_ai_generate_button
import ammo.composeapp.generated.resources.nutrition_ai_grocery_cleared
import ammo.composeapp.generated.resources.nutrition_ai_grocery_result_title
import ammo.composeapp.generated.resources.nutrition_ai_grocery_saved_readonly_note
import ammo.composeapp.generated.resources.nutrition_ai_grocery_summary
import ammo.composeapp.generated.resources.nutrition_ai_ingredients_step_body
import ammo.composeapp.generated.resources.nutrition_ai_ingredients_step_loading
import ammo.composeapp.generated.resources.nutrition_ai_ingredients_step_skip
import ammo.composeapp.generated.resources.nutrition_ai_ingredients_step_title
import ammo.composeapp.generated.resources.nutrition_ai_adopt_all_grocery
import ammo.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_none
import ammo.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_summary
import ammo.composeapp.generated.resources.nutrition_ai_idle_body
import ammo.composeapp.generated.resources.nutrition_ai_idle_title
import ammo.composeapp.generated.resources.nutrition_ai_meal_plan_result_title
import ammo.composeapp.generated.resources.nutrition_ai_meal_plan_summary_full_week
import ammo.composeapp.generated.resources.nutrition_ai_meal_plan_summary_single_day
import ammo.composeapp.generated.resources.nutrition_ai_meal_plan_summary_single_meal
import ammo.composeapp.generated.resources.nutrition_ai_mode_advice
import ammo.composeapp.generated.resources.nutrition_ai_mode_grocery
import ammo.composeapp.generated.resources.nutrition_ai_mode_meal_plan
import ammo.composeapp.generated.resources.nutrition_ai_more_options
import ammo.composeapp.generated.resources.nutrition_ai_scope_full_week
import ammo.composeapp.generated.resources.nutrition_ai_scope_today
import ammo.composeapp.generated.resources.nutrition_ai_chip_use_up
import ammo.composeapp.generated.resources.nutrition_ai_criteria_use_up
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_allergy
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_budget_grocery
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_budget_plan
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_protein
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_protein_plan
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_seasonal_week
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_veggie_grocery
import ammo.composeapp.generated.resources.nutrition_ai_suggestion_veggies
import ammo.composeapp.generated.resources.nutrition_ai_suggestions_title
import ammo.composeapp.generated.resources.nutrition_ai_title
import ammo.composeapp.generated.resources.nutrition_ai_try_again
import ammo.composeapp.generated.resources.nutrition_grocery_undo_action
import ammo.composeapp.generated.resources.nutrition_meal_accept
import ammo.composeapp.generated.resources.nutrition_meal_accept_done
import ammo.composeapp.generated.resources.nutrition_meal_dinner
import ammo.composeapp.generated.resources.nutrition_meal_lunch
import ammo.composeapp.generated.resources.nutrition_meal_plan_preview_line
import ammo.composeapp.generated.resources.nutrition_meal_replace
import ammo.composeapp.generated.resources.nutrition_week_label
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private data class AiQuickPick(
    val label: String,
    val criteria: String,
    val testTag: String? = null,
    /** Runs instead of filling [criteria], for picks that need no criteria. */
    val onPick: (() -> Unit)? = null,
)

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
    val mealPlan by screenModel.mealPlan.collectAsState()
    val groceryItems by screenModel.groceryItems.collectAsState()
    val languageManager = koinInject<LanguageManager>()
    val currentLanguage by languageManager.currentLanguage.collectAsState()
    val canWrite by screenModel.canWriteHouseholdData.collectAsState()
    val mealGroceryLoading by screenModel.mealGroceryLoading.collectAsState()
    val adoptAllResult by screenModel.adoptAllGroceryResult.collectAsState()
    val mealPlanAcceptUndo by screenModel.mealPlanAcceptUndo.collectAsState()
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
    // Mode is a decision only when the caller had no intent. Launched from a meal slot or
    // the seasonal chip, the mode is already known and showing the picker is noise.
    val modeLocked = launchContext != null
    // Everyone starts on suggestions. Mode, scope, the free-text field and Generate are
    // configuration, and they live behind "More options" until someone wants them.
    var showMoreOptions by rememberSaveable { mutableStateOf(false) }
    val todayIndex = remember(screenModel.weekKey) { WeekCalendar.todayIndexInWeek(screenModel.weekKey) }
    val coroutineScope = rememberCoroutineScope()
    val localSnackbar = remember { SnackbarHostState() }
    val snackbar = snackbarHostState ?: localSnackbar
    val criteriaScrollIntoView = rememberFieldScrollIntoViewModifier()
    var mealPlanApplyRequested by remember { mutableStateOf(false) }
    var showIngredientsStep by remember { mutableStateOf(false) }
    val scopeLocked = launchContext?.mealPlanScope is MealPlanGenerationScope.SingleDay ||
        launchContext?.mealPlanScope is MealPlanGenerationScope.SingleMeal
    val ingredientsAfterApplyFlow = chipFirstSheet &&
        launchContext.offerIngredientsAfterApply &&
        launchContext.targetMealSlot != null &&
        (launchContext.mealPlanScope is MealPlanGenerationScope.SingleDay ||
            launchContext.mealPlanScope is MealPlanGenerationScope.SingleMeal)

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

    val contextualMatches = remember(mealPlan, groceryItems, currentLanguage) {
        NutritionContextualChipsResolver.ingredientMatches(
            mealPlan = mealPlan,
            groceryItems = groceryItems,
            languageCode = currentLanguage,
        )
    }
    val contextualQuickPicks = contextualMatches.map { match ->
        AiQuickPick(
            label = stringResource(Res.string.nutrition_ai_chip_use_up, match.displayName),
            criteria = stringResource(Res.string.nutrition_ai_criteria_use_up, match.displayName),
            testTag = NutritionAiTestTags.contextualChip(match.id),
        )
    }
    val staticQuickPicks = when (mode) {
        NutritionAiMode.Advice -> listOf(
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_protein),
                stringResource(Res.string.nutrition_ai_suggestion_protein),
            ),
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_veggies),
                stringResource(Res.string.nutrition_ai_suggestion_veggies),
            ),
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_allergy),
                stringResource(Res.string.nutrition_ai_suggestion_allergy),
            ),
        )
        NutritionAiMode.GroceryList -> listOf(
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_protein),
                stringResource(Res.string.nutrition_ai_suggestion_protein),
            ),
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_veggie_grocery),
                stringResource(Res.string.nutrition_ai_suggestion_veggie_grocery),
            ),
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_budget_grocery),
                stringResource(Res.string.nutrition_ai_suggestion_budget_grocery),
            ),
        )
        NutritionAiMode.MealPlan -> listOf(
            AiQuickPick(
                label = stringResource(Res.string.nutrition_ai_suggestion_seasonal_week),
                criteria = "",
                testTag = NutritionAiTestTags.SEASONAL_WEEK_CHIP,
                onPick = { screenModel.suggestSeasonalWeek() },
            ),
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_protein_plan),
                stringResource(Res.string.nutrition_ai_suggestion_protein_plan),
            ),
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_budget_plan),
                stringResource(Res.string.nutrition_ai_suggestion_budget_plan),
            ),
            AiQuickPick(
                stringResource(Res.string.nutrition_ai_suggestion_allergy),
                stringResource(Res.string.nutrition_ai_suggestion_allergy),
            ),
        )
    }
    val quickPicks = contextualQuickPicks + staticQuickPicks

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

    val mealAcceptUndoMessage = stringResource(Res.string.nutrition_meal_accept_done)
    val mealAcceptUndoAction = stringResource(Res.string.nutrition_grocery_undo_action)
    LaunchedEffect(mealPlanAcceptUndo, mealAcceptUndoMessage, mealAcceptUndoAction) {
        val undo = mealPlanAcceptUndo ?: return@LaunchedEffect
        val result = snackbar.showSnackbar(
            message = mealAcceptUndoMessage,
            actionLabel = mealAcceptUndoAction,
            withDismissAction = true,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> screenModel.undoMealPlanAccept()
            SnackbarResult.Dismissed -> screenModel.clearMealPlanAcceptUndo()
        }
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

        if (!modeLocked && showMoreOptions) {
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

        if (showMoreOptions && mode == NutritionAiMode.MealPlan && todayIndex != null && !scopeLocked) {
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
                quickPicks.forEach { pick ->
                    SuggestionChip(
                        label = pick.label,
                        enabled = inputsEnabled,
                        modifier = pick.testTag?.let { Modifier.testTag(it) } ?: Modifier,
                        onClick = {
                            val action = pick.onPick
                            if (action != null) {
                                action()
                            } else {
                                // A suggestion is a request, not a form pre-fill: run it.
                                criteria = pick.criteria
                                screenModel.runAiAssistant(
                                    mode = mode,
                                    criteria = pick.criteria,
                                    mealPlanScope = mealPlanScope,
                                )
                            }
                        },
                    )
                }
            }
        }

        if (!showMoreOptions) {
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

        if (showMoreOptions) {
            item {
                JourneyTextField(
                    value = criteria,
                    onValueChange = { criteria = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(criteriaScrollIntoView)
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
                            JourneyButtonLabel(
                                text = stringResource(Res.string.nutrition_ai_adopt_all_grocery),
                                nutritionFeature = NutritionFeatureKind.Grocery,
                                role = AppIconRole.OnAccent,
                                useContentColor = true,
                            )
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
                    // A seasonal week can take ~30s. A bare line of grey text reads as a
                    // frozen sheet, so show motion next to the message.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = accentColor,
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = stringResource(Res.string.nutrition_ai_generating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = JourneySemanticColors.inkMuted(),
                        )
                    }
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
                    val lunchLabel = stringResource(Res.string.nutrition_meal_lunch)
                    val dinnerLabel = stringResource(Res.string.nutrition_meal_dinner)
                    val summaryText = when (val scope = state.scope) {
                        is MealPlanGenerationScope.FullWeek ->
                            stringResource(Res.string.nutrition_ai_meal_plan_summary_full_week)
                        is MealPlanGenerationScope.SingleDay ->
                            stringResource(
                                Res.string.nutrition_ai_meal_plan_summary_single_day,
                                nutritionDayLabel(scope.dayIndex),
                            )
                        is MealPlanGenerationScope.SingleMeal ->
                            stringResource(
                                Res.string.nutrition_ai_meal_plan_summary_single_meal,
                                nutritionDayLabel(scope.dayIndex),
                                when (scope.slot) {
                                    MealSlot.Lunch -> lunchLabel
                                    MealSlot.Dinner -> dinnerLabel
                                },
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
                    is MealPlanGenerationScope.SingleMeal -> listOf(state.scope.dayIndex)
                }
                val singleMealSlot = (state.scope as? MealPlanGenerationScope.SingleMeal)?.slot
                items(daysToShow, key = { it }) { dayIndex ->
                    val day = state.plan.days[dayIndex]
                    val liveDay = mealPlan.days.getOrNull(dayIndex)
                    MealPlanPreviewCard(
                        dayIndex = dayIndex,
                        lunch = day.lunch,
                        dinner = day.dinner,
                        visibleSlot = singleMealSlot,
                        liveLunch = liveDay?.lunch ?: "",
                        liveDinner = liveDay?.dinner ?: "",
                        canAccept = canWrite,
                        onAccept = { slot -> screenModel.acceptPreviewedMeal(dayIndex, slot) },
                        modifier = Modifier.testTag(
                            "${NutritionAiTestTags.MEAL_PLAN_PREVIEW_ROW_PREFIX}$dayIndex",
                        ),
                    )
                }
                item {
                    Button(
                        onClick = {
                            if (ingredientsAfterApplyFlow) {
                                val ctx = launchContext
                                val dayIndex = when (val s = ctx.mealPlanScope) {
                                    is MealPlanGenerationScope.SingleMeal -> s.dayIndex
                                    is MealPlanGenerationScope.SingleDay -> s.dayIndex
                                    is MealPlanGenerationScope.FullWeek -> return@Button
                                }
                                val slot = ctx.targetMealSlot
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
                        Text(
                            text = stringResource(
                                if (state.scope is MealPlanGenerationScope.FullWeek &&
                                    mealPlan.days.any { it.lunch.isNotBlank() || it.dinner.isNotBlank() }
                                ) {
                                    Res.string.nutrition_ai_replace_week
                                } else {
                                    Res.string.nutrition_ai_apply_meal_plan
                                },
                            ),
                        )
                    }
                }
                item { ResetButton { screenModel.resetAiState(); criteria = "" } }
            }
            is NutritionAiState.Error -> {
                item(key = "ai-error-message") {
                    Text(
                        text = aiErrorMessage(state.kind),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SharedJourneyColors.TerracottaOrange,
                        modifier = Modifier.testTag(NutritionAiTestTags.ERROR_MESSAGE),
                    )
                }
                // Retrying a spent daily allowance or a rejected session just fails again,
                // so only offer the retry where it can actually succeed.
                if (state.kind != AiErrorKind.DailyLimitReached &&
                    state.kind != AiErrorKind.SignInRequired
                ) {
                    item(key = "ai-error-retry") {
                        JourneyTertiaryButton(
                            onClick = { generate() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(NutritionAiTestTags.ERROR_RETRY),
                            enabled = inputsEnabled && criteria.isNotBlank(),
                            label = stringResource(Res.string.nutrition_ai_try_again),
                        )
                    }
                }
                item { ResetButton { screenModel.resetAiState() } }
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
    /** When non-null, only the specified slot is shown (single-meal generation flow). */
    visibleSlot: MealSlot? = null,
    liveLunch: String = "",
    liveDinner: String = "",
    canAccept: Boolean = true,
    onAccept: (MealSlot) -> Unit = {},
) {
    val dayLabel = nutritionDayLabel(dayIndex)
    FamilyLogisticsCardSurface {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
            )
            if (visibleSlot == null || visibleSlot == MealSlot.Lunch) {
                MealPlanPreviewSlot(
                    slotLabel = stringResource(Res.string.nutrition_meal_lunch),
                    text = lunch,
                    accepted = lunch.isNotBlank() && lunch.trim() == liveLunch.trim(),
                    replace = liveLunch.isNotBlank() && lunch.trim() != liveLunch.trim(),
                    canAccept = canAccept,
                    testTag = "${NutritionAiTestTags.MEAL_PLAN_ACCEPT_LUNCH_PREFIX}$dayIndex",
                    onAccept = { onAccept(MealSlot.Lunch) },
                )
            }
            if (visibleSlot == null || visibleSlot == MealSlot.Dinner) {
                MealPlanPreviewSlot(
                    slotLabel = stringResource(Res.string.nutrition_meal_dinner),
                    text = dinner,
                    accepted = dinner.isNotBlank() && dinner.trim() == liveDinner.trim(),
                    replace = liveDinner.isNotBlank() && dinner.trim() != liveDinner.trim(),
                    canAccept = canAccept,
                    testTag = "${NutritionAiTestTags.MEAL_PLAN_ACCEPT_DINNER_PREFIX}$dayIndex",
                    onAccept = { onAccept(MealSlot.Dinner) },
                )
            }
        }
    }
}

@Composable
private fun MealPlanPreviewSlot(
    slotLabel: String,
    text: String,
    accepted: Boolean,
    replace: Boolean,
    canAccept: Boolean,
    testTag: String,
    onAccept: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(Res.string.nutrition_meal_plan_preview_line, slotLabel, text),
            style = MaterialTheme.typography.bodyMedium,
            color = JourneySemanticColors.inkDeep(),
        )
        if (accepted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                JourneyIcon(
                    role = AppIconRole.ActionConfirm,
                    contentDescription = stringResource(Res.string.nutrition_meal_accept_done),
                    modifier = Modifier.size(16.dp),
                )
            }
        } else {
            JourneyTertiaryButton(
                onClick = onAccept,
                modifier = Modifier.testTag(testTag),
                enabled = canAccept,
                label = stringResource(
                    if (replace) Res.string.nutrition_meal_replace else Res.string.nutrition_meal_accept,
                ),
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
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(
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

/**
 * One specific, actionable sentence per failure kind. The app holds no Gemini key, so
 * none of these ever asks the user for one.
 */
@Composable
private fun aiErrorMessage(kind: AiErrorKind): String = when (kind) {
    AiErrorKind.SignInRequired -> stringResource(Res.string.nutrition_ai_error_sign_in)
    AiErrorKind.DailyLimitReached -> stringResource(Res.string.nutrition_ai_error_daily_limit)
    AiErrorKind.ServiceUnavailable -> stringResource(Res.string.nutrition_ai_error_unavailable)
    AiErrorKind.Network -> stringResource(Res.string.nutrition_ai_error_network)
    AiErrorKind.EmptyInput -> stringResource(Res.string.nutrition_ai_empty_question)
    AiErrorKind.Generic -> stringResource(Res.string.nutrition_ai_error)
}
