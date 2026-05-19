package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_delete_item
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_add_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_title
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.EmptyStateCard
import app.mymultiverse.kmp.presentation.components.NutritionProgressChip
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import org.koin.compose.koinInject

@Composable
fun GroceryShoppingScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val items by screenModel.groceryItems.collectAsState()
    var newItemText by rememberSaveable { mutableStateOf("") }
    val weekSubtitle = WeekCalendar.formatWeekRange(screenModel.weekKey)
    val checkedCount = items.count { it.isChecked }
    val deleteLabel = stringResource(Res.string.nutrition_delete_item)

    fun submitNewItem() {
        if (newItemText.isNotBlank()) {
            screenModel.addGroceryItem(newItemText)
            newItemText = ""
        }
    }

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_grocery_title),
        subtitle = weekSubtitle,
        onBack = onBack,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SharedJourneyColors.GlassWhite,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(Res.string.nutrition_grocery_add_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitNewItem() }),
                    )
                    FilledIconButton(
                        onClick = { submitNewItem() },
                        enabled = newItemText.isNotBlank(),
                    ) {
                        Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = stringResource(Res.string.nutrition_grocery_add_hint),
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (items.isNotEmpty()) {
                item {
                    NutritionProgressChip(
                        label = stringResource(
                            Res.string.nutrition_grocery_progress,
                            checkedCount,
                            items.size,
                        ),
                        progress = checkedCount.toFloat() / items.size,
                        accentColor = SharedJourneyColors.SageSoft,
                    )
                }
            }

            if (items.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = stringResource(Res.string.nutrition_grocery_empty),
                        icon = AppIcons.Restaurant,
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    GroceryItemRow(
                        item = item,
                        deleteLabel = deleteLabel,
                        onToggle = { screenModel.toggleGroceryItem(item.id) },
                        onRemove = { screenModel.removeGroceryItem(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GroceryItemRow(
    item: GroceryItem,
    deleteLabel: String,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(20.dp),
        color = SharedJourneyColors.GlassWhite,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.isChecked) {
                    SharedJourneyColors.InkMuted
                } else {
                    SharedJourneyColors.InkDeep
                },
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
            )
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = SharedJourneyColors.TerracottaOrange,
                ),
            ) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = deleteLabel,
                )
            }
        }
    }
}
