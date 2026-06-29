package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors

object HomeComingSoonTestTags {
    const val ROW = "home_coming_soon_row"
    const val ADVENTURES = "home_coming_soon_adventures"
    const val BUDGET = "home_coming_soon_budget"
}

@Composable
fun HomeComingSoonRow(
    label: String,
    badge: String,
    adventuresLabel: String,
    budgetLabel: String,
    hint: String? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(0.92f)
            .testTag(HomeComingSoonTestTags.ROW),
        shape = FamilyLogisticsDesign.cardShape,
        color = JourneySemanticColors.cardSurface(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneySemanticColors.inkDeep(),
                    fontWeight = FontWeight.SemiBold,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ComingSoonFeatureChip(
                    role = AppIconRole.ComingSoonExplore,
                    label = adventuresLabel,
                    modifier = Modifier.testTag(HomeComingSoonTestTags.ADVENTURES),
                )
                ComingSoonFeatureChip(
                    role = AppIconRole.ComingSoonBudget,
                    label = budgetLabel,
                    modifier = Modifier.testTag(HomeComingSoonTestTags.BUDGET),
                )
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

@Composable
private fun ComingSoonFeatureChip(
    role: AppIconRole,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JourneyIcon(
            role = role,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = JourneySemanticColors.inkSecondary(),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
