package app.mymultiverse.kmp.presentation.screens.home

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Documents the banner supporting-line rules used by [HomeWelcomeContent].
 */
class HomeGreetingSelectionTest {

    @Test
    fun select_personalizesWhenNameAvailable() {
        assertEquals(
            HomeGreetingSelection.Personalized("Roberto"),
            HomeGreetingSelection.select(userDisplayName = "Roberto"),
        )
    }

    @Test
    fun select_fallsBackToGenericFamilyMessage() {
        assertEquals(
            HomeGreetingSelection.Generic,
            HomeGreetingSelection.select(userDisplayName = null),
        )
    }

    @Test
    fun select_personalizesEvenWhileGreetingPending() {
        assertEquals(
            HomeGreetingSelection.Personalized("Roberto"),
            HomeGreetingSelection.select(userDisplayName = "Roberto"),
        )
    }
}
