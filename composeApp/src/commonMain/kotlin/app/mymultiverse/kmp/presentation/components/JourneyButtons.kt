package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@Composable
fun JourneyDestructiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(1.dp, SharedJourneyColors.TerracottaOrange),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SharedJourneyColors.TerracottaOrange,
        ),
        content = content,
    )
}

@Composable
fun JourneyDestructiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(label, color = SharedJourneyColors.TerracottaOrange)
    }
}
