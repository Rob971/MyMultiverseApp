package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object HomeComingSoonTestTags {
    const val ROW = "home_coming_soon_row"
}

@Composable
fun HomeComingSoonRow(
    label: String,
    badge: String,
    hint: String? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(0.85f)
            .testTag(HomeComingSoonTestTags.ROW),
        shape = FamilyLogisticsDesign.cardShape,
        color = JourneySemanticColors.cardSurface(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = AppIcons.Explore,
                    contentDescription = null,
                    tint = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp),
                )
                Icon(
                    imageVector = AppIcons.AccountBalance,
                    contentDescription = null,
                    tint = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneySemanticColors.inkMuted(),
                    fontWeight = FontWeight.Medium,
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = JourneySemanticColors.inkMuted().copy(alpha = 0.12f),
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = JourneySemanticColors.inkMuted(),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (hint != null) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneySemanticColors.inkMuted(),
                )
            }
        }
    }
}
