package app.mymultiverse.kmp.presentation.screens.home

internal sealed interface HomeGreetingSelection {
    data object Loading : HomeGreetingSelection
    data object Generic : HomeGreetingSelection
    data class Personalized(val name: String) : HomeGreetingSelection

    companion object {
        fun select(greetingReady: Boolean, userDisplayName: String?): HomeGreetingSelection =
            when {
                !greetingReady -> Loading
                userDisplayName != null -> Personalized(userDisplayName)
                else -> Generic
            }
    }
}
