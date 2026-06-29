package app.mymultiverse.ammo.presentation.screens.home

import app.mymultiverse.ammo.domain.model.Greeting
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeInspirationLineTest {

    @Test
    fun select_showsLoadingWhenGreetingPending() {
        assertEquals(
            HomeInspirationLine.Loading,
            HomeInspirationLine.select(greeting = null),
        )
    }

    @Test
    fun select_showsGreetingTextWhenReady() {
        assertEquals(
            HomeInspirationLine.Ready("Plan meals together."),
            HomeInspirationLine.select(greeting = Greeting("Plan meals together.")),
        )
    }
}
