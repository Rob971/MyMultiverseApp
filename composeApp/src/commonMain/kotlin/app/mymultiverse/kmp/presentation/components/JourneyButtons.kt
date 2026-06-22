package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@Composable
fun JourneyPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = SharedJourneyColors.MediterraneanTeal,
            contentColor = Color.White,
            disabledContainerColor = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
        content = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                content()
            }
        },
    )
}

@Composable
fun JourneySecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(1.dp, SharedJourneyColors.MediterraneanTeal),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SharedJourneyColors.MediterraneanTeal,
        ),
        content = content,
    )
}

@Composable
fun JourneyTertiaryButton(
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = SharedJourneyColors.MediterraneanTeal,
        )
    }
}

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
