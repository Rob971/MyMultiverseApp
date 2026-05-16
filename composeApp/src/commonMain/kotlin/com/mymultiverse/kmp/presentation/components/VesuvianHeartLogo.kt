package com.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@Composable
fun VesuvianHeartLogo(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Sun/Lemon Glow
            drawCircle(
                color = SharedJourneyColors.LemonZestYellow.copy(alpha = 0.3f),
                radius = 12.dp.toPx(),
                center = Offset(w * 0.85f, h * 0.15f)
            )

            // 2. The 'Napolitan Heart' Base
            val heartPath = Path().apply {
                moveTo(w / 2, h * 0.9f)
                // Left side
                cubicTo(w * 0.1f, h * 0.7f, w * 0.05f, h * 0.25f, w / 2, h * 0.25f)
                // Right side
                cubicTo(w * 0.95f, h * 0.25f, w * 0.9f, h * 0.7f, w / 2, h * 0.9f)
                close()
            }
            drawPath(heartPath, SharedJourneyColors.TerracottaOrange)

            // 3. Stylized Vesuvius Silhouette (White 'Snow' cap or just silhouette)
            val vPath = Path().apply {
                moveTo(w * 0.32f, h * 0.58f)
                lineTo(w * 0.46f, h * 0.42f) // Peak 1
                lineTo(w * 0.52f, h * 0.46f) // Crater Dip
                lineTo(w * 0.68f, h * 0.42f) // Peak 2
                lineTo(w * 0.82f, h * 0.58f)
                close()
            }
            drawPath(vPath, Color.White.copy(alpha = 0.85f))
            
            // 4. Little Mediterranean Teal accent (The Sea spark)
            drawCircle(
                color = SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.6f),
                radius = 3.dp.toPx(),
                center = Offset(w * 0.5f, h * 0.75f)
            )
        }
    }
}
