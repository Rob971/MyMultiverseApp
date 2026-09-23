package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors

@Composable
fun JourneyErrorContent(
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    containerTestTag: String? = null,
    retryButtonTestTag: String? = null,
    secondaryActionTestTag: String? = null,
) {
    Column(
        modifier = modifier.then(
            containerTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = SharedJourneyColors.TerracottaOrange,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        JourneyPrimaryButton(
            onClick = onRetry,
            modifier = Modifier
                .padding(top = 16.dp)
                .then(retryButtonTestTag?.let { Modifier.testTag(it) } ?: Modifier),
        ) {
            Text(
                text = retryLabel,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            TextButton(
                onClick = onSecondaryAction,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .then(secondaryActionTestTag?.let { Modifier.testTag(it) } ?: Modifier),
            ) {
                Text(
                    text = secondaryActionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneySemanticColors.inkMuted(),
                )
            }
        }
    }
}

