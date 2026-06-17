package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_clear_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_cleared
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_readonly_note
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_result_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_delete_item
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_add_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_cancel_edit
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_clear_checked
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_clear_checked_undo
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_description
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_duplicate
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_edit_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_edit_item
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty_active
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_save_edit
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_section_completed
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_section_to_buy
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_toggle_item
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_undo_action
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_undo_delete
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.nutrition.GroceryListPresentation
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.AiReadOnlyGroceryList
import app.mymultiverse.kmp.presentation.components.EmptyStateCard
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.GroceryInputBar
import app.mymultiverse.kmp.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.kmp.presentation.components.GroceryItemRow
import app.mymultiverse.kmp.presentation.components.GroceryItemRowTestTags
import app.mymultiverse.kmp.presentation.components.NutritionFeatureHeader
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenContentArea
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.SharedJourneyColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** @see GroceryItemRowTestTags */
object GroceryListTestTags {
    const val ITEM_ROW_PREFIX = GroceryItemRowTestTags.ROW_PREFIX
    const val CHECKBOX_PREFIX = GroceryItemRowTestTags.CHECKBOX_PREFIX
    const val CLEAR_CHECKED_ACTION = "grocery_clear_checked"
    const val CLEAR_AI_GROCERY_BUTTON = "grocery_clear_ai_grocery"
}

