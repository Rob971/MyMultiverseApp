package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_hero_family
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_hero_grocery_list
import kmpvoyagercleanarchitecture.composeapp.generated.resources.home_hero_plan_meals
import org.jetbrains.compose.resources.stringResource

object HomePrimaryActionsTestTags {
    const val ROOT = "home_primary_actions"
    const val PLAN = "home_hero_plan"
    const val GROCERY = "home_hero_grocery"
    const val FAMILY = "home_hero_family"
}

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
            Text(stringResource(Res.string.home_hero_plan_meals))
        }
        JourneySecondaryButton(
            onClick = onOpenGrocery,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomePrimaryActionsTestTags.GROCERY),
        ) {
            Text(stringResource(Res.string.home_hero_grocery_list))
        }
        JourneySecondaryButton(
            onClick = onOpenFamily,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(HomePrimaryActionsTestTags.FAMILY),
        ) {
            Text(stringResource(Res.string.home_hero_family))
        }
    }
}
