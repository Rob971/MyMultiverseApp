package app.mymultiverse.kmp.presentation.screens.home

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Documents the banner supporting-line rules used by [HomeContent].
 * Keeps greeting selection testable without a Compose runtime.
 */
class HomeGreetingSelectionTest {

    @Test
    fun selectSupportingLine_showsLoadingWhileGreetingPending() {
        assertEquals(
            HomeGreetingSelection.Loading,
            HomeGreetingSelection.select(greetingReady = false, userDisplayName = "Roberto"),
        )
    }

    @Test
    fun selectSupportingLine_personalizesWhenNameAvailable() {
        assertEquals(
            HomeGreetingSelection.Personalized("Roberto"),
            HomeGreetingSelection.select(greetingReady = true, userDisplayName = "Roberto"),
        )
    }

    @Test
    fun selectSupportingLine_fallsBackToGenericFamilyMessage() {
        assertEquals(
            HomeGreetingSelection.Generic,
            HomeGreetingSelection.select(greetingReady = true, userDisplayName = null),
        )
    }
}