@Composable
fun GroceryShoppingScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val items by screenModel.groceryItems.collectAsState()
    val aiGroceryItems by screenModel.aiGroceryItems.collectAsState()
    val canWrite by screenModel.canWriteHouseholdData.collectAsState()
    var newItemText by rememberSaveable { mutableStateOf("") }
    var editingItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var refocusInput by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )
    val sections = remember(items) { GroceryListPresentation.partition(items) }
    val checkedCount = sections.completed.size
    val totalCount = items.size
    val accentColor = SharedJourneyColors.SageSoft

    val addHint = stringResource(Res.string.nutrition_grocery_add_hint)
    val duplicateMessage = stringResource(Res.string.nutrition_grocery_duplicate)
    val undoLabel = stringResource(Res.string.nutrition_grocery_undo_action)
    val undoMessage = stringResource(Res.string.nutrition_grocery_undo_delete)
    val editLabel = stringResource(Res.string.nutrition_grocery_edit_hint)
    val editContentDescription = stringResource(Res.string.nutrition_grocery_edit_item)
    val saveContentDescription = stringResource(Res.string.nutrition_grocery_save_edit)
    val cancelEditLabel = stringResource(Res.string.nutrition_grocery_cancel_edit)
    val toggleContentDescription = stringResource(Res.string.nutrition_grocery_toggle_item)
    val deleteContentDescription = stringResource(Res.string.nutrition_delete_item)
    val sectionToBuy = stringResource(Res.string.nutrition_grocery_section_to_buy)
    val sectionCompleted = stringResource(Res.string.nutrition_grocery_section_completed)
    val clearCheckedLabel = stringResource(Res.string.nutrition_grocery_clear_checked)
    val clearCheckedMessage = stringResource(Res.string.nutrition_grocery_clear_checked_undo)
    val aiGroceryClearedMessage = stringResource(Res.string.nutrition_ai_grocery_cleared)

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun submitNewItem() {
        if (newItemText.isBlank()) return
        if (screenModel.addGroceryItem(newItemText)) {
            newItemText = ""
            editingItemId = null
            refocusInput = true
        } else {
            showMessage(duplicateMessage)
        }
    }

    fun deleteWithUndo(item: GroceryItem, index: Int) {
        if (editingItemId == item.id) editingItemId = null
        screenModel.removeGroceryItem(item.id)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = undoMessage,
                actionLabel = undoLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                screenModel.restoreGroceryItem(item, index)
            }
        }
    }

    fun clearCheckedWithUndo() {
        val snapshot = screenModel.clearCheckedGroceryItems()
        if (snapshot.isEmpty()) return
        if (editingItemId != null && snapshot.firstOrNull { it.id == editingItemId }?.isChecked == true) {
            editingItemId = null
        }
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = clearCheckedMessage,
                actionLabel = undoLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                screenModel.restoreGroceryItemsSnapshot(snapshot)
            }
        }
    }

    fun clearAiGroceryWithUndo() {
        val snapshot = screenModel.clearAiGrocery()
        if (snapshot.isEmpty()) return
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = aiGroceryClearedMessage,
                actionLabel = undoLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                screenModel.restoreAiGroceryItems(snapshot)
            }
        }
    }

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_grocery_title),
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.screenContentArea(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = screenListPadding(extraBottom = ScreenLayout.listItemSpacing),
                verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
            ) {
                item {
                    NutritionFeatureHeader(
                        weekLabel = weekLabel,
                        description = stringResource(Res.string.nutrition_grocery_description),
                        icon = AppIcons.Restaurant,
                        accentColor = accentColor,
                        progressLabel = if (totalCount > 0) {
                            stringResource(
                                Res.string.nutrition_grocery_progress,
                                checkedCount,
                                totalCount,
                            )
                        } else {
                            stringResource(Res.string.nutrition_grocery_empty)
                        },
                        progress = if (totalCount == 0) 0f else checkedCount.toFloat() / totalCount,
                    )
                }

                if (!canWrite) {
                    item {
                        HouseholdViewerReadOnlyNotice()
                    }
                }

                if (aiGroceryItems.isNotEmpty()) {
                    item {
                        AiReadOnlyGroceryList(
                            items = aiGroceryItems,
                            title = stringResource(Res.string.nutrition_ai_grocery_result_title),
                            subtitle = stringResource(Res.string.nutrition_ai_grocery_readonly_note),
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { clearAiGroceryWithUndo() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(GroceryListTestTags.CLEAR_AI_GROCERY_BUTTON),
                            enabled = canWrite,
                        ) {
                            Text(stringResource(Res.string.nutrition_ai_clear_grocery))
                        }
                    }
                }

                if (totalCount == 0 && aiGroceryItems.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = stringResource(Res.string.nutrition_grocery_empty),
                            icon = AppIcons.Restaurant,
                        )
                    }
                } else if (totalCount > 0) {
                    if (sections.active.isNotEmpty()) {
                        item {
                            FamilyLogisticsSectionHeader(title = sectionToBuy)
                        }
                        items(sections.active, key = { it.id }) { item ->
                            val index = items.indexOfFirst { it.id == item.id }
                            GroceryListItem(
                                item = item,
                                index = index,
                                editingItemId = editingItemId,
                                editLabel = editLabel,
                                editContentDescription = editContentDescription,
                                saveContentDescription = saveContentDescription,
                                cancelEditLabel = cancelEditLabel,
                                toggleContentDescription = toggleContentDescription,
                                deleteContentDescription = deleteContentDescription,
                                onStartEdit = { editingItemId = item.id },
                                onCancelEdit = { if (editingItemId == item.id) editingItemId = null },
                                onSaveEdit = { label ->
                                    val saved = screenModel.updateGroceryItemLabel(item.id, label)
                                    if (!saved) showMessage(duplicateMessage)
                                    saved
                                },
                                onToggle = { screenModel.toggleGroceryItem(item.id) },
                                onDelete = { deleteWithUndo(item, index) },
                                readOnly = !canWrite,
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(Res.string.nutrition_grocery_empty_active),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SharedJourneyColors.InkMuted,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                            )
                        }
                    }

                    if (sections.completed.isNotEmpty()) {
                        item {
                            FamilyLogisticsSectionHeader(
                                title = sectionCompleted,
                                actionLabel = if (canWrite) clearCheckedLabel else null,
                                actionModifier = Modifier.testTag(GroceryListTestTags.CLEAR_CHECKED_ACTION),
                                onAction = if (canWrite) ({ clearCheckedWithUndo() }) else null,
                            )
                        }
                        items(sections.completed, key = { "done-${it.id}" }) { item ->
                            val index = items.indexOfFirst { it.id == item.id }
                            GroceryListItem(
                                item = item,
                                index = index,
                                editingItemId = editingItemId,
                                editLabel = editLabel,
                                editContentDescription = editContentDescription,
                                saveContentDescription = saveContentDescription,
                                cancelEditLabel = cancelEditLabel,
                                toggleContentDescription = toggleContentDescription,
                                deleteContentDescription = deleteContentDescription,
                                onStartEdit = { editingItemId = item.id },
                                onCancelEdit = { if (editingItemId == item.id) editingItemId = null },
                                onSaveEdit = { label ->
                                    val saved = screenModel.updateGroceryItemLabel(item.id, label)
                                    if (!saved) showMessage(duplicateMessage)
                                    saved
                                },
                                onToggle = { screenModel.toggleGroceryItem(item.id) },
                                onDelete = { deleteWithUndo(item, index) },
                                readOnly = !canWrite,
                            )
                        }
                    }
                }
            }

            if (canWrite) {
                GroceryInputBar(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = addHint,
                    addContentDescription = addHint,
                    onSubmit = { submitNewItem() },
                    accentColor = accentColor,
                    requestFocus = refocusInput,
                    onFocusRequested = { refocusInput = false },
                )
            }
        }
    }
}

@Composable
private fun GroceryListItem(
    item: GroceryItem,
    index: Int,
    editingItemId: String?,
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
) {
    GroceryItemRow(
        item = item,
        isEditing = editingItemId == item.id,
        editLabel = editLabel,
        editContentDescription = editContentDescription,
        saveContentDescription = saveContentDescription,
        cancelEditLabel = cancelEditLabel,
        toggleContentDescription = toggleContentDescription,
        deleteContentDescription = deleteContentDescription,
        onStartEdit = onStartEdit,
        onCancelEdit = onCancelEdit,
        onSaveEdit = onSaveEdit,
        onToggle = onToggle,
        onDelete = onDelete,
        readOnly = readOnly,
    )
}
