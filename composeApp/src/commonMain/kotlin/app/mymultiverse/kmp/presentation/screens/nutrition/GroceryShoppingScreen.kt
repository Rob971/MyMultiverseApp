package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_none
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_adopt_all_grocery_summary
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_ai_grocery_suggestions_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_next
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_previous
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
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_section_completed_count
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
import app.mymultiverse.kmp.presentation.components.AiGrocerySuggestionsSection
import app.mymultiverse.kmp.presentation.components.WeekSelectorBanner
import app.mymultiverse.kmp.presentation.components.EmptyStateCard
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.GroceryDashboardCard
import app.mymultiverse.kmp.presentation.components.GroceryInputBar
import app.mymultiverse.kmp.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.kmp.presentation.components.GroceryItemRow
import app.mymultiverse.kmp.presentation.components.GroceryItemRowTestTags
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.components.ScreenLayout
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
    const val SCROLL_LIST = "grocery_scroll_list"
    const val COMPLETED_SECTION_HEADER = "grocery_completed_section_header"
}

@Composable
fun GroceryShoppingScreen(
    onBack: () -> Unit,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val items by screenModel.groceryItems.collectAsState()
    val aiGroceryItems by screenModel.aiGroceryItems.collectAsState()
    val adoptAllResult by screenModel.adoptAllGroceryResult.collectAsState()
    val canWrite by screenModel.canWriteHouseholdData.collectAsState()
    val weekOffset by screenModel.weekOffset.collectAsState()
    var newItemText by rememberSaveable { mutableStateOf("") }
    var editingItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var refocusInput by rememberSaveable { mutableStateOf(false) }
    var pendingScrollLabel by rememberSaveable { mutableStateOf<String?>(null) }
    var completedExpanded by rememberSaveable { mutableStateOf(false) }
    var completedExpandUserSet by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )
    val previousWeekLabel = stringResource(Res.string.nutrition_week_previous)
    val nextWeekLabel = stringResource(Res.string.nutrition_week_next)
    val aiSuggestionsTitle = stringResource(Res.string.nutrition_ai_grocery_suggestions_title)
    val adoptAllLabel = stringResource(Res.string.nutrition_ai_adopt_all_grocery)
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
    val sectionCompletedCount = stringResource(
        Res.string.nutrition_grocery_section_completed_count,
        sections.completed.size,
    )
    val clearCheckedLabel = stringResource(Res.string.nutrition_grocery_clear_checked)
    val clearCheckedMessage = stringResource(Res.string.nutrition_grocery_clear_checked_undo)
    val adoptAllNoneMessage = stringResource(Res.string.nutrition_ai_adopt_all_grocery_none)
    val adoptAllSummaryMessage = adoptAllResult?.let { count ->
        if (count == 0) {
            adoptAllNoneMessage
        } else {
            stringResource(Res.string.nutrition_ai_adopt_all_grocery_summary, count)
        }
    }

    LaunchedEffect(adoptAllSummaryMessage) {
        val message = adoptAllSummaryMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeAdoptAllGroceryResult()
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun submitNewItem() {
        if (newItemText.isBlank()) return
        if (screenModel.addGroceryItem(newItemText)) {
            pendingScrollLabel = newItemText.trim()
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

    LaunchedEffect(sections.completed.size) {
        if (!completedExpandUserSet) {
            completedExpanded = sections.completed.size <= 3
        }
    }

    LaunchedEffect(items, pendingScrollLabel, aiGroceryItems.size, canWrite) {
        val label = pendingScrollLabel ?: return@LaunchedEffect
        if (items.none { it.label.equals(label, ignoreCase = true) }) return@LaunchedEffect

        var index = 1
        if (!canWrite) index++
        if (aiGroceryItems.isNotEmpty()) index += 2
        val activeIndex = sections.active.indexOfFirst { it.label.equals(label, ignoreCase = true) }
        if (activeIndex < 0) return@LaunchedEffect
        index += 1 + activeIndex

        listState.animateScrollToItem(index.coerceAtLeast(0))
        pendingScrollLabel = null
    }

    NutritionScaffold(
        title = stringResource(Res.string.nutrition_grocery_title),
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (canWrite) {
                GroceryInputBar(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = addHint,
                    addContentDescription = addHint,
                    onSubmit = { submitNewItem() },
                    accentColor = SharedJourneyColors.MediterraneanTeal,
                    requestFocus = refocusInput,
                    onFocusRequested = { refocusInput = false },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenLayout.horizontalPadding)
                .testTag(GroceryListTestTags.SCROLL_LIST),
            state = listState,
            contentPadding = screenListPadding(extraBottom = ScreenLayout.listItemSpacing),
            verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
        ) {
            item(key = "week-selector") {
                WeekSelectorBanner(
                    weekLabel = weekLabel,
                    canGoToPreviousWeek = screenModel.canGoToPreviousWeek,
                    canGoToNextWeek = screenModel.canGoToNextWeek,
                    previousWeekLabel = previousWeekLabel,
                    nextWeekLabel = nextWeekLabel,
                    onPreviousWeek = { screenModel.selectWeekOffset(weekOffset - 1) },
                    onNextWeek = { screenModel.selectWeekOffset(weekOffset + 1) },
                )
            }
            item(key = "dashboard") {
                GroceryDashboardCard(
                    description = stringResource(Res.string.nutrition_grocery_description),
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
                    accentColor = accentColor,
                )
            }

            if (!canWrite) {
                item(key = "viewer-notice") {
                    HouseholdViewerReadOnlyNotice()
                }
            }

            if (aiGroceryItems.isNotEmpty()) {
                item(key = "ai-suggestions") {
                    AiGrocerySuggestionsSection(
                        title = aiSuggestionsTitle,
                        items = aiGroceryItems,
                        adoptAllLabel = adoptAllLabel,
                        onAdoptAll = { screenModel.adoptAllAiGrocerySuggestions() },
                        onAdopt = { suggestion ->
                            screenModel.adoptAiGrocerySuggestion(suggestion.id)
                            pendingScrollLabel = suggestion.label
                        },
                        enabled = canWrite,
                    )
                }
            }

            if (totalCount == 0 && aiGroceryItems.isEmpty()) {
                item(key = "empty-state") {
                    EmptyStateCard(
                        message = stringResource(Res.string.nutrition_grocery_empty),
                        icon = AppIcons.Restaurant,
                    )
                }
            } else if (totalCount > 0) {
                if (sections.active.isNotEmpty()) {
                    item(key = "section-to-buy") {
                        FamilyLogisticsSectionHeader(title = sectionToBuy)
                    }
                    items(
                        items = sections.active,
                        key = { it.id },
                    ) { item ->
                        val index = items.indexOfFirst { it.id == item.id }
                        val isLastActive = item.id == sections.active.last().id
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
                            showDivider = !isLastActive || sections.completed.isNotEmpty(),
                            modifier = Modifier.animateItem(),
                        )
                    }
                } else {
                    item(key = "empty-active") {
                        Text(
                            text = stringResource(Res.string.nutrition_grocery_empty_active),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SharedJourneyColors.InkMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        )
                    }
                }

                if (sections.completed.isNotEmpty()) {
                    item(key = "section-completed") {
                        FamilyLogisticsSectionHeader(
                            title = sectionCompletedCount,
                            titleModifier = Modifier.testTag(GroceryListTestTags.COMPLETED_SECTION_HEADER),
                            onTitleClick = {
                                completedExpandUserSet = true
                                completedExpanded = !completedExpanded
                            },
                            actionLabel = if (canWrite) clearCheckedLabel else null,
                            actionModifier = Modifier.testTag(GroceryListTestTags.CLEAR_CHECKED_ACTION),
                            onAction = if (canWrite) ({ clearCheckedWithUndo() }) else null,
                        )
                    }
                    if (completedExpanded) {
                        items(
                            items = sections.completed,
                            key = { "done-${it.id}" },
                        ) { item ->
                            val index = items.indexOfFirst { it.id == item.id }
                            val isLastCompleted = item.id == sections.completed.last().id
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
                            showDivider = !isLastCompleted,
                            modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
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
    showDivider: Boolean = true,
    modifier: Modifier = Modifier,
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
        showDivider = showDivider,
        modifier = modifier,
    )
}
