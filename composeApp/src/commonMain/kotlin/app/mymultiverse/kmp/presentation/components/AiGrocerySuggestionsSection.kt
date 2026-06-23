package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object AiGrocerySuggestionsSectionTestTags {
    const val ADOPT_ALL = "grocery_ai_adopt_all"
}

@Composable
fun AiGrocerySuggestionsSection(
    title: String,
    items: List<GroceryItem>,
    adoptAllLabel: String,
    onAdoptAll: () -> Unit,
    onAdopt: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (items.isEmpty()) return

    ColumnSection(
        modifier = modifier,
        title = title,
        adoptAllLabel = adoptAllLabel,
        onAdoptAll = onAdoptAll,
        enabled = enabled,
        items = items,
        onAdopt = onAdopt,
    )
}

@Composable
private fun ColumnSection(
    title: String,
    adoptAllLabel: String,
    onAdoptAll: () -> Unit,
    enabled: Boolean,
    items: List<GroceryItem>,
    onAdopt: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = JourneySemanticColors.inkMuted(),
            )
            TextButton(
                onClick = onAdoptAll,
                enabled = enabled,
                modifier = Modifier.testTag(AiGrocerySuggestionsSectionTestTags.ADOPT_ALL),
            ) {
                Text(
                    text = adoptAllLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = SharedJourneyColors.AiReadOnlyAccent,
                )
            }
        }
        AiGrocerySuggestionChips(
            items = items,
            onAdopt = onAdopt,
            enabled = enabled,
        )
    }
}
