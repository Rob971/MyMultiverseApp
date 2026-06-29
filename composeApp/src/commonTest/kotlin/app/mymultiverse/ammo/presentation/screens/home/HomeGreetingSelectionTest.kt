package app.mymultiverse.ammo.presentation.screens.home

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeGreetingSelectionTest {

    @Test
    fun select_personalizesWhenNameAvailable() {
        assertEquals(
            HomeGreetingSelection.Personalized("Roberto", HomeDayPart.Morning),
            HomeGreetingSelection.select(userDisplayName = "Roberto", hour = 9),
        )
    }

    @Test
    fun select_usesAfternoonDayPart() {
        assertEquals(
            HomeGreetingSelection.Personalized("Roberto", HomeDayPart.Afternoon),
            HomeGreetingSelection.select(userDisplayName = "Roberto", hour = 14),
        )
    }

    @Test
    fun select_fallsBackToGenericFamilyMessage() {
        assertEquals(
            HomeGreetingSelection.Generic(HomeDayPart.Evening),
            HomeGreetingSelection.select(userDisplayName = null, hour = 20),
        )
    }
}
