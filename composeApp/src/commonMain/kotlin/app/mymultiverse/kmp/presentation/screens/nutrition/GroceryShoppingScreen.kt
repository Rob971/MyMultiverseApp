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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_add_button
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_add_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
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

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_grocery_title),
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
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                    )
                    Button(
                        onClick = {
                            screenModel.addGroceryItem(newItemText)
                            newItemText = ""
                        },
                        enabled = newItemText.isNotBlank(),
                    ) {
                        Text(stringResource(Res.string.nutrition_grocery_add_button))
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Text(
                        text = stringResource(Res.string.nutrition_grocery_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SharedJourneyColors.InkMuted,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    GroceryItemRow(
                        item = item,
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
    onToggle: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SharedJourneyColors.GlassWhite,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SharedJourneyColors.SageSoft,
                    checkmarkColor = SharedJourneyColors.InkDeep,
                ),
            )
            Text(
                text = item.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = SharedJourneyColors.InkDeep,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = null,
                    tint = SharedJourneyColors.TerracottaOrange,
                )
            }
        }
    }
}
