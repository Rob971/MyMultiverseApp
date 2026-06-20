package app.mymultiverse.kmp.presentation.screens.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeDayPartTest {

    @Test
    fun fromHour_mapsMorningAfternoonAndEvening() {
        assertEquals(HomeDayPart.Morning, HomeDayPart.fromHour(8))
        assertEquals(HomeDayPart.Afternoon, HomeDayPart.fromHour(15))
        assertEquals(HomeDayPart.Evening, HomeDayPart.fromHour(21))
    }
}
