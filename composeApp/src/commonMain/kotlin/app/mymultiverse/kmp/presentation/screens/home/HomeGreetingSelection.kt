package app.mymultiverse.kmp.presentation.screens.home

internal sealed interface HomeGreetingSelection {
    data class Personalized(val name: String, val dayPart: HomeDayPart) : HomeGreetingSelection
    data class Generic(val dayPart: HomeDayPart) : HomeGreetingSelection

    companion object {
        fun select(userDisplayName: String?, hour: Int): HomeGreetingSelection {
            val dayPart = HomeDayPart.fromHour(hour)
            return when {
                userDisplayName != null -> Personalized(userDisplayName, dayPart)
                else -> Generic(dayPart)
            }
        }
    }
}
