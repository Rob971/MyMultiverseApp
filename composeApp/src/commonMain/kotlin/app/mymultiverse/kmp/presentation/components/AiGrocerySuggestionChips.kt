package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.presentation.theme.AppIconRole
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors

object AiGrocerySuggestionChipsTestTags {
    const val ROW = "grocery_ai_suggestion_chips"
    fun chip(id: String) = "grocery_ai_chip_$id"
}

@Composable
fun AiGrocerySuggestionChips(
    items: List<GroceryItem>,
    onAdopt: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (items.isEmpty()) return

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AiGrocerySuggestionChipsTestTags.ROW),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        items(items, key = { it.id }) { item ->
            SuggestionChip(
                onClick = { onAdopt(item) },
                enabled = enabled,
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                icon = {
                    JourneyIcon(
                        role = AppIconRole.ActionAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        useContentColor = true,
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = JourneySemanticColors.brandTerracotta().copy(alpha = 0.12f),
                    labelColor = JourneySemanticColors.brandTerracotta(),
                    iconContentColor = JourneySemanticColors.brandTerracotta(),
                ),
                modifier = Modifier
                    .animateItem()
                    .testTag(AiGrocerySuggestionChipsTestTags.chip(item.id)),
            )
        }
    }
}
