package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    const val STICKY_BAR = "grocery_sticky_input_bar"
}

@Composable
fun GroceryInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    addContentDescription: String,
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(GroceryInputBarTestTags.STICKY_BAR),
        color = SharedJourneyColors.SunDrenchedWhite,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    horizontal = ScreenLayout.inputBarHorizontalPadding,
                    vertical = ScreenLayout.inputBarVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JourneyTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .testTag(GroceryInputBarTestTags.INPUT_FIELD),
                placeholder = { Text(placeholder) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                focusAccentColor = accentColor,
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
