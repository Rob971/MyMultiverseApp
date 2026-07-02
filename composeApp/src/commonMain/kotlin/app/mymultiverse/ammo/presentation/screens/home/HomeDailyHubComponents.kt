package app.mymultiverse.ammo.presentation.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.home.HomeTonightDinner
import app.mymultiverse.ammo.domain.nutrition.NutritionHubSummary
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.components.FamilyLogisticsDesign
import app.mymultiverse.ammo.presentation.components.HomePrimaryActionsTestTags
import app.mymultiverse.ammo.presentation.components.JourneyIcon
import app.mymultiverse.ammo.presentation.components.WeekContextBanner
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.home_daily_meal_plan_title
import ammo.composeapp.generated.resources.home_dashboard_plan_lunch
import ammo.composeapp.generated.resources.home_dashboard_shopping_list
import ammo.composeapp.generated.resources.home_grocery_empty_cta
import ammo.composeapp.generated.resources.home_hero_grocery_list
import ammo.composeapp.generated.resources.home_section_this_week
import ammo.composeapp.generated.resources.home_tonight_dinner_empty
import ammo.composeapp.generated.resources.home_tonight_dinner_title
import ammo.composeapp.generated.resources.nutrition_week_label
import ammo.composeapp.generated.resources.nutrition_grocery_progress
import ammo.composeapp.generated.resources.nutrition_meal_plan_progress
import ammo.composeapp.generated.resources.nutrition_today
import org.jetbrains.compose.resources.stringResource

private enum class HomeDailyFeedTab {
    Today,
    ThisWeek,
}

@Composable
fun HomeDailyHubCircularActions(
    onOpenMealPlan: () -> Unit,
    onOpenGrocery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(HomePrimaryActionsTestTags.ROOT),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        HomeCircularHubCta(
            ringColor = colorScheme.primary,
            containerColor = colorScheme.primaryContainer,
            iconTint = colorScheme.onPrimaryContainer,
            icon = AppIcons.PlanLunchPlaceSetting,
            label = stringResource(Res.string.home_dashboard_plan_lunch),
            onClick = onOpenMealPlan,
            modifier = Modifier.testTag(HomePrimaryActionsTestTags.PLAN),
        )
        HomeCircularHubCta(
            ringColor = colorScheme.secondary,
            containerColor = colorScheme.secondaryContainer,
            iconTint = colorScheme.onSecondaryContainer,
            icon = AppIcons.FreshGroceries,
            label = stringResource(Res.string.home_dashboard_shopping_list),
            onClick = onOpenGrocery,
            modifier = Modifier.testTag(HomePrimaryActionsTestTags.GROCERY),
        )
    }
}

@Composable
private fun HomeCircularHubCta(
    ringColor: Color,
    containerColor: Color,
    iconTint: Color,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(132.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = containerColor,
            border = BorderStroke(2.dp, ringColor.copy(alpha = 0.55f)),
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                JourneyIcon(
                    imageVector = icon,
                    role = AppIconRole.FeatureAccent,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = JourneySemanticColors.inkMuted(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun HomeDailyMealPlanBlock(
    nutritionSummary: HomeNutritionSummary?,
    onOpenMealPlan: () -> Unit,
    onOpenGrocery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(HomeDailyFeedTab.Today) }
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(HomeTestTags.DAILY_MEAL_PLAN_BLOCK),
        shape = FamilyLogisticsDesign.cardShape,
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_daily_meal_plan_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
            )

            HomeDailyFeedTabHeader(
                selectedTab = selectedTab,
                onSelectToday = { selectedTab = HomeDailyFeedTab.Today },
                onSelectThisWeek = { selectedTab = HomeDailyFeedTab.ThisWeek },
            )

            when (selectedTab) {
                HomeDailyFeedTab.Today -> HomeTodayTimelineFeed(
                    tonightsDinner = nutritionSummary?.tonightsDinner,
                    groceryProgress = nutritionSummary?.groceryProgress,
                    onOpenMealPlan = onOpenMealPlan,
                    onOpenGrocery = onOpenGrocery,
                )
                HomeDailyFeedTab.ThisWeek -> HomeThisWeekFeed(
                    nutritionSummary = nutritionSummary,
                )
            }
        }
    }
}

