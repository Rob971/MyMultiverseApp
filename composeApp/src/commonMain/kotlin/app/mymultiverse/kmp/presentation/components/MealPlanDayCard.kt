package app.mymultiverse.kmp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object MealPlanTestTags {
    fun lunchField(dayIndex: Int) = "meal_plan_lunch_$dayIndex"
    fun dinnerField(dayIndex: Int) = "meal_plan_dinner_$dayIndex"
    fun dayHeader(dayIndex: Int) = "meal_plan_day_header_$dayIndex"
    fun groceryButton(dayIndex: Int, slot: MealSlot) =
        "meal_plan_${slot.name.lowercase()}_grocery_$dayIndex"
}

@Composable
fun MealPlanDayCard(
    dayIndex: Int,
    dayLabel: String,
    day: DayMeals,
    isToday: Boolean,
    todayLabel: String,
    lunchLabel: String,
    dinnerLabel: String,
    notPlannedLabel: String,
    expandDayLabel: String,
    collapseDayLabel: String,
    generateGroceryLabel: String,
    onLunchChange: (String) -> Unit,
    onDinnerChange: (String) -> Unit,
    onGenerateGroceryForMeal: (MealSlot) -> Unit,
    loadingMeal: MealSlot? = null,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = isToday,
) {
    var expanded by rememberSaveable(dayIndex) { mutableStateOf(initiallyExpanded) }
    val isPlanned = MealPlanPresentation.isPlanned(day)
    val accentColor = SharedJourneyColors.TerracottaOrange

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
                        color = SharedJourneyColors.InkDeep,
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
                    text = MealPlanPresentation.summaryText(day, notPlannedLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPlanned) SharedJourneyColors.InkDeep else SharedJourneyColors.InkMuted,
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
                        accentColor = accentColor,
                        generateGroceryLabel = generateGroceryLabel,
                        onGenerateGrocery = { onGenerateGroceryForMeal(MealSlot.Lunch) },
                        isGeneratingGrocery = loadingMeal == MealSlot.Lunch,
                        readOnly = readOnly,
                        modifier = Modifier.fillMaxWidth(),
                        fieldTestTag = MealPlanTestTags.lunchField(dayIndex),
                        generateGroceryTestTag = MealPlanTestTags.groceryButton(dayIndex, MealSlot.Lunch),
                    )
                    MealPlanMealField(
                        value = day.dinner,
                        onValueChange = onDinnerChange,
                        label = dinnerLabel,
                        accentColor = SharedJourneyColors.MediterraneanTeal,
                        generateGroceryLabel = generateGroceryLabel,
                        onGenerateGrocery = { onGenerateGroceryForMeal(MealSlot.Dinner) },
                        isGeneratingGrocery = loadingMeal == MealSlot.Dinner,
                        readOnly = readOnly,
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
    accentColor: androidx.compose.ui.graphics.Color,
    generateGroceryLabel: String,
    onGenerateGrocery: () -> Unit,
    isGeneratingGrocery: Boolean,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier,
    fieldTestTag: String,
    generateGroceryTestTag: String,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(fieldTestTag),
            label = { Text(label) },
            placeholder = { Text(label) },
            shape = FamilyLogisticsDesign.fieldShape,
            minLines = 2,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { /* focus moves naturally */ }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                focusedLabelColor = accentColor,
                cursorColor = accentColor,
            ),
        )
        if (value.isNotBlank() && !readOnly) {
            TextButton(
                onClick = onGenerateGrocery,
                enabled = !isGeneratingGrocery,
                modifier = Modifier.testTag(generateGroceryTestTag),
            ) {
                if (isGeneratingGrocery) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = SharedJourneyColors.MediterraneanTeal,
                    )
                    Spacer(Modifier.width(8.dp))
                } else {
                    androidx.compose.material3.Icon(
                        imageVector = AppIcons.Sparkles,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SharedJourneyColors.MediterraneanTeal,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = generateGroceryLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = SharedJourneyColors.MediterraneanTeal,
                )
            }
        }
    }
}
