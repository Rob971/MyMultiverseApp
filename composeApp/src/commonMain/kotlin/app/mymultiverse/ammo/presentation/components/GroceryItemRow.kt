package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.presentation.platform.JourneyHapticFeedback
import app.mymultiverse.ammo.presentation.platform.rememberJourneyHapticFeedback
import app.mymultiverse.ammo.presentation.theme.AppIconRole
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

// ── Primitive icon draw functions ─────────────────────────────────────────────
//
// These draw icons using ONLY raw DrawScope primitives (drawRoundRect, drawPath,
// rotate).  No VectorPainter / Picture / ImageVector involved.
//
// Root cause of previous invisible icons: this device's GPU driver does not
// support Picture playback (used internally by VectorPainter / Icon composable).
// drawCircle() and drawRoundRect() are direct GPU draw calls — always work.

/** Trash-can icon: handle bar + lid bar + trapezoidal body. */
private fun DrawScope.drawTrashIcon(color: Color) {
    val w = size.width
    val h = size.height

    // ── Handle (small bar above lid, centred) ─────────────────────────────────
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.375f, h * 0.10f),
        size = Size(w * 0.25f, h * 0.12f),
        cornerRadius = CornerRadius(w * 0.04f),
    )

    // ── Lid (full-width horizontal bar) ──────────────────────────────────────
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.10f, h * 0.26f),
        size = Size(w * 0.80f, h * 0.11f),
        cornerRadius = CornerRadius(w * 0.04f),
    )

    // ── Body (trapezoid — slightly narrower at bottom, rounded corners) ───────
    val bL = w * 0.18f   // body left
    val bR = w * 0.82f   // body right
    val bT = h * 0.40f   // body top
    val bB = h * 0.87f   // body bottom
    val r  = w * 0.07f   // corner radius
    val body = Path().apply {
        moveTo(bL, bT)
        lineTo(bR, bT)
        // Taper slightly inward toward bottom
        lineTo(bR - r * 0.5f, bB - r)
        quadraticTo(bR, bB, bR - r, bB)
        lineTo(bL + r, bB)
        quadraticTo(bL, bB, bL + r * 0.5f, bB - r)
        close()
    }
    drawPath(body, color = color)

    // ── Three vertical lines inside body (visual detail) ─────────────────────
    val lineTop = bT + h * 0.06f
    val lineBot = bB - h * 0.10f
    val lineW = w * 0.055f
    val lineR = CornerRadius(lineW / 2)
    for (i in 0..2) {
        val lx = w * 0.32f + i * w * 0.18f - lineW / 2
        drawRoundRect(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = Offset(lx, lineTop),
            size = Size(lineW, lineBot - lineTop),
            cornerRadius = lineR,
        )
    }
}

/** Pencil icon: rotated body + tip triangle + eraser. */
private fun DrawScope.drawPencilIcon(color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Rotate -45° so a vertical pencil becomes a diagonal one pointing bottom-left
    rotate(degrees = -45f, pivot = Offset(cx, cy)) {
        val s = size.minDimension

        val pw = s * 0.19f    // pencil width
        val bodyH = s * 0.52f // pencil body height
        val tipH  = s * 0.14f // tip triangle height
        val eraserH = s * 0.09f

        val bodyTop = cy - bodyH / 2f

        // Body (rounded rectangle)
        drawRoundRect(
            color = color,
            topLeft = Offset(cx - pw / 2, bodyTop),
            size = Size(pw, bodyH),
            cornerRadius = CornerRadius(pw * 0.28f),
        )

        // Tip (triangle at bottom of body)
        val tipPath = Path().apply {
            moveTo(cx - pw / 2, bodyTop + bodyH)
            lineTo(cx, bodyTop + bodyH + tipH)   // sharp point
            lineTo(cx + pw / 2, bodyTop + bodyH)
            close()
        }
        drawPath(tipPath, color = color)

        // Eraser band (slightly lighter, at top)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.60f),
            topLeft = Offset(cx - pw / 2, bodyTop - eraserH + pw * 0.28f),
            size = Size(pw, eraserH),
            cornerRadius = CornerRadius(pw * 0.28f),
        )
    }
}

// ── Action button composable ──────────────────────────────────────────────────

/**
 * Circular grocery action button drawn entirely with DrawScope primitives.
 *
 * Circle background and icon are drawn in a SINGLE Canvas call so there is
 * no compositing layer boundary that could cause the icon to be invisible on
 * certain Android GPU drivers.
 */
@Composable
private fun GroceryCircleButton(
    containerColor: Color,
    drawIconContent: DrawScope.() -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    circleSize: Dp = 36.dp,
    onClick: () -> Unit,
) {
    Canvas(
        modifier = modifier
            .size(circleSize)
            .semantics {
                this.role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            }
            .clickable(onClick = onClick),
    ) {
        // 1. Circle background — direct GPU call, always works
        drawCircle(color = containerColor, radius = size.minDimension / 2f)
        // 2. Icon — same DrawScope, same GPU draw call, same layer
        drawIconContent()
    }
}

// ─────────────────────────────────────────────────────────────────────────────

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

    // Pre-resolve composable colors
    val checkTeal = JourneySemanticColors.brandTeal()
    val mutedColor = JourneySemanticColors.inkMuted()
    val checkboxColors = CheckboxDefaults.colors(
        checkedColor = checkTeal,
        uncheckedColor = mutedColor,
        checkmarkColor = Color.White,
        disabledCheckedColor = checkTeal.copy(alpha = 0.38f),
        disabledUncheckedColor = mutedColor.copy(alpha = 0.38f),
    )
    val editContainerColor = JourneySemanticColors.brandTealContainer()
    val editContentColor = JourneySemanticColors.brandTeal()
    val deleteContainerColor = JourneySemanticColors.brandTerracotta().copy(alpha = 0.22f)
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
            // ── Edit mode ────────────────────────────────────────────────────
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = rowMinHeight)
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
                // ── Label — always at true screen centre ──────────────────────
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
                        color = if (item.isChecked) mutedColor
                                else JourneySemanticColors.inkDeep(),
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough
                                         else null,
                    )
                }

                // ── Left edge: Checkbox [drag handle?] ───────────────────────
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Checkbox first — column position never shifts.
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
                if (!readOnly) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!item.isChecked) {
                            Box(
                                modifier = Modifier
                                    .size(FamilyLogisticsDesign.minTouchTarget)
                                    .testTag("${GroceryItemRowTestTags.EDIT_BUTTON_PREFIX}${item.id}"),
                                contentAlignment = Alignment.Center,
                            ) {
                                GroceryCircleButton(
                                    containerColor = editContainerColor,
                                    drawIconContent = { drawPencilIcon(editContentColor) },
                                    contentDescription = editContentDescription,
                                    onClick = onStartEdit,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(FamilyLogisticsDesign.minTouchTarget)
                                .testTag("${GroceryItemRowTestTags.DELETE_BUTTON_PREFIX}${item.id}"),
                            contentAlignment = Alignment.Center,
                        ) {
                            GroceryCircleButton(
                                containerColor = deleteContainerColor,
                                drawIconContent = { drawTrashIcon(deleteContentColor) },
                                contentDescription = deleteContentDescription,
                                onClick = { showDeleteConfirm = true },
                            )
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
