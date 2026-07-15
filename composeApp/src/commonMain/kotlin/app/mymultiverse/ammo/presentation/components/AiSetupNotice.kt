package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_ai_setup_notice
import org.jetbrains.compose.resources.stringResource

object AiSetupNoticeTestTags {
    const val BANNER = "ai_setup_notice_banner"
    const val DISMISS = "ai_setup_notice_dismiss"
}

/**
 * Inline, dismissable notice shown on the meal plan screen when no Gemini API key is
 * configured. Points users to Account & settings with a Sparkles icon for visual context.
 *
 * The notice disappears automatically once the user saves a key (reactive [StateFlow])
 * and can be manually dismissed without saving a key — the dismissed state is held for
 * the current session only.
 *
 * @param onDismiss Called when the user taps the dismiss (×) icon. The caller is
 *   responsible for hiding the notice until the session ends.
 */
@Composable
fun AiSetupNotice(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AiSetupNoticeTestTags.BANNER),
        color = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.10f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            JourneyIcon(
                role = AppIconRole.AiAccent,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(Res.string.nutrition_ai_setup_notice),
                style = MaterialTheme.typography.bodySmall,
                color = JourneySemanticColors.inkDeep(),
                modifier = Modifier.weight(1f),
            )
            JourneyIconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(AiSetupNoticeTestTags.DISMISS),
            ) {
                JourneyIcon(
                    role = AppIconRole.ChromeClose,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
