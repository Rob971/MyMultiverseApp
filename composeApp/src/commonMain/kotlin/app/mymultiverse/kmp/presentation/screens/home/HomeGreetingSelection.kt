package app.mymultiverse.kmp.presentation.screens.home

internal sealed interface HomeGreetingSelection {
    data object Generic : HomeGreetingSelection
    data class Personalized(val name: String) : HomeGreetingSelection

    companion object {
        fun select(userDisplayName: String?): HomeGreetingSelection =
            when {
                userDisplayName != null -> Personalized(userDisplayName)
                else -> Generic
            }
    }
}
