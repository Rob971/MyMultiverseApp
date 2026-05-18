package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Large, tactile progress ring — grounded organic feel (Option 2).
 */
@Composable
fun FriendlyProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 88.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color,
    progressColor: Color,
) {
    val p = progress.coerceIn(0f, 1f)
    Canvas(modifier = modifier.size(ringSize)) {
        val stroke = strokeWidth.toPx()
        val pad = stroke / 2f + 2f
        val dim = min(this.size.width, this.size.height) - pad * 2f
        val topLeft = Offset(pad, pad)
        val arcSize = Size(dim, dim)
        val sweep = 360f * p
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        if (p > 0f) {
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}
