package app.mymultiverse.kmp.presentation.platform

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual fun performJourneyHaptic(feedback: JourneyHapticFeedback) {
    when (feedback) {
        JourneyHapticFeedback.LightClick -> {
            val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
            generator.prepare()
            generator.impactOccurred()
        }
    }
}
