package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@Composable
fun WeekContextBanner(
    weekLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, SharedJourneyColors.InkMuted.copy(alpha = 0.12f)),
    ) {
        Text(
            text = weekLabel,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = SharedJourneyColors.InkDeep,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun GroceryDashboardCard(
    weekLabel: String,
    description: String,
    progressLabel: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FamilyLogisticsDesign.cardShape,
        color = SharedJourneyColors.SunDrenchedWhite,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = weekLabel,
                style = MaterialTheme.typography.titleSmall,
                color = SharedJourneyColors.InkDeep,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SharedJourneyColors.InkMuted,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            Text(
                text = progressLabel,
                style = MaterialTheme.typography.labelMedium,
                color = SharedJourneyColors.InkMuted,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun NutritionProgressChip(
    label: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SharedJourneyColors.InkMuted,
            fontWeight = FontWeight.Medium,
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = accentColor,
            trackColor = accentColor.copy(alpha = 0.2f),
        )
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FamilyLogisticsDesign.cardShape,
        color = SharedJourneyColors.GlassWhite,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SharedJourneyColors.InkMuted.copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SharedJourneyColors.InkMuted,
            )
        }
    }
}

@Composable
fun TodayBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.15f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = SharedJourneyColors.TerracottaOrange,
            fontWeight = FontWeight.Bold,
        )
    }
}
