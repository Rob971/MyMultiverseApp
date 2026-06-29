package app.mymultiverse.ammo.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

enum class JourneyHapticFeedback {
    LightClick,
}

expect fun performJourneyHaptic(feedback: JourneyHapticFeedback)

fun performJourneyHaptic(feedback: JourneyHapticFeedback, haptic: HapticFeedback) {
    when (feedback) {
        JourneyHapticFeedback.LightClick ->
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
    }
}

@Composable
fun rememberJourneyHapticFeedback(): (JourneyHapticFeedback) -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) {
        { feedback ->
            performJourneyHaptic(feedback, haptic)
            performJourneyHaptic(feedback)
        }
    }
}
