package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors

/**
 * Displays a food emoji inside a rounded tile — used as a leading thumbnail for known grocery
 * items and recognized dishes in the meal plan.
 */
@Composable
fun FoodItemThumbnail(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    val background = JourneySemanticColors.cardSurface()
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.6f).sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
