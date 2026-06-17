package app.mymultiverse.kmp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors

object GroceryItemRowTestTags {
    const val ROW_PREFIX = "grocery_item_"
    const val CHECKBOX_PREFIX = "grocery_checkbox_"
    const val EDIT_FIELD_PREFIX = "grocery_edit_"
    const val EDIT_BUTTON_PREFIX = "grocery_edit_btn_"
    const val SAVE_BUTTON_PREFIX = "grocery_save_btn_"
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
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )

    LaunchedEffect(item.id) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = deleteContentDescription,
                    tint = SharedJourneyColors.TerracottaOrange,
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("${GroceryItemRowTestTags.ROW_PREFIX}${item.id}"),
    ) {
        GroceryItemCard(
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
        )
    }
}

@Composable
private fun GroceryItemCard(
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
) {
    var editText by remember(item.id, isEditing) {
        mutableStateOf(item.label)
    }

    FamilyLogisticsCardSurface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = onToggle,
                enabled = !isEditing,
                modifier = Modifier.testTag("${GroceryItemRowTestTags.CHECKBOX_PREFIX}${item.id}"),
            ) {
                Icon(
                    imageVector = if (item.isChecked) AppIcons.CheckCircle else AppIcons.RadioButtonUnchecked,
                    contentDescription = toggleContentDescription,
                    tint = if (item.isChecked) {
                        SharedJourneyColors.SageSoft
                    } else {
                        SharedJourneyColors.InkMuted
                    },
                )
            }

            if (isEditing) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${GroceryItemRowTestTags.EDIT_FIELD_PREFIX}${item.id}"),
                    label = { Text(editLabel) },
                    singleLine = true,
                    shape = FamilyLogisticsDesign.fieldShape,
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
                        color = SharedJourneyColors.InkMuted,
                    )
                }
                IconButton(
                    onClick = {
                        if (onSaveEdit(editText)) onCancelEdit()
                    },
                    enabled = editText.isNotBlank(),
                    modifier = Modifier.testTag("${GroceryItemRowTestTags.SAVE_BUTTON_PREFIX}${item.id}"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = SharedJourneyColors.MediterraneanTeal,
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
                        SharedJourneyColors.InkMuted
                    } else {
                        SharedJourneyColors.InkDeep
                    },
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                )
                AnimatedVisibility(visible = !item.isChecked) {
                    IconButton(
                        onClick = onStartEdit,
                        modifier = Modifier.testTag("${GroceryItemRowTestTags.EDIT_BUTTON_PREFIX}${item.id}"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = SharedJourneyColors.MediterraneanTeal,
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
    }
}
