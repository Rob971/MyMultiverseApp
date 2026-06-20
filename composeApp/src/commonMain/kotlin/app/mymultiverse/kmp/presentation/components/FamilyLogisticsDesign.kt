package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

/** Shared visual language for home dashboard and nutrition flows. */
object FamilyLogisticsDesign {
    val cardCornerRadius = 24.dp
    val bannerCornerRadius = 32.dp
    val fieldCornerRadius = 16.dp
    val iconCircleSize = 52.dp
    val iconSize = 28.dp

    val cardShape = RoundedCornerShape(cardCornerRadius)
    val bannerShape = RoundedCornerShape(bannerCornerRadius)
    val fieldShape = RoundedCornerShape(fieldCornerRadius)
}

@Composable
fun FamilyLogisticsCardSurface(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    val border = accentColor?.let {
        BorderStroke(2.dp, it.copy(alpha = if (onClick != null) 0.45f else 0.35f))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier),
        shape = FamilyLogisticsDesign.cardShape,
        color = SharedJourneyColors.GlassWhite,
        border = border,
    ) {
        content()
    }
}

@Composable
fun FamilyLogisticsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionModifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null,
) {
    val clickableTitleModifier = if (onTitleClick != null) {
        titleModifier.clickable(onClick = onTitleClick)
    } else {
        titleModifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = clickableTitleModifier,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = SharedJourneyColors.InkDeep,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                modifier = actionModifier.clickable(onClick = onAction),
                style = MaterialTheme.typography.labelLarge,
                color = SharedJourneyColors.TerracottaOrange,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun NutritionFeatureHeader(
    weekLabel: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    progressLabel: String,
    progress: Float,
    modifier: Modifier = Modifier,
    canGoToPreviousWeek: Boolean = false,
    canGoToNextWeek: Boolean = false,
    previousWeekLabel: String? = null,
    nextWeekLabel: String? = null,
    onPreviousWeek: (() -> Unit)? = null,
    onNextWeek: (() -> Unit)? = null,
    planWithAiLabel: String? = null,
    onPlanWithAi: (() -> Unit)? = null,
    showPlanWithAi: Boolean = false,
    planWithAiTestTag: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ScreenLayout.sectionSpacing),
    ) {
        if (
            onPreviousWeek != null &&
            onNextWeek != null &&
            previousWeekLabel != null &&
            nextWeekLabel != null
        ) {
            WeekSelectorBanner(
                weekLabel = weekLabel,
                canGoToPreviousWeek = canGoToPreviousWeek,
                canGoToNextWeek = canGoToNextWeek,
                previousWeekLabel = previousWeekLabel,
                nextWeekLabel = nextWeekLabel,
                onPreviousWeek = onPreviousWeek,
                onNextWeek = onNextWeek,
            )
        } else {
            WeekContextBanner(weekLabel = weekLabel)
        }

        FamilyLogisticsCardSurface(accentColor = accentColor) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(FamilyLogisticsDesign.iconCircleSize)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(FamilyLogisticsDesign.iconSize),
                    )
                }
                Text(
                    text = description,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SharedJourneyColors.InkMuted,
                )
            }
        }

        NutritionProgressChip(
            label = progressLabel,
            progress = progress,
            accentColor = accentColor,
        )

        if (showPlanWithAi && planWithAiLabel != null && onPlanWithAi != null) {
            OutlinedButton(
                onClick = onPlanWithAi,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (planWithAiTestTag != null) {
                            Modifier.testTag(planWithAiTestTag)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Text(planWithAiLabel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureAccentIconButton(
    onClick: () -> Unit,
    enabled: Boolean,
    accentColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = if (enabled) accentColor else accentColor.copy(alpha = 0.35f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
fun CollapsibleSectionChevron(
    expanded: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = if (expanded) AppIcons.KeyboardArrowUp else AppIcons.KeyboardArrowDown,
        contentDescription = contentDescription,
        tint = SharedJourneyColors.InkMuted,
        modifier = modifier.size(24.dp),
    )
}
