package app.mymultiverse.kmp.presentation.screens.home

internal enum class HomeDayPart {
    Morning,
    Afternoon,
    Evening,
    ;

    companion object {
        fun fromHour(hour: Int): HomeDayPart =
            when (hour) {
                in 5..11 -> Morning
                in 12..17 -> Afternoon
                else -> Evening
            }
    }
}
