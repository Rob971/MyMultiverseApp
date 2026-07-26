package app.mymultiverse.ammo.presentation.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Keeps only digits (up to [maxLength]) and maps the cursor/selection so it does not jump when
 * non-digit characters are stripped (paste with spaces, dashes, etc.).
 */
fun filterToDigits(value: TextFieldValue, maxLength: Int): TextFieldValue {
    val filtered = buildString {
        for (character in value.text) {
            if (character.isDigit()) append(character)
        }
    }.take(maxLength)
    val selectionStart = digitIndexBefore(value.text, value.selection.start).coerceIn(0, filtered.length)
    val selectionEnd = digitIndexBefore(value.text, value.selection.end).coerceIn(0, filtered.length)
    return TextFieldValue(
        text = filtered,
        selection = TextRange(selectionStart, selectionEnd),
    )
}

private fun digitIndexBefore(text: String, index: Int): Int {
    val clampedIndex = index.coerceIn(0, text.length)
    var digitCount = 0
    for (position in 0 until clampedIndex) {
        if (text[position].isDigit()) digitCount++
    }
    return digitCount
}
