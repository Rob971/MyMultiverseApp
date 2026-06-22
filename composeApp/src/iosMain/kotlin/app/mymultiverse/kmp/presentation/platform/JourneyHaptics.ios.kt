package app.mymultiverse.kmp.presentation.platform

actual fun performJourneyHaptic(feedback: JourneyHapticFeedback) {
    // No-op until UIKit haptics are wired; composable path uses LocalHapticFeedback when available.
}
