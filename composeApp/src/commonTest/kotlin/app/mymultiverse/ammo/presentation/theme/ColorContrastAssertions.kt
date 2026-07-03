package app.mymultiverse.ammo.presentation.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.test.assertTrue

internal object ColorContrastAssertions {
    fun contrastRatio(foreground: Color, background: Color): Double {
        val foregroundLuminance = relativeLuminance(foreground)
        val backgroundLuminance = relativeLuminance(background)
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    fun assertAaTextContrast(
        foreground: Color,
        background: Color,
        label: String,
        minRatio: Double = 4.5,
    ) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            ratio >= minRatio,
            "$label contrast ratio %.2f is below %.2f".format(ratio, minRatio),
        )
    }

    fun assertAaNonTextContrast(
        foreground: Color,
        background: Color,
        label: String,
        minRatio: Double = 3.0,
    ) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            ratio >= minRatio,
            "$label contrast ratio %.2f is below %.2f".format(ratio, minRatio),
        )
    }

    private fun relativeLuminance(color: Color): Double {
        fun linearize(channel: Float): Double {
            val normalized = channel.toDouble()
            return if (normalized <= 0.03928) {
                normalized / 12.92
            } else {
                ((normalized + 0.055) / 1.055).pow(2.4)
            }
        }

        val r = linearize(color.red)
        val g = linearize(color.green)
        val b = linearize(color.blue)
        return (0.2126 * r) + (0.7152 * g) + (0.0722 * b)
    }
}
