package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIconRole
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@Composable
fun RowScope.JourneyButtonLabel(
    text: String,
    icon: ImageVector? = null,
    role: AppIconRole = AppIconRole.Primary,
    useContentColor: Boolean = true,
) {
    if (icon != null) {
        JourneyIcon(
            imageVector = icon,
            role = role,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            useContentColor = useContentColor,
        )
        Spacer(Modifier.width(8.dp))
    }
    Text(text)
}

@Composable
fun RowScope.JourneySsoButtonLabel(
    text: String,
    provider: AppIconRole,
    useContentColor: Boolean = true,
) {
    JourneyIcon(
        role = provider,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        useContentColor = useContentColor,
    )
    Spacer(Modifier.width(10.dp))
    Text(text)
}

@Composable
fun JourneyPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val brandTeal = JourneySemanticColors.brandTeal()
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = brandTeal,
            contentColor = Color.White,
            disabledContainerColor = brandTeal.copy(alpha = 0.4f),
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
    val accent = JourneySemanticColors.brandSecondaryAccent()
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(1.dp, accent),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = accent,
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
            color = JourneySemanticColors.brandTeal(),
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
    val accent = JourneySemanticColors.brandTerracotta()
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = BorderStroke(1.dp, accent),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = accent,
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
