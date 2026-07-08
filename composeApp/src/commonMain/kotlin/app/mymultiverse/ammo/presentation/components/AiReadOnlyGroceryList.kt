package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors

@Composable
fun AiReadOnlyGroceryList(
    items: List<GroceryItem>,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val aiAccent = JourneySemanticColors.brandTerracotta()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
    ) {
        FamilyLogisticsSectionHeader(title = title)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = JourneySemanticColors.inkMuted(),
            modifier = Modifier.padding(bottom = 4.dp),
        )
        items.forEach { item ->
            val foodEmoji = FoodEmojiCatalog.emojiForGroceryLabel(item.label)
            FamilyLogisticsCardSurface(accentColor = aiAccent) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (foodEmoji != null) {
                        FoodItemThumbnail(emoji = foodEmoji, size = 32.dp)
                    } else {
                        JourneyIcon(
                            role = AppIconRole.AiAccent,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = JourneySemanticColors.inkDeep(),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