@Composable
private fun HomeDailyFeedTabHeader(
    selectedTab: HomeDailyFeedTab,
    onSelectToday: () -> Unit,
    onSelectThisWeek: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceContainerHighest),
    ) {
        HomeDailyFeedTabChip(
            label = stringResource(Res.string.nutrition_today),
            selected = selectedTab == HomeDailyFeedTab.Today,
            onClick = onSelectToday,
            modifier = Modifier
                .weight(1f)
                .testTag(HomeTestTags.DAILY_TAB_TODAY),
        )
        HomeDailyFeedTabChip(
            label = stringResource(Res.string.home_section_this_week),
            selected = selectedTab == HomeDailyFeedTab.ThisWeek,
            onClick = onSelectThisWeek,
            modifier = Modifier
                .weight(1f)
                .testTag(HomeTestTags.DAILY_TAB_THIS_WEEK),
        )
    }
}

@Composable
private fun HomeDailyFeedTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) {
                    colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    Color.Transparent
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                colorScheme.primary
            } else {
                JourneySemanticColors.inkMuted()
            },
        )
    }
}

@Composable
private fun HomeTodayTimelineFeed(
    tonightsDinner: HomeTonightDinner.State?,
    groceryProgress: NutritionHubSummary.GroceryProgress?,
    onOpenMealPlan: () -> Unit,
    onOpenGrocery: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        when (tonightsDinner) {
            is HomeTonightDinner.State.Planned -> {
                HomeTimelineRow(
                    markerColor = colorScheme.primary,
                    title = tonightsDinner.title,
                    subtitle = stringResource(Res.string.home_tonight_dinner_title),
                    onClick = onOpenMealPlan,
                    titleTestTag = HomeTestTags.TONIGHT_DINNER_MEAL,
                    rowTestTag = HomeTestTags.TONIGHT_DINNER_CARD,
                )
            }
            HomeTonightDinner.State.Unplanned -> {
                HomeTimelineRow(
                    markerColor = colorScheme.primary.copy(alpha = 0.45f),
                    title = stringResource(Res.string.home_tonight_dinner_empty),
                    subtitle = stringResource(Res.string.home_tonight_dinner_title),
                    onClick = onOpenMealPlan,
                    rowTestTag = HomeTestTags.TONIGHT_DINNER_CARD,
                )
            }
            HomeTonightDinner.State.Hidden, null -> Unit
        }

        val grocerySubtitle = groceryProgress?.let { progress ->
            stringResource(
                Res.string.nutrition_grocery_progress,
                progress.checked,
                progress.total,
            )
        } ?: stringResource(Res.string.home_grocery_empty_cta)

        HomeTimelineRow(
            markerColor = colorScheme.secondary,
            title = stringResource(Res.string.home_hero_grocery_list),
            subtitle = grocerySubtitle,
            onClick = onOpenGrocery,
            rowTestTag = HomeTestTags.UPDATE_LIST_ROW,
        )
    }
}

@Composable
private fun HomeThisWeekFeed(nutritionSummary: HomeNutritionSummary?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        nutritionSummary?.weekKey?.takeIf { it.isNotBlank() }?.let { weekKey ->
            WeekContextBanner(
                weekLabel = stringResource(
                    Res.string.nutrition_week_label,
                    WeekCalendar.formatWeekRange(weekKey),
                ),
                modifier = Modifier.testTag(HomeTestTags.WEEK_CONTEXT_BANNER),
            )
        }

        val mealLine = stringResource(
            Res.string.nutrition_meal_plan_progress,
            nutritionSummary?.plannedMealSlots ?: 0,
            NutritionHubSummary.MEAL_SLOTS_PER_WEEK,
        )
        Text(
            text = mealLine,
            style = MaterialTheme.typography.bodyMedium,
            color = JourneySemanticColors.inkDeep(),
            modifier = Modifier.testTag(HomeTestTags.WEEK_MEAL_PROGRESS_LINE),
        )

        nutritionSummary?.groceryProgress?.let { progress ->
            Text(
                text = stringResource(
                    Res.string.nutrition_grocery_progress,
                    progress.checked,
                    progress.total,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = JourneySemanticColors.inkMuted(),
                modifier = Modifier.testTag(HomeTestTags.WEEK_GROCERY_PROGRESS_LINE),
            )
        }
    }
}

@Composable
private fun HomeTimelineRow(
    markerColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleTestTag: String? = null,
    rowTestTag: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .then(if (rowTestTag != null) Modifier.testTag(rowTestTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(markerColor),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = JourneySemanticColors.inkDeep(),
                modifier = if (titleTestTag != null) {
                    Modifier.testTag(titleTestTag)
                } else {
                    Modifier
                },
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = JourneySemanticColors.inkMuted(),
            )
        }
    }
}
