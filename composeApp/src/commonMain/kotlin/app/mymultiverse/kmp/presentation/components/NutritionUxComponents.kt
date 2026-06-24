package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIconRole
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors

object WeekSelectorTestTags {
    const val PREVIOUS = "nutrition_week_previous"
    const val NEXT = "nutrition_week_next"
}

@Composable
fun WeekSelectorBanner(
    weekLabel: String,
    canGoToPreviousWeek: Boolean,
    canGoToNextWeek: Boolean,
    previousWeekLabel: String,
    nextWeekLabel: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brandTeal = JourneySemanticColors.brandTeal()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = JourneySemanticColors.brandTealContainer(),
        border = BorderStroke(1.dp, JourneySemanticColors.subtleBorder()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            JourneyIconButton(
                onClick = onPreviousWeek,
                enabled = canGoToPreviousWeek,
                modifier = Modifier.testTag(WeekSelectorTestTags.PREVIOUS),
            ) {
                JourneyIcon(
                    role = AppIconRole.ChromeChevronLeft,
                    contentDescription = previousWeekLabel,
                    tint = if (canGoToPreviousWeek) {
                        brandTeal
                    } else {
                        JourneySemanticColors.inkMuted().copy(alpha = 0.4f)
                    },
                )
            }
            Text(
                text = weekLabel,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = JourneySemanticColors.inkDeep(),
                fontWeight = FontWeight.SemiBold,
            )
            JourneyIconButton(
                onClick = onNextWeek,
                enabled = canGoToNextWeek,
                modifier = Modifier.testTag(WeekSelectorTestTags.NEXT),
            ) {
                JourneyIcon(
                    role = AppIconRole.ChromeChevronRight,
                    contentDescription = nextWeekLabel,
                    tint = if (canGoToNextWeek) {
                        brandTeal
                    } else {
                        JourneySemanticColors.inkMuted().copy(alpha = 0.4f)
                    },
                )
            }
        }
    }
}

@Composable
fun WeekContextBanner(
    weekLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = JourneySemanticColors.brandTealContainer(),
        border = BorderStroke(1.dp, JourneySemanticColors.subtleBorder()),
    ) {
        Text(
            text = weekLabel,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = JourneySemanticColors.inkDeep(),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun GroceryDashboardCard(
    description: String,
    progressLabel: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
    weekLabel: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FamilyLogisticsDesign.cardShape,
        color = JourneySemanticColors.cardSurface(),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (weekLabel != null) {
                Text(
                    text = weekLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = JourneySemanticColors.inkDeep(),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = JourneySemanticColors.inkSecondary(),
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
                color = JourneySemanticColors.inkMuted(),
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
            color = JourneySemanticColors.inkMuted(),
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
    JourneyEmptyState(
        title = message,
        icon = icon,
        modifier = modifier,
    )
}

@Composable
fun TodayBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    val accent = JourneySemanticColors.brandTerracotta()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = accent.copy(alpha = 0.22f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
    }
}
