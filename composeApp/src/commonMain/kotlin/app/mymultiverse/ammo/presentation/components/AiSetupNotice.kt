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
import ammo.composeapp.generated.resources.nutrition_ai_setup_notice
import org.jetbrains.compose.resources.stringResource

object AiSetupNoticeTestTags {
    const val BANNER = "ai_setup_notice_banner"
}

/**
 * Inline notice shown on the meal plan screen when no Gemini API key is configured.
 * Explains that the user can set up AI-powered ingredient generation in Account settings.
 */
@Composable
fun AiSetupNotice(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AiSetupNoticeTestTags.BANNER),
        color = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.10f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = stringResource(Res.string.nutrition_ai_setup_notice),
            style = MaterialTheme.typography.bodySmall,
            color = JourneySemanticColors.inkDeep(),
            modifier = Modifier.padding(12.dp),
        )
    }
}
