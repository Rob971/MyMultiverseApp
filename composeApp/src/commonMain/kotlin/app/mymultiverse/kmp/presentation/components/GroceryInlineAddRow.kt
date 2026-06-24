package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.presentation.theme.AppIconRole
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object GroceryInlineAddRowTestTags {
    const val ROW = "grocery_inline_add_row"
    const val INPUT_FIELD = "grocery_inline_add_input"
    const val ADD_BUTTON = "grocery_inline_add_button"
}

@Composable
fun GroceryInlineAddRow(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    addButtonLabel: String,
    onSubmit: () -> Unit,
    accentColor: Color = SharedJourneyColors.MediterraneanTeal,
    modifier: Modifier = Modifier,
    requestFocus: Boolean = false,
    onFocusRequested: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            onFocusRequested()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(GroceryInlineAddRowTestTags.ROW),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JourneyTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .testTag(GroceryInlineAddRowTestTags.INPUT_FIELD),
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            focusAccentColor = accentColor,
        )
        JourneyPrimaryButton(
            onClick = onSubmit,
            enabled = value.isNotBlank(),
            modifier = Modifier.testTag(GroceryInlineAddRowTestTags.ADD_BUTTON),
        ) {
            JourneyIcon(
                role = AppIconRole.ActionAdd,
                contentDescription = null,
                modifier = Modifier.size(FamilyLogisticsDesign.iconSize - 8.dp),
                tint = JourneySemanticColors.onAccentButton(),
            )
            Text(addButtonLabel)
        }
    }
}
