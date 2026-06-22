package app.mymultiverse.kmp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.presentation.platform.JourneyHapticFeedback
import app.mymultiverse.kmp.presentation.platform.rememberJourneyHapticFeedback
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object GroceryItemRowTestTags {
    const val ROW_PREFIX = "grocery_item_"
    const val CHECKBOX_PREFIX = "grocery_checkbox_"
    const val EDIT_FIELD_PREFIX = "grocery_edit_"
    const val EDIT_BUTTON_PREFIX = "grocery_edit_btn_"
    const val SAVE_BUTTON_PREFIX = "grocery_save_btn_"
    const val SWIPE_CHECK_HINT = "grocery_swipe_check_hint"
    const val SWIPE_DELETE_HINT = "grocery_swipe_delete_hint"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryItemRow(
    item: GroceryItem,
    isEditing: Boolean,
    editLabel: String,
    editContentDescription: String,
    saveContentDescription: String,
    cancelEditLabel: String,
    toggleContentDescription: String,
    deleteContentDescription: String,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    readOnly: Boolean = false,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (readOnly) {
        GroceryFlatRowContent(
            item = item,
            isEditing = false,
            editLabel = editLabel,
            editContentDescription = editContentDescription,
            saveContentDescription = saveContentDescription,
            cancelEditLabel = cancelEditLabel,
            toggleContentDescription = toggleContentDescription,
            onStartEdit = {},
            onCancelEdit = {},
            onSaveEdit = { false },
            onToggle = {},
            readOnly = true,
            showDivider = showDivider,
            modifier = modifier
                .fillMaxWidth()
                .testTag("${GroceryItemRowTestTags.ROW_PREFIX}${item.id}"),
        )
        return
    }

    val performHaptic = rememberJourneyHapticFeedback()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    performHaptic(JourneyHapticFeedback.LightClick)
                    onToggle()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                else -> false
            }
        },
    )

    LaunchedEffect(item.id, isEditing) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !isEditing,
        enableDismissFromEndToStart = !isEditing,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (item.isChecked) {
                        AppIcons.RadioButtonUnchecked
                    } else {
                        AppIcons.CheckCircle
                    },
                    contentDescription = toggleContentDescription,
                    tint = SharedJourneyColors.SageSoft,
                    modifier = Modifier.testTag(GroceryItemRowTestTags.SWIPE_CHECK_HINT),
                )
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = deleteContentDescription,
                    tint = SharedJourneyColors.TerracottaOrange,
                    modifier = Modifier.testTag(GroceryItemRowTestTags.SWIPE_DELETE_HINT),
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("${GroceryItemRowTestTags.ROW_PREFIX}${item.id}"),
    ) {
        GroceryFlatRowContent(
            item = item,
            isEditing = isEditing,
            editLabel = editLabel,
            editContentDescription = editContentDescription,
            saveContentDescription = saveContentDescription,
            cancelEditLabel = cancelEditLabel,
            toggleContentDescription = toggleContentDescription,
            onStartEdit = onStartEdit,
            onCancelEdit = onCancelEdit,
            onSaveEdit = onSaveEdit,
            onToggle = onToggle,
            readOnly = false,
            showDivider = showDivider,
        )
    }
}

@Composable
private fun GroceryFlatRowContent(
    item: GroceryItem,
    isEditing: Boolean,
    editLabel: String,
    editContentDescription: String,
    saveContentDescription: String,
    cancelEditLabel: String,
    toggleContentDescription: String,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Boolean,
    onToggle: () -> Unit,
    readOnly: Boolean,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    var editText by remember(item.id, isEditing) {
        mutableStateOf(item.label)
    }
    val performHaptic = rememberJourneyHapticFeedback()
    val fontScale = maxOf(1f, LocalDensity.current.fontScale)
    val rowMinHeight = (56 * fontScale).dp

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = rowMinHeight)
                .clickable(
                    enabled = !isEditing && !readOnly,
                    onClick = {
                        performHaptic(JourneyHapticFeedback.LightClick)
                        onToggle()
                    },
                )
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalAlignment = if (isEditing) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            JourneyIconButton(
                onClick = {
                    performHaptic(JourneyHapticFeedback.LightClick)
                    onToggle()
                },
                enabled = !isEditing && !readOnly,
                modifier = Modifier.testTag("${GroceryItemRowTestTags.CHECKBOX_PREFIX}${item.id}"),
            ) {
                Icon(
                    imageVector = if (item.isChecked) AppIcons.CheckCircle else AppIcons.RadioButtonUnchecked,
                    contentDescription = toggleContentDescription,
                    tint = if (item.isChecked) {
                        SharedJourneyColors.SageSoft
                    } else {
                        JourneySemanticColors.inkMuted()
                    },
                )
            }

            if (isEditing) {
                JourneyTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${GroceryItemRowTestTags.EDIT_FIELD_PREFIX}${item.id}"),
                    label = { Text(editLabel) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (onSaveEdit(editText)) onCancelEdit()
                        },
                    ),
                )
                TextButton(onClick = onCancelEdit) {
                    Text(
                        text = cancelEditLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = JourneySemanticColors.inkMuted(),
                    )
                }
                JourneyIconButton(
                    onClick = {
                        if (onSaveEdit(editText)) onCancelEdit()
                    },
                    enabled = editText.isNotBlank(),
                    modifier = Modifier.testTag("${GroceryItemRowTestTags.SAVE_BUTTON_PREFIX}${item.id}"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = JourneySemanticColors.brandTeal(),
                    ),
                ) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = saveContentDescription,
                    )
                }
            } else {
                Text(
                    text = item.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.isChecked) {
                        JourneySemanticColors.inkMuted()
                    } else {
                        JourneySemanticColors.inkDeep()
                    },
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                )
                AnimatedVisibility(visible = !item.isChecked && !readOnly) {
                    JourneyIconButton(
                        onClick = onStartEdit,
                        modifier = Modifier.testTag("${GroceryItemRowTestTags.EDIT_BUTTON_PREFIX}${item.id}"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = JourneySemanticColors.brandTeal(),
                        ),
                    ) {
                        Icon(
                            imageVector = AppIcons.Edit,
                            contentDescription = editContentDescription,
                        )
                    }
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = JourneySemanticColors.inkMuted().copy(alpha = 0.12f),
            )
        }
    }
}
