package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatarButton(
    initials: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showPersonFallback: Boolean = initials == "?",
) {
    JourneyIconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        if (showPersonFallback) {
            Icon(
                imageVector = AppIcons.Person,
                contentDescription = contentDescription,
                tint = SharedJourneyColors.MediterraneanTeal,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SharedJourneyColors.MediterraneanTeal),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}
