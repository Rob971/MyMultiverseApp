package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

@Composable
fun NapolitanBackground(
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Stylized Gulf of Naples (Water)
            val seaPath = Path().apply {
                moveTo(0f, h * 0.85f)
                quadraticTo(w * 0.3f, h * 0.82f, w * 0.5f, h * 0.88f)
                quadraticTo(w * 0.8f, h * 0.92f, w, h * 0.85f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(seaPath, SharedJourneyColors.MediterraneanTeal.copy(alpha = 0.05f))

            // Stylized Mount Vesuvius Silhouette
            val vesuviusPath = Path().apply {
                moveTo(w * 0.4f, h * 0.88f)
                lineTo(w * 0.65f, h * 0.75f) // Main Peak
                lineTo(w * 0.72f, h * 0.78f) // Crater Dip
                lineTo(w * 0.85f, h * 0.82f) // Secondary Peak
                lineTo(w * 1.2f, h * 0.88f)
                close()
            }
            drawPath(vesuviusPath, SharedJourneyColors.TerracottaOrange.copy(alpha = 0.08f))
        }
        content()
    }
}
