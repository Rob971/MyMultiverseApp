package app.mymultiverse.ammo.presentation.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals

class TextFieldInputFiltersTest {

    @Test
    fun filterToDigits_stripsNonDigitsAndKeepsCursorAfterValidPrefix() {
        val input = TextFieldValue(
            text = "12a34",
            selection = TextRange(4),
        )

        val filtered = filterToDigits(input, maxLength = 6)

        assertEquals("1234", filtered.text)
        assertEquals(3, filtered.selection.start)
        assertEquals(3, filtered.selection.end)
    }

    @Test
    fun filterToDigits_pasteWithSeparatorsPlacesCursorAtEndOfDigits() {
        val input = TextFieldValue(
            text = "123 456",
            selection = TextRange(7),
        )

        val filtered = filterToDigits(input, maxLength = 6)

        assertEquals("123456", filtered.text)
        assertEquals(6, filtered.selection.start)
    }

    @Test
    fun filterToDigits_enforcesMaxLength() {
        val input = TextFieldValue(
            text = "1234567890",
            selection = TextRange(10),
        )

        val filtered = filterToDigits(input, maxLength = 6)

        assertEquals("123456", filtered.text)
        assertEquals(6, filtered.selection.start)
    }
}
