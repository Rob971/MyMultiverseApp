package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors

@Composable
fun JourneyLoadingContent(
    message: String,
    modifier: Modifier = Modifier,
    containerTestTag: String? = null,
) {
    Column(
        modifier = modifier.then(
            containerTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = JourneySemanticColors.brandTeal())
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = JourneySemanticColors.inkMuted(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

