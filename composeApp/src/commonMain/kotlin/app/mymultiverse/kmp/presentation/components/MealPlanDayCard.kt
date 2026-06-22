package app.mymultiverse.kmp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import app.mymultiverse.kmp.presentation.components.JourneyPrimaryButton
import app.mymultiverse.kmp.presentation.components.AiInlineTriggerButton
import app.mymultiverse.kmp.presentation.components.JourneyTertiaryButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.nutrition.DayMeals
import app.mymultiverse.kmp.domain.nutrition.MealPlanPresentation
import app.mymultiverse.kmp.domain.nutrition.MealSlot
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors

object MealPlanTestTags {
    const val SCROLL_LIST = "meal_plan_scroll_list"
    const val GENERATE_ALL_GROCERY = "meal_plan_generate_all_grocery"
    const val CLEAR_WEEK = "meal_plan_clear_week"
    const val PLAN_WITH_AI = "meal_plan_plan_with_ai"
    fun suggestAiButton(dayIndex: Int, slot: MealSlot) =
        "meal_plan_suggest_ai_${slot.name.lowercase()}_$dayIndex"
    fun lunchField(dayIndex: Int) = "meal_plan_lunch_$dayIndex"
    fun dinnerField(dayIndex: Int) = "meal_plan_dinner_$dayIndex"
    fun dayHeader(dayIndex: Int) = "meal_plan_day_header_$dayIndex"
    fun groceryButton(dayIndex: Int, slot: MealSlot) =
        "meal_plan_${slot.name.lowercase()}_grocery_$dayIndex"
    fun mealSuggestion(dayIndex: Int, slot: MealSlot, suggestionIndex: Int) =
        "meal_plan_${slot.name.lowercase()}_suggestion_${dayIndex}_$suggestionIndex"
}

@Composable
fun MealPlanDayCard(
    dayIndex: Int,
    dayLabel: String,
    day: DayMeals,
    weekDays: List<DayMeals>,
    isToday: Boolean,
    todayLabel: String,
    lunchLabel: String,
    dinnerLabel: String,
    notPlannedLabel: String,
    unplannedSlotLabel: String,
    expandDayLabel: String,
    collapseDayLabel: String,
    generateGroceryLabel: String,
    copyToTomorrowLabel: String,
    clearFieldLabel: String,
    onLunchChange: (String) -> Unit,
    onDinnerChange: (String) -> Unit,
    onGenerateGroceryForMeal: (MealSlot) -> Unit,
    onCopyToTomorrowLunch: (() -> Unit)? = null,
    onClearLunch: (() -> Unit)? = null,
    onClearDinner: (() -> Unit)? = null,
    suggestQuickMealLabel: String? = null,
    onSuggestQuickMeal: ((MealSlot) -> Unit)? = null,
    loadingMeal: MealSlot? = null,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = isToday,
) {
    var expanded by rememberSaveable(dayIndex) { mutableStateOf(initiallyExpanded) }
    val isPlanned = MealPlanPresentation.isPlanned(day)
    val accentColor = JourneySemanticColors.brandTerracotta()

    FamilyLogisticsCardSurface(
        modifier = modifier,
        accentColor = if (isToday) accentColor else null,
        onClick = if (!isToday) { { expanded = !expanded } } else null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(MealPlanTestTags.dayHeader(dayIndex)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = JourneySemanticColors.inkDeep(),
                    )
                    if (isToday) {
                        TodayBadge(label = todayLabel)
                    }
                }
                if (!isToday) {
                    CollapsibleSectionChevron(
                        expanded = expanded,
                        contentDescription = if (expanded) collapseDayLabel else expandDayLabel,
                    )
                }
            }

            if (!expanded && !isToday) {
                Text(
                    text = MealPlanPresentation.summaryText(
                        day = day,
                        notPlannedLabel = notPlannedLabel,
                        unplannedSlotLabel = unplannedSlotLabel,
                        lunchLabel = lunchLabel,
                        dinnerLabel = dinnerLabel,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPlanned) JourneySemanticColors.inkDeep() else JourneySemanticColors.inkMuted(),
                    fontWeight = if (isPlanned) FontWeight.Medium else FontWeight.Normal,
                )
            }

            AnimatedVisibility(
                visible = expanded || isToday,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MealPlanMealField(
                        value = day.lunch,
                        onValueChange = onLunchChange,
                        label = lunchLabel,
                        slot = MealSlot.Lunch,
                        dayIndex = dayIndex,
                        weekDays = weekDays,
                        accentColor = accentColor,
                        generateGroceryLabel = generateGroceryLabel,
                        onGenerateGrocery = { onGenerateGroceryForMeal(MealSlot.Lunch) },
                        isGeneratingGrocery = loadingMeal == MealSlot.Lunch,
                        readOnly = readOnly,
                        clearFieldLabel = clearFieldLabel,
                        onClear = onClearLunch,
                        suggestQuickMealLabel = suggestQuickMealLabel,
                        onSuggestQuickMeal = onSuggestQuickMeal,
                        modifier = Modifier.fillMaxWidth(),
                        fieldTestTag = MealPlanTestTags.lunchField(dayIndex),
                        generateGroceryTestTag = MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch),
                    )
                    MealPlanMealField(
                        value = day.dinner,
                        onValueChange = onDinnerChange,
                        label = dinnerLabel,
                        slot = MealSlot.Dinner,
                        dayIndex = dayIndex,
                        weekDays = weekDays,
                        accentColor = JourneySemanticColors.brandTeal(),
                        generateGroceryLabel = generateGroceryLabel,
                        onGenerateGrocery = { onGenerateGroceryForMeal(MealSlot.Dinner) },
                        isGeneratingGrocery = loadingMeal == MealSlot.Dinner,
                        readOnly = readOnly,
                        clearFieldLabel = clearFieldLabel,
                        onClear = onClearDinner,
                        copyToTomorrowLabel = copyToTomorrowLabel,
                        onCopyToTomorrow = onCopyToTomorrowLunch,
                        suggestQuickMealLabel = suggestQuickMealLabel,
                        onSuggestQuickMeal = onSuggestQuickMeal,
                        modifier = Modifier.fillMaxWidth(),
                        fieldTestTag = MealPlanTestTags.dinnerField(dayIndex),
                        generateGroceryTestTag = MealPlanTestTags.groceryButton(dayIndex, MealSlot.Dinner),
                    )
                }
            }
        }
    }
}

