package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_readonly_note
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_result_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_add_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_cancel_edit
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_clear_checked
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
}

@Composable
fun GroceryShoppingScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val items by screenModel.groceryItems.collectAsState()
    val aiGroceryItems by screenModel.aiGroceryItems.collectAsState()
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
    val sectionToBuy = stringResource(Res.string.nutrition_grocery_section_to_buy)
    val sectionCompleted = stringResource(Res.string.nutrition_grocery_section_completed)
    val clearCheckedLabel = stringResource(Res.string.nutrition_grocery_clear_checked)

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

                if (aiGroceryItems.isNotEmpty()) {
                    item {
                        AiReadOnlyGroceryList(
                            items = aiGroceryItems,
                            title = stringResource(Res.string.nutrition_ai_grocery_result_title),
                            subtitle = stringResource(Res.string.nutrition_ai_grocery_readonly_note),
                        )
                    }
                }

                if (totalCount == 0) {
                    item {
                        EmptyStateCard(
                            message = stringResource(Res.string.nutrition_grocery_empty),
                            icon = AppIcons.Restaurant,
                        )
                    }
                } else {
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
                                onStartEdit = { editingItemId = item.id },
                                onCancelEdit = { if (editingItemId == item.id) editingItemId = null },
                                onSaveEdit = { label ->
                                    val saved = screenModel.updateGroceryItemLabel(item.id, label)
                                    if (!saved) showMessage(duplicateMessage)
                                    saved
                                },
                                onToggle = { screenModel.toggleGroceryItem(item.id) },
                                onDelete = { deleteWithUndo(item, index) },
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
                                actionLabel = clearCheckedLabel,
                                onAction = { screenModel.clearCheckedGroceryItems() },
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
                                onStartEdit = { editingItemId = item.id },
                                onCancelEdit = { if (editingItemId == item.id) editingItemId = null },
                                onSaveEdit = { label ->
                                    val saved = screenModel.updateGroceryItemLabel(item.id, label)
                                    if (!saved) showMessage(duplicateMessage)
                                    saved
                                },
                                onToggle = { screenModel.toggleGroceryItem(item.id) },
                                onDelete = { deleteWithUndo(item, index) },
                            )
                        }
                    }
                }
            }

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
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    GroceryItemRow(
        item = item,
        isEditing = editingItemId == item.id,
        editLabel = editLabel,
        editContentDescription = editContentDescription,
        saveContentDescription = saveContentDescription,
        cancelEditLabel = cancelEditLabel,
        toggleContentDescription = toggleContentDescription,
        onStartEdit = onStartEdit,
        onCancelEdit = onCancelEdit,
        onSaveEdit = onSaveEdit,
        onToggle = onToggle,
        onDelete = onDelete,
    )
}
