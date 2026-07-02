package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors

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
    nutritionFeature: NutritionFeatureKind? = null,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionIcon: ImageVector? = null,
    primaryActionIconRole: AppIconRole = AppIconRole.OnAccent,
    primaryActionTestTag: String = JourneyEmptyStateTestTags.PRIMARY_ACTION,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    secondaryActionIcon: ImageVector? = null,
    secondaryActionIconRole: AppIconRole = AppIconRole.Primary,
    secondaryActionTestTag: String = JourneyEmptyStateTestTags.SECONDARY_ACTION,
    testTag: String? = null,
    extraContent: @Composable (() -> Unit)? = null,
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
            when (val feature = nutritionFeature) {
                null -> JourneyIcon(
                    imageVector = icon,
                    role = AppIconRole.Muted,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
                else -> NutritionFeatureArt(
                    feature = feature,
                    contentDescription = title,
                    modifier = Modifier.size(72.dp),
                )
            }
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
                    JourneyButtonLabel(
                        text = primaryActionLabel,
                        icon = primaryActionIcon ?: icon,
                        role = primaryActionIconRole,
                        useContentColor = true,
                    )
                }
            }
            if (secondaryActionLabel != null && onSecondaryAction != null) {
                JourneySecondaryButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(secondaryActionTestTag),
                ) {
                    JourneyButtonLabel(
                        text = secondaryActionLabel,
                        icon = secondaryActionIcon,
                        role = secondaryActionIconRole,
                        useContentColor = true,
                    )
                }
            }
            extraContent?.invoke()
        }
    }
}
