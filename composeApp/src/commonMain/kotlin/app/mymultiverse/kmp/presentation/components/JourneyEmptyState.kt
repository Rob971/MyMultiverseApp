package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object JourneyEmptyStateTestTags {
    const val PRIMARY_ACTION = "journey_empty_primary_action"
    const val SECONDARY_ACTION = "journey_empty_secondary_action"
}

@Composable
fun JourneyEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    icon: ImageVector = AppIcons.Restaurant,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionTestTag: String = JourneyEmptyStateTestTags.PRIMARY_ACTION,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    secondaryActionTestTag: String = JourneyEmptyStateTestTags.SECONDARY_ACTION,
    testTag: String? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
        shape = FamilyLogisticsDesign.cardShape,
        color = JourneySemanticColors.cardSurface(),
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
                tint = JourneySemanticColors.inkMuted().copy(alpha = 0.7f),
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = JourneySemanticColors.inkDeep(),
                textAlign = TextAlign.Center,
            )
            if (!body.isNullOrBlank()) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneySemanticColors.inkMuted(),
                    textAlign = TextAlign.Center,
                )
            }
            if (primaryActionLabel != null && onPrimaryAction != null) {
                JourneyPrimaryButton(
                    onClick = onPrimaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(primaryActionTestTag),
                ) {
                    Text(primaryActionLabel)
                }
            }
            if (secondaryActionLabel != null && onSecondaryAction != null) {
                JourneySecondaryButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(secondaryActionTestTag),
                ) {
                    Text(secondaryActionLabel)
                }
            }
        }
    }
}
