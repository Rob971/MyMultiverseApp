package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object GroceryInputBarTestTags {
    const val INPUT_FIELD = "grocery_add_input"
    const val ADD_BUTTON = "grocery_add_button"
}

@Composable
fun GroceryInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    addContentDescription: String,
    onSubmit: () -> Unit,
    accentColor: Color = SharedJourneyColors.SageSoft,
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

    FamilyLogisticsCardSurface(
        modifier = modifier,
        accentColor = accentColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = ScreenLayout.inputBarHorizontalPadding,
                    vertical = ScreenLayout.inputBarVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .testTag(GroceryInputBarTestTags.INPUT_FIELD),
                placeholder = { Text(placeholder) },
                singleLine = true,
                shape = FamilyLogisticsDesign.fieldShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    focusedLabelColor = accentColor,
                    cursorColor = accentColor,
                ),
            )
            FeatureAccentIconButton(
                onClick = onSubmit,
                enabled = value.isNotBlank(),
                accentColor = accentColor,
                contentDescription = addContentDescription,
                modifier = Modifier.testTag(GroceryInputBarTestTags.ADD_BUTTON),
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = null,
                    tint = SharedJourneyColors.SunDrenchedWhite,
                    modifier = Modifier.size(FamilyLogisticsDesign.iconSize - 4.dp),
                )
            }
        }
    }
}
