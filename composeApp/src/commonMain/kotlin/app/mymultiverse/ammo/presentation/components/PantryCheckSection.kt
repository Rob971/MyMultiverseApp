package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors

object PantryCheckSectionTestTags {
    const val ROOT = "grocery_pantry_check_section"
    const val ROW = "grocery_pantry_check_chips"
    const val ADOPT_REMAINING = "grocery_pantry_adopt_remaining"
    fun chip(id: String) = "grocery_pantry_chip_$id"
}

@Composable
fun PantryCheckSection(
    title: String,
    subtitle: String,
    items: List<GroceryItem>,
    onMarkHave: (GroceryItem) -> Unit,
    adoptRemainingLabel: String?,
    onAdoptRemaining: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val visible = items.filterNot { it.isChecked }
    if (visible.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(PantryCheckSectionTestTags.ROOT),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = JourneySemanticColors.inkMuted(),
            modifier = Modifier.padding(top = 4.dp, start = 2.dp),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = JourneySemanticColors.inkMuted(),
            modifier = Modifier.padding(start = 2.dp),
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PantryCheckSectionTestTags.ROW),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            items(visible, key = { it.id }) { item ->
                SuggestionChip(
                    onClick = { onMarkHave(item) },
                    enabled = enabled,
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    icon = {
                        JourneyIcon(
                            role = AppIconRole.PantryHave,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            useContentColor = true,
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = JourneySemanticColors.brandTerracotta().copy(alpha = 0.08f),
                        labelColor = JourneySemanticColors.brandTerracotta(),
                        iconContentColor = JourneySemanticColors.brandTerracotta(),
                    ),
                    modifier = Modifier
                        .animateItem()
                        .testTag(PantryCheckSectionTestTags.chip(item.id)),
                )
            }
        }
        if (adoptRemainingLabel != null && enabled) {
            JourneyTertiaryButton(
                onClick = onAdoptRemaining,
                label = adoptRemainingLabel,
                modifier = Modifier.testTag(PantryCheckSectionTestTags.ADOPT_REMAINING),
            )
        }
    }
}
