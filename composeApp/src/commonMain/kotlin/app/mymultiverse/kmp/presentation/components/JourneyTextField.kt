package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object JourneyTextFieldDefaults {
    val fieldSpacing = 16.dp
    val minHeight = 56.dp

    @Composable
    fun colors(focusAccentColor: Color = SharedJourneyColors.MediterraneanTeal): TextFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusAccentColor,
            focusedLabelColor = focusAccentColor,
            cursorColor = SharedJourneyColors.TerracottaOrange,
            errorBorderColor = SharedJourneyColors.TerracottaOrange,
            errorLabelColor = SharedJourneyColors.TerracottaOrange,
            errorCursorColor = SharedJourneyColors.TerracottaOrange,
            errorSupportingTextColor = SharedJourneyColors.TerracottaOrange,
        )
}

@Composable
fun JourneyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusAccentColor: Color = SharedJourneyColors.MediterraneanTeal,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = JourneyTextFieldDefaults.minHeight),
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        supportingText = supportingText,
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = FamilyLogisticsDesign.fieldShape,
        colors = JourneyTextFieldDefaults.colors(focusAccentColor = focusAccentColor),
    )
}
