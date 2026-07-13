package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
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
    var editText by remember(item.id, isEditing) { mutableStateOf(item.label) }
    var showDeleteConfirm by remember(item.id) { mutableStateOf(false) }
    val performHaptic = rememberJourneyHapticFeedback()
    val fontScale = maxOf(1f, LocalDensity.current.fontScale)
    val rowMinHeight = (56 * fontScale).dp
    val density = LocalDensity.current
    val reorderThresholdPx = remember(density) { with(density) { 48.dp.toPx() } }
    var accumulatedDrag by remember(item.id) { mutableFloatStateOf(0f) }

    // ── Pre-resolve composable colors ─────────────────────────────────────────
    val checkTeal = JourneySemanticColors.brandTeal()
    val mutedColor = JourneySemanticColors.inkMuted()
    // Checkbox: teal fill when checked, muted gray border when unchecked.
    // White checkmark ensures maximum contrast in both light and dark themes.
    val checkboxColors = CheckboxDefaults.colors(
        checkedColor = checkTeal,
        uncheckedColor = mutedColor,
        checkmarkColor = Color.White,
        disabledCheckedColor = checkTeal.copy(alpha = 0.38f),
        disabledUncheckedColor = mutedColor.copy(alpha = 0.38f),
    )

    // Edit / delete button colors
    val editContainerColor = JourneySemanticColors.brandTealContainer()
    val editContentColor = JourneySemanticColors.brandTeal()
    val deleteContainerColor = JourneySemanticColors.brandTerracotta().copy(alpha = 0.18f)
    val deleteContentColor = JourneySemanticColors.brandTerracotta()

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

        if (isEditing) {
            // ── Edit mode: inline text field + save / cancel ─────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = rowMinHeight)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                JourneyTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${GroceryItemRowTestTags.EDIT_FIELD_PREFIX}${item.id}"),
                    label = { Text(editLabel) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (onSaveEdit(editText)) onCancelEdit() },
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
                    onClick = { if (onSaveEdit(editText)) onCancelEdit() },
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
            }
        } else {
            // ── View mode: Box overlay — label pinned to screen centre ────────
            //
            // The item label fills the full row width with Arrangement.Center so
            // it always sits at the true horizontal midpoint of the screen.
            // Action buttons are absolutely positioned at the left/right edges
            // and never displace the label's centre.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = rowMinHeight)
                    // Double-tap anywhere on the row opens the inline edit field.
                    .pointerInput(item.id, readOnly, item.isChecked) {
                        if (!readOnly && !item.isChecked) {
                            detectTapGestures(onDoubleTap = {
                                performHaptic(JourneyHapticFeedback.LightClick)
                                onStartEdit()
                            })
                        }
                    }
                    .padding(vertical = 10.dp),
            ) {
                // ── Label: always at the horizontal centre of the screen ──────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (foodEmoji != null) {
                        FoodItemThumbnail(
                            emoji = foodEmoji,
                            size = 32.dp,
                            modifier = Modifier.alpha(if (item.isChecked) 0.35f else 1f),
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.isChecked) mutedColor else JourneySemanticColors.inkDeep(),
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    )
                }

                // ── Left edge: Checkbox [drag handle?] ───────────────────────
                // IMPORTANT: Checkbox is always the FIRST child so it sits at
                // x=0 of the content area for every row — checked, unchecked,
                // and in reorder mode.  The optional drag handle appears to its
                // right so it never shifts the checkbox's horizontal position.
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // ── Checkbox (always leftmost) ────────────────────────────
                    // Material3 Checkbox guarantees:
                    //   • visually distinct checked (teal fill + white ✓) vs
                    //     unchecked (muted gray border, hollow) states
                    //   • correct rendering in light and dark themes
                    //   • built-in 48 dp touch target
                    //   • accessibility semantics (Role.Checkbox, checked state)
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = if (readOnly) null else { _ ->
                            performHaptic(JourneyHapticFeedback.LightClick)
                            onToggle()
                        },
                        modifier = Modifier.testTag(
                            "${GroceryItemRowTestTags.CHECKBOX_PREFIX}${item.id}",
                        ),
                        colors = checkboxColors,
                    )

                    // ── Drag handle (right of checkbox when reorder active) ───
                    if (enableReorder) {
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
                }

                // ── Right edge: [edit?] delete ────────────────────────────────
                // Buttons use Modifier.background(color, CircleShape) to draw the
                // coloured disc without creating a clipping layer that can suppress
                // child Icon rendering on some Android GPU drivers.
                if (!readOnly) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Edit button — only shown for unchecked items
                        if (!item.isChecked) {
                            Box(
                                modifier = Modifier
                                    .size(FamilyLogisticsDesign.minTouchTarget)
                                    .testTag("${GroceryItemRowTestTags.EDIT_BUTTON_PREFIX}${item.id}"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = editContainerColor,
                                            shape = CircleShape,
                                        )
                                        .clickable(
                                            onClickLabel = editContentDescription,
                                            role = Role.Button,
                                            onClick = onStartEdit,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    JourneyIcon(
                                        role = AppIconRole.ActionEdit,
                                        contentDescription = null,
                                        tint = editContentColor,
                                    )
                                }
                            }
                        }

                        // Delete button
                        Box(
                            modifier = Modifier
                                .size(FamilyLogisticsDesign.minTouchTarget)
                                .testTag("${GroceryItemRowTestTags.DELETE_BUTTON_PREFIX}${item.id}"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = deleteContainerColor,
                                        shape = CircleShape,
                                    )
                                    .clickable(
                                        onClickLabel = deleteContentDescription,
                                        role = Role.Button,
                                        onClick = { showDeleteConfirm = true },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = AppIcons.GroceryRemoveCircle,
                                    contentDescription = null,
                                    tint = deleteContentColor,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
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
