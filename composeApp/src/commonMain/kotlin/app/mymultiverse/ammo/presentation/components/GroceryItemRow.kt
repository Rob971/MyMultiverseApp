package app.mymultiverse.ammo.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.presentation.platform.JourneyHapticFeedback
import app.mymultiverse.ammo.presentation.platform.rememberJourneyHapticFeedback
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors

object GroceryItemRowTestTags {
    const val ROW_PREFIX = "grocery_item_"
    const val CHECKBOX_PREFIX = "grocery_checkbox_"
    const val EDIT_FIELD_PREFIX = "grocery_edit_"
    const val EDIT_BUTTON_PREFIX = "grocery_edit_btn_"
    const val SAVE_BUTTON_PREFIX = "grocery_save_btn_"
    const val DRAG_HANDLE_PREFIX = "grocery_drag_handle_"
    const val DELETE_BUTTON_PREFIX = "grocery_delete_btn_"
    const val DELETE_CONFIRM_BUTTON = "grocery_delete_confirm_btn"
}

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
    confirmDeleteTitle: String,
    confirmDeleteBody: String,
    confirmDeleteActionLabel: String,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    readOnly: Boolean = false,
    showDivider: Boolean = true,
    enableReorder: Boolean = false,
    dragHandleContentDescription: String = "",
    onReorderStep: (Int) -> Unit = {},
    foodEmoji: String? = null,
    modifier: Modifier = Modifier,
) {
    GroceryFlatRowContent(
        item = item,
        isEditing = isEditing,
        editLabel = editLabel,
        editContentDescription = editContentDescription,
        saveContentDescription = saveContentDescription,
        cancelEditLabel = cancelEditLabel,
        toggleContentDescription = toggleContentDescription,
        deleteContentDescription = deleteContentDescription,
        confirmDeleteTitle = confirmDeleteTitle,
        confirmDeleteBody = confirmDeleteBody,
        confirmDeleteActionLabel = confirmDeleteActionLabel,
        onStartEdit = if (readOnly) {{}} else onStartEdit,
        onCancelEdit = if (readOnly) {{}} else onCancelEdit,
        onSaveEdit = if (readOnly) {{ false }} else onSaveEdit,
        onToggle = if (readOnly) {{}} else onToggle,
        onDelete = onDelete,
        readOnly = readOnly,
        showDivider = showDivider,
        enableReorder = enableReorder && !item.isChecked,
        dragHandleContentDescription = dragHandleContentDescription,
        onReorderStep = onReorderStep,
        foodEmoji = foodEmoji,
        modifier = modifier
            .fillMaxWidth()
            .testTag("${GroceryItemRowTestTags.ROW_PREFIX}${item.id}"),
    )
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
    deleteContentDescription: String,
    confirmDeleteTitle: String,
    confirmDeleteBody: String,
    confirmDeleteActionLabel: String,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    readOnly: Boolean,
    showDivider: Boolean,
    enableReorder: Boolean,
    dragHandleContentDescription: String,
    onReorderStep: (Int) -> Unit,
    foodEmoji: String? = null,
    modifier: Modifier = Modifier,
) {
    var editText by remember(item.id, isEditing) {
        mutableStateOf(item.label)
    }
    var showDeleteConfirm by remember(item.id) { mutableStateOf(false) }
    val performHaptic = rememberJourneyHapticFeedback()
    val fontScale = maxOf(1f, LocalDensity.current.fontScale)
    val rowMinHeight = (56 * fontScale).dp
    val density = LocalDensity.current
    val reorderThresholdPx = remember(density) { with(density) { 48.dp.toPx() } }
    var accumulatedDrag by remember(item.id) { mutableFloatStateOf(0f) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(confirmDeleteTitle) },
            text = { Text(confirmDeleteBody) },
            containerColor = JourneySemanticColors.elevatedSurface(),
            titleContentColor = JourneySemanticColors.inkDeep(),
            textContentColor = JourneySemanticColors.inkMuted(),
            confirmButton = {
                JourneyDestructiveTextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    label = confirmDeleteActionLabel,
                    modifier = Modifier.testTag(GroceryItemRowTestTags.DELETE_CONFIRM_BUTTON),
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        text = cancelEditLabel,
                        color = JourneySemanticColors.inkMuted(),
                    )
                }
            },
        )
    }

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
            if (enableReorder && !isEditing) {
                Box(
                    modifier = Modifier
                        .size(FamilyLogisticsDesign.minTouchTarget)
                        .testTag("${GroceryItemRowTestTags.DRAG_HANDLE_PREFIX}${item.id}")
                        .pointerInput(item.id) {
                            detectVerticalDragGestures(
                                onDragStart = { accumulatedDrag = 0f },
                                onDragEnd = { accumulatedDrag = 0f },
                                onDragCancel = { accumulatedDrag = 0f },
                                onVerticalDrag = { _, dragAmount ->
                                    accumulatedDrag += dragAmount
                                    while (accumulatedDrag >= reorderThresholdPx) {
                                        performHaptic(JourneyHapticFeedback.LightClick)
                                        onReorderStep(1)
                                        accumulatedDrag -= reorderThresholdPx
                                    }
                                    while (accumulatedDrag <= -reorderThresholdPx) {
                                        performHaptic(JourneyHapticFeedback.LightClick)
                                        onReorderStep(-1)
                                        accumulatedDrag += reorderThresholdPx
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    JourneyIcon(
                        role = AppIconRole.DragHandle,
                        contentDescription = dragHandleContentDescription,
                    )
                }
            }

            JourneyIconButton(
                onClick = {
                    performHaptic(JourneyHapticFeedback.LightClick)
                    onToggle()
                },
                enabled = !isEditing && !readOnly,
                modifier = Modifier.testTag("${GroceryItemRowTestTags.CHECKBOX_PREFIX}${item.id}"),
            ) {
                JourneyIcon(
                    imageVector = if (item.isChecked) AppIcons.CheckCircle else AppIcons.RadioButtonUnchecked,
                    role = if (item.isChecked) AppIconRole.GroceryChecked else AppIconRole.GroceryUnchecked,
                    contentDescription = toggleContentDescription,
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
                    JourneyIcon(
                        role = AppIconRole.ActionConfirm,
                        contentDescription = saveContentDescription,
                        useContentColor = true,
                    )
                }
            } else {
                if (foodEmoji != null) {
                    FoodItemThumbnail(
                        emoji = foodEmoji,
                        size = 32.dp,
                        modifier = Modifier.alpha(if (item.isChecked) 0.35f else 1f),
                    )
                }
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
                        JourneyIcon(
                            role = AppIconRole.ActionEdit,
                            contentDescription = editContentDescription,
                            useContentColor = true,
                        )
                    }
                }
                if (!readOnly) {
                    JourneyIconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("${GroceryItemRowTestTags.DELETE_BUTTON_PREFIX}${item.id}"),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = JourneySemanticColors.brandTerracotta(),
                        ),
                    ) {
                        JourneyIcon(
                            imageVector = AppIcons.Close,
                            role = AppIconRole.ActionDelete,
                            contentDescription = deleteContentDescription,
                            useContentColor = true,
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