@Composable
private fun MealPlanMealField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    slot: MealSlot,
    dayIndex: Int,
    weekDays: List<DayMeals>,
    accentColor: androidx.compose.ui.graphics.Color,
    generateGroceryLabel: String,
    onGenerateGrocery: () -> Unit,
    isGeneratingGrocery: Boolean,
    readOnly: Boolean = false,
    clearFieldLabel: String,
    onClear: (() -> Unit)? = null,
    suggestQuickMealLabel: String? = null,
    onSuggestQuickMeal: ((MealSlot) -> Unit)? = null,
    copyToTomorrowLabel: String? = null,
    onCopyToTomorrow: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    fieldTestTag: String,
    generateGroceryTestTag: String,
) {
    val suggestions = if (readOnly) {
        emptyList()
    } else {
        MealPlanPresentation.mealLabelSuggestions(weekDays, value)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        JourneyTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(fieldTestTag),
            label = { Text(label) },
            placeholder = { Text(label) },
            singleLine = false,
            minLines = 2,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { /* focus moves naturally */ }),
            trailingIcon = if (value.isNotBlank() && !readOnly && onClear != null) {
                {
                    JourneyIconButton(onClick = onClear) {
                        Icon(
                            imageVector = AppIcons.Delete,
                            contentDescription = clearFieldLabel,
                        )
                    }
                }
            } else {
                null
            },
            focusAccentColor = accentColor,
        )
        if (value.isBlank() && !readOnly && suggestQuickMealLabel != null && onSuggestQuickMeal != null) {
            AiInlineTriggerButton(
                label = suggestQuickMealLabel,
                onClick = { onSuggestQuickMeal(slot) },
                testTag = MealPlanTestTags.suggestAiButton(dayIndex, slot),
            )
        }
        if (suggestions.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
            ) {
                items(suggestions.withIndex().toList(), key = { it.value }) { (index, suggestion) ->
                    SuggestionChip(
                        onClick = { onValueChange(suggestion) },
                        label = {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = accentColor.copy(alpha = 0.1f),
                            labelColor = accentColor,
                        ),
                        modifier = Modifier.testTag(
                            MealPlanTestTags.mealSuggestion(dayIndex, slot, index),
                        ),
                    )
                }
            }
        }
        if (value.isNotBlank() && !readOnly) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                JourneyPrimaryButton(
                    onClick = onGenerateGrocery,
                    enabled = !isGeneratingGrocery,
                    isLoading = isGeneratingGrocery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(generateGroceryTestTag),
                ) {
                    if (!isGeneratingGrocery) {
                        Icon(
                            imageVector = AppIcons.Sparkles,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(generateGroceryLabel)
                }
                if (onCopyToTomorrow != null && copyToTomorrowLabel != null) {
                    JourneyTertiaryButton(
                        onClick = onCopyToTomorrow,
                        modifier = Modifier.fillMaxWidth(),
                        label = copyToTomorrowLabel,
                    )
                }
            }
        }
    }
}
