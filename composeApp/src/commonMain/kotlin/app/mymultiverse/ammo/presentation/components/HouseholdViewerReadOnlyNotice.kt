package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_household_viewer_readonly_note
import org.jetbrains.compose.resources.stringResource

object HouseholdViewerReadOnlyTestTags {
    const val BANNER = "household_viewer_readonly_banner"
}

@Composable
fun HouseholdViewerReadOnlyNotice(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(HouseholdViewerReadOnlyTestTags.BANNER),
        color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = stringResource(Res.string.nutrition_household_viewer_readonly_note),
            style = MaterialTheme.typography.bodyMedium,
            color = JourneySemanticColors.inkDeep(),
            modifier = Modifier.padding(12.dp),
        )
    }
}
