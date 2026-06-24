package app.mymultiverse.kmp.presentation.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIconRole
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors

object GroceryGhostPairingTestTags {
    const val ROOT = "grocery_ghost_pairing_banner"
    const val ACTION = "grocery_ghost_pairing_add"
    const val DISMISS = "grocery_ghost_pairing_dismiss"
}

@Composable
fun GroceryGhostPairingBanner(
    title: String,
    actionLabel: String,
    dismissLabel: String,
    onAddSuggestions: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FamilyLogisticsCardSurface(
        modifier = modifier.testTag(GroceryGhostPairingTestTags.ROOT),
        accentColor = JourneySemanticColors.brandTerracotta(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    JourneyIcon(
                        role = AppIconRole.AiAccent,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = JourneySemanticColors.inkDeep(),
                    )
                }
                JourneyIconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .testTag(GroceryGhostPairingTestTags.DISMISS)
                        .semantics { contentDescription = dismissLabel },
                ) {
                    JourneyIcon(
                        role = AppIconRole.ChromeClose,
                        contentDescription = null,
                    )
                }
            }
            JourneyPrimaryButton(
                onClick = onAddSuggestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(GroceryGhostPairingTestTags.ACTION),
            ) {
                JourneyButtonLabel(
                    text = actionLabel,
                    icon = AppIcons.GroceryList,
                    role = AppIconRole.OnAccent,
                    useContentColor = true,
                )
            }
        }
    }
}
