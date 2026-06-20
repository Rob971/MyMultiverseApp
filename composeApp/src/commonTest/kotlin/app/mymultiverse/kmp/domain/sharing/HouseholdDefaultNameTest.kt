package app.mymultiverse.kmp.domain.sharing

import kotlin.test.Test
import kotlin.test.assertEquals

class HouseholdDefaultNameTest {
    @Test
    fun suggest_usesLastNameFromDisplayName() {
        assertEquals(
            "Rossi",
            HouseholdDefaultName.suggest(displayName = "Roberto Rossi", email = "roberto@example.com"),
        )
    }

    @Test
    fun suggest_fallsBackToEmailLocalPart() {
        assertEquals(
            "Maria",
            HouseholdDefaultName.suggest(displayName = null, email = "maria@gmail.com"),
        )
    }

    @Test
    fun suggest_returnsEmptyWhenNoUsableInput() {
        assertEquals("", HouseholdDefaultName.suggest(displayName = null, email = null))
        assertEquals("", HouseholdDefaultName.suggest(displayName = "  ", email = "  "))
    }
}
