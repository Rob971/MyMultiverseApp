package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.home_hero_family
import ammo.composeapp.generated.resources.home_hero_grocery_list
import ammo.composeapp.generated.resources.home_hero_plan_meals
import org.jetbrains.compose.resources.stringResource

object HomePrimaryActionsTestTags {
    const val ROOT = "home_primary_actions"
    const val PLAN = "home_hero_plan"
    const val GROCERY = "home_hero_grocery"
    const val FAMILY = "home_hero_family"
}

/** @deprecated Replaced by [app.mymultiverse.ammo.presentation.screens.home.HomeDailyHubCircularActions]. */
@Deprecated("Use HomeDailyHubCircularActions on Today dashboard")
@Composable
fun HomePrimaryActions(
    onOpenMealPlan: () -> Unit,
    onOpenGrocery: () -> Unit,
    onOpenFamily: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(HomePrimaryActionsTestTags.ROOT),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        JourneyPrimaryButton(
            onClick = onOpenMealPlan,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomePrimaryActionsTestTags.PLAN),
        ) {
            JourneyButtonLabel(
                text = stringResource(Res.string.home_hero_plan_meals),
                icon = AppIcons.MealPlan,
                role = AppIconRole.OnAccent,
                useContentColor = true,
            )
        }
        JourneySecondaryButton(
            onClick = onOpenGrocery,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomePrimaryActionsTestTags.GROCERY),
        ) {
            JourneyButtonLabel(
                text = stringResource(Res.string.home_hero_grocery_list),
                icon = AppIcons.GroceryList,
                role = AppIconRole.Primary,
                useContentColor = true,
            )
        }
        JourneySecondaryButton(
            onClick = onOpenFamily,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomePrimaryActionsTestTags.FAMILY),
        ) {
            JourneyButtonLabel(
                text = stringResource(Res.string.home_hero_family),
                icon = AppIcons.Household,
                role = AppIconRole.Household,
                useContentColor = true,
            )
        }
    }
}
