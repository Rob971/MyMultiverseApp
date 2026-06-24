package app.mymultiverse.kmp.presentation.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_next
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_previous
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_delete_item
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_collaboration_actor_unknown
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_collaboration_grocery_added
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_collaboration_grocery_batch
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_collaboration_grocery_checked
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_add_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_cancel_edit
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_clear_checked
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_clear_checked_undo
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_drag_handle
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_duplicate
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_ghost_pairing_action
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_ghost_pairing_dismiss
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_ghost_pairing_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_edit_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_edit_item
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty_active
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty_cta
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_empty_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_keep_screen_off
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_keep_screen_on
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_hide_checked
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_show_checked
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_progress
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_save_edit
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_section_completed_count
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_section_update_list
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_update_list_hint
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_toggle_item
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_undo_action
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_grocery_undo_delete
import kmpvoyagercleanarchitecture.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.nutrition.GroceryGhostPairing
import app.mymultiverse.kmp.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.kmp.domain.nutrition.GroceryListPresentation
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.presentation.components.JourneyEmptyState
import app.mymultiverse.kmp.presentation.components.JourneyIcon
import app.mymultiverse.kmp.presentation.components.JourneyIconButton
import app.mymultiverse.kmp.presentation.components.JourneyTertiaryButton
import app.mymultiverse.kmp.presentation.components.WeekSelectorBanner
import app.mymultiverse.kmp.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.kmp.presentation.components.GroceryGhostPairingBanner
import app.mymultiverse.kmp.presentation.components.GroceryInputBar
import app.mymultiverse.kmp.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.kmp.presentation.components.GroceryItemRow
import app.mymultiverse.kmp.presentation.components.GroceryItemRowTestTags
import app.mymultiverse.kmp.presentation.components.JourneySnackbarHost
import app.mymultiverse.kmp.presentation.components.NutritionScaffold
import app.mymultiverse.kmp.presentation.platform.KeepScreenOn
import app.mymultiverse.kmp.presentation.components.ScreenLayout
import app.mymultiverse.kmp.presentation.components.screenListPadding
import app.mymultiverse.kmp.presentation.theme.AppIconRole
import app.mymultiverse.kmp.presentation.theme.AppIcons
import app.mymultiverse.kmp.presentation.theme.JourneySemanticColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** @see GroceryItemRowTestTags */
object GroceryListTestTags {
    const val ITEM_ROW_PREFIX = GroceryItemRowTestTags.ROW_PREFIX
    const val CHECKBOX_PREFIX = GroceryItemRowTestTags.CHECKBOX_PREFIX
    const val CLEAR_CHECKED_ACTION = "grocery_clear_checked"
    const val SCROLL_LIST = "grocery_scroll_list"
    const val EMPTY_STATE = "grocery_empty_state"
    const val COMPLETED_SECTION_HEADER = "grocery_completed_section_header"
    const val SHOPPING_HIDE_CHECKED_TOGGLE = "grocery_shopping_hide_checked_toggle"
    const val UPDATE_LIST_SECTION = "grocery_update_list_section"
    const val KEEP_SCREEN_ON_TOGGLE = "grocery_keep_screen_on_toggle"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryShoppingScreen(
    onBack: () -> Unit,
    embeddedInTabs: Boolean = false,
    screenModel: NutritionScreenModel = koinInject(),
) {
    val items by screenModel.groceryItems.collectAsState()
    val collaborationSnackbar by screenModel.collaborationSnackbar.collectAsState()
    val ghostPairingOffer by screenModel.ghostPairingOffer.collectAsState()
    val canWrite by screenModel.canWriteHouseholdData.collectAsState()
    val isRefreshing by screenModel.isRefreshing.collectAsState()
    val weekOffset by screenModel.weekOffset.collectAsState()
    var newItemText by rememberSaveable { mutableStateOf("") }
    var editingItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var refocusInput by rememberSaveable { mutableStateOf(false) }
    var pendingScrollLabel by rememberSaveable { mutableStateOf<String?>(null) }
    var completedExpanded by rememberSaveable { mutableStateOf(false) }
    var completedExpandUserSet by rememberSaveable { mutableStateOf(false) }
    var hideCheckedItems by rememberSaveable { mutableStateOf(false) }
    var keepScreenOn by rememberSaveable { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    val weekLabel = stringResource(
        Res.string.nutrition_week_label,
        WeekCalendar.formatWeekRange(screenModel.weekKey),
    )
    val previousWeekLabel = stringResource(Res.string.nutrition_week_previous)
    val nextWeekLabel = stringResource(Res.string.nutrition_week_next)
    val sections = remember(items) { GroceryListPresentation.partition(items) }
    val displaySections = remember(sections, hideCheckedItems) {
        GroceryListPresentation.forShoppingDisplay(sections, hideCheckedItems)
    }
    val checkedCount = sections.completed.size
    val totalCount = items.size

    KeepScreenOn(enabled = keepScreenOn)

    val keepScreenOnLabel = stringResource(Res.string.nutrition_grocery_keep_screen_on)
    val keepScreenOffLabel = stringResource(Res.string.nutrition_grocery_keep_screen_off)

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
    val dragHandleContentDescription = stringResource(Res.string.nutrition_grocery_drag_handle)
    val updateListTitle = stringResource(Res.string.nutrition_grocery_section_update_list)
    val updateListHint = stringResource(Res.string.nutrition_grocery_update_list_hint)
    val updateListProgress = if (totalCount > 0) {
        stringResource(Res.string.nutrition_grocery_progress, checkedCount, totalCount)
    } else {
        ""
    }
    val updateListEmptySubtitle = stringResource(Res.string.nutrition_grocery_empty)
    val updateListSubtitle = if (totalCount > 0) {
        "$updateListHint · $updateListProgress"
    } else {
        updateListEmptySubtitle
    }
    val sectionCompletedCount = stringResource(
        Res.string.nutrition_grocery_section_completed_count,
        sections.completed.size,
    )
    val clearCheckedLabel = stringResource(Res.string.nutrition_grocery_clear_checked)
    val hideCheckedLabel = stringResource(Res.string.nutrition_grocery_hide_checked)
    val showCheckedLabel = stringResource(
        Res.string.nutrition_grocery_show_checked,
        sections.completed.size,
    )
    val clearCheckedMessage = stringResource(Res.string.nutrition_grocery_clear_checked_undo)
    val collaborationActorUnknown = stringResource(Res.string.nutrition_collaboration_actor_unknown)

    val collaborationSnackbarMessage = collaborationSnackbar?.let { event ->
        val actor = event.actorName.ifBlank { collaborationActorUnknown }
        when {
            event.batchedCount > 1 -> stringResource(
                Res.string.nutrition_collaboration_grocery_batch,
                actor,
                event.batchedCount,
            )
            event.kind == NutritionCollaborationActivityKind.GroceryAdded -> stringResource(
                Res.string.nutrition_collaboration_grocery_added,
                actor,
                event.itemLabel,
            )
            else -> stringResource(
                Res.string.nutrition_collaboration_grocery_checked,
                actor,
                event.itemLabel,
            )
        }
    }

    LaunchedEffect(collaborationSnackbarMessage) {
        val message = collaborationSnackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeCollaborationSnackbar()
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun submitNewItem() {
        if (newItemText.isBlank()) return
        val trimmed = newItemText.trim()
        if (screenModel.addGroceryItem(trimmed)) {
            pendingScrollLabel = trimmed
            newItemText = ""
            editingItemId = null
            refocusInput = true
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

    LaunchedEffect(items, pendingScrollLabel, canWrite) {
        val label = pendingScrollLabel ?: return@LaunchedEffect
        if (items.none { it.label.equals(label, ignoreCase = true) }) return@LaunchedEffect

        var index = 1
        if (!canWrite) index++
        index += 1
        val activeIndex = sections.active.indexOfFirst { it.label.equals(label, ignoreCase = true) }
        if (activeIndex < 0) return@LaunchedEffect
        index += activeIndex

        listState.animateScrollToItem(index.coerceAtLeast(0))
        pendingScrollLabel = null
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideLayout = maxWidth >= ScreenLayout.expandedMinWidth

        NutritionScaffold(
            title = stringResource(Res.string.nutrition_grocery_title),
            onBack = onBack,
            showBackButton = !embeddedInTabs,
            actions = {
                JourneyIconButton(
                    onClick = { keepScreenOn = !keepScreenOn },
                    modifier = Modifier.testTag(GroceryListTestTags.KEEP_SCREEN_ON_TOGGLE),
                ) {
                    JourneyIcon(
                        role = AppIconRole.Hint,
                        contentDescription = if (keepScreenOn) keepScreenOffLabel else keepScreenOnLabel,
                        tint = if (keepScreenOn) {
                            JourneySemanticColors.brandTeal()
                        } else {
                            JourneySemanticColors.inkMuted()
                        },
                    )
                }
            },
            snackbarHost = {
                JourneySnackbarHost(
                    hostState = snackbarHostState,
                    aboveBottomBar = canWrite && !isWideLayout,
                )
            },
            bottomBar = {
                if (canWrite && !isWideLayout) {
                    GroceryInputBar(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        placeholder = addHint,
                        addContentDescription = addHint,
                        onSubmit = { submitNewItem() },
                        accentColor = JourneySemanticColors.brandTeal(),
                        requestFocus = refocusInput,
                        onFocusRequested = { refocusInput = false },
                        embeddedInMainTabs = embeddedInTabs,
                    )
                }
            },
        ) { padding ->
            val listModifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ScreenLayout.horizontalPadding)
                .testTag(GroceryListTestTags.SCROLL_LIST)

            val groceryListContent: LazyListScope.() -> Unit = {
                groceryShoppingListItems(
                    weekLabel = weekLabel,
                    previousWeekLabel = previousWeekLabel,
                    nextWeekLabel = nextWeekLabel,
                    weekOffset = weekOffset,
                    screenModel = screenModel,
                    canWrite = canWrite,
                    updateListTitle = updateListTitle,
                    updateListSubtitle = updateListSubtitle,
                    sections = displaySections,
                    rawSections = sections,
                    hideCheckedItems = hideCheckedItems,
                    onHideCheckedToggle = { hideCheckedItems = !hideCheckedItems },
                    hideCheckedLabel = hideCheckedLabel,
                    showCheckedLabel = showCheckedLabel,
                    totalCount = totalCount,
                    items = items,
                    editingItemId = editingItemId,
                    editLabel = editLabel,
                    editContentDescription = editContentDescription,
                    saveContentDescription = saveContentDescription,
                    cancelEditLabel = cancelEditLabel,
                    toggleContentDescription = toggleContentDescription,
                    deleteContentDescription = deleteContentDescription,
                    dragHandleContentDescription = dragHandleContentDescription,
                    sectionCompletedCount = sectionCompletedCount,
                    clearCheckedLabel = clearCheckedLabel,
                    completedExpanded = completedExpanded,
                    onCompletedExpandToggle = {
                        completedExpandUserSet = true
                        completedExpanded = !completedExpanded
                    },
                    onRefocusInput = { refocusInput = true },
                    onStartEdit = { editingItemId = it },
                    onCancelEdit = { if (editingItemId == it) editingItemId = null },
                    onSaveEdit = { id, label ->
                        val saved = screenModel.updateGroceryItemLabel(id, label)
                        if (!saved) showMessage(duplicateMessage)
                        saved
                    },
                    onToggle = { screenModel.toggleGroceryItem(it) },
                    onDelete = { item, index -> deleteWithUndo(item, index) },
                    onReorderStep = { itemId, direction ->
                        screenModel.moveActiveGroceryItem(itemId, direction)
                    },
                    onClearChecked = { clearCheckedWithUndo() },
                    ghostPairingOffer = ghostPairingOffer,
                    onAcceptGhostPairing = { labels ->
                        screenModel.acceptGhostPairing(labels)
                        pendingScrollLabel = labels.firstOrNull()
                    },
                    onDismissGhostPairing = screenModel::dismissGhostPairing,
                )
            }

            if (isWideLayout && canWrite) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = screenModel::refresh,
                        state = pullRefreshState,
                        modifier = Modifier.weight(1f),
                    ) {
                        LazyColumn(
                            modifier = listModifier,
                            state = listState,
                            contentPadding = screenListPadding(extraBottom = ScreenLayout.listItemSpacing),
                            verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
                            content = groceryListContent,
                        )
                    }
                    GroceryInputBar(
                        modifier = Modifier
                            .width(ScreenLayout.expandedSidePanelWidth)
                            .fillMaxHeight(),
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        placeholder = addHint,
                        addContentDescription = addHint,
                        onSubmit = { submitNewItem() },
                        accentColor = JourneySemanticColors.brandTeal(),
                        requestFocus = refocusInput,
                        onFocusRequested = { refocusInput = false },
                        embeddedInSidePanel = true,
                    )
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = screenModel::refresh,
                    state = pullRefreshState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    LazyColumn(
                        modifier = listModifier,
                        state = listState,
                        contentPadding = screenListPadding(extraBottom = ScreenLayout.listItemSpacing),
                        verticalArrangement = Arrangement.spacedBy(ScreenLayout.listItemSpacing),
                        content = groceryListContent,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.groceryShoppingListItems(
    weekLabel: String,
    previousWeekLabel: String,
    nextWeekLabel: String,
    weekOffset: Int,
    screenModel: NutritionScreenModel,
    canWrite: Boolean,
    updateListTitle: String,
    updateListSubtitle: String,
    sections: GroceryListPresentation.Sections,
    rawSections: GroceryListPresentation.Sections,
    hideCheckedItems: Boolean,
    onHideCheckedToggle: () -> Unit,
    hideCheckedLabel: String,
    showCheckedLabel: String,
    totalCount: Int,
    items: List<GroceryItem>,
    editingItemId: String?,
    editLabel: String,
    editContentDescription: String,
    saveContentDescription: String,
    cancelEditLabel: String,
    toggleContentDescription: String,
    deleteContentDescription: String,
    dragHandleContentDescription: String,
    sectionCompletedCount: String,
    clearCheckedLabel: String,
    completedExpanded: Boolean,
    onCompletedExpandToggle: () -> Unit,
    onRefocusInput: () -> Unit,
    onStartEdit: (String) -> Unit,
    onCancelEdit: (String) -> Unit,
    onSaveEdit: (String, String) -> Boolean,
    onToggle: (String) -> Unit,
    onDelete: (GroceryItem, Int) -> Unit,
    onReorderStep: (String, Int) -> Unit,
    onClearChecked: () -> Unit,
    ghostPairingOffer: GroceryGhostPairing.Offer? = null,
    onAcceptGhostPairing: (List<String>) -> Unit = {},
    onDismissGhostPairing: () -> Unit = {},
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

    item(key = "update-list-header") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FamilyLogisticsSectionHeader(
                title = updateListTitle,
                titleModifier = Modifier.testTag(GroceryListTestTags.UPDATE_LIST_SECTION),
            )
            Text(
                text = updateListSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = JourneySemanticColors.inkMuted(),
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }

    if (!canWrite) {
        item(key = "viewer-notice") {
            HouseholdViewerReadOnlyNotice()
        }
    }

    if (canWrite && ghostPairingOffer != null) {
        item(key = "ghost-pairing") {
            val labels = ghostPairingOffer.suggestions.map { groceryGhostPairingItemLabel(it) }
            if (labels.isNotEmpty()) {
                GroceryGhostPairingBanner(
                    title = stringResource(Res.string.nutrition_grocery_ghost_pairing_title),
                    actionLabel = stringResource(
                        Res.string.nutrition_grocery_ghost_pairing_action,
                        formatGhostPairingItemList(labels),
                    ),
                    dismissLabel = stringResource(Res.string.nutrition_grocery_ghost_pairing_dismiss),
                    onAddSuggestions = { onAcceptGhostPairing(labels) },
                    onDismiss = onDismissGhostPairing,
                )
            }
        }
    }

    if (rawSections.completed.isNotEmpty() && totalCount > 0) {
        item(key = "shopping-mode-toggle") {
            JourneyTertiaryButton(
                onClick = onHideCheckedToggle,
                label = if (hideCheckedItems) showCheckedLabel else hideCheckedLabel,
                modifier = Modifier.testTag(GroceryListTestTags.SHOPPING_HIDE_CHECKED_TOGGLE),
            )
        }
    }

    if (totalCount == 0) {
        item(key = "empty-state") {
            JourneyEmptyState(
                title = stringResource(Res.string.nutrition_grocery_empty_title),
                body = stringResource(Res.string.nutrition_grocery_empty),
                icon = AppIcons.GroceryList,
                primaryActionLabel = if (canWrite) {
                    stringResource(Res.string.nutrition_grocery_empty_cta)
                } else {
                    null
                },
                onPrimaryAction = if (canWrite) onRefocusInput else null,
                testTag = GroceryListTestTags.EMPTY_STATE,
            )
        }
    } else {
        if (sections.active.isNotEmpty()) {
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
                    dragHandleContentDescription = dragHandleContentDescription,
                    enableReorder = canWrite && sections.active.size > 1,
                    onReorderStep = { direction -> onReorderStep(item.id, direction) },
                    onStartEdit = { onStartEdit(item.id) },
                    onCancelEdit = { onCancelEdit(item.id) },
                    onSaveEdit = { label -> onSaveEdit(item.id, label) },
                    onToggle = { onToggle(item.id) },
                    onDelete = { onDelete(item, index) },
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
                    color = JourneySemanticColors.inkMuted(),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                )
            }
        }

        if (sections.completed.isNotEmpty()) {
            item(key = "section-completed") {
                FamilyLogisticsSectionHeader(
                    title = sectionCompletedCount,
                    titleModifier = Modifier.testTag(GroceryListTestTags.COMPLETED_SECTION_HEADER),
                    onTitleClick = onCompletedExpandToggle,
                    actionLabel = if (canWrite) clearCheckedLabel else null,
                    actionModifier = Modifier.testTag(GroceryListTestTags.CLEAR_CHECKED_ACTION),
                    onAction = if (canWrite) onClearChecked else null,
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
                        dragHandleContentDescription = dragHandleContentDescription,
                        enableReorder = false,
                        onReorderStep = {},
                        onStartEdit = { onStartEdit(item.id) },
                        onCancelEdit = { onCancelEdit(item.id) },
                        onSaveEdit = { label -> onSaveEdit(item.id, label) },
                        onToggle = { onToggle(item.id) },
                        onDelete = { onDelete(item, index) },
                        readOnly = !canWrite,
                        showDivider = !isLastCompleted,
                        modifier = Modifier.animateItem(),
                    )
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
    dragHandleContentDescription: String,
    enableReorder: Boolean,
    onReorderStep: (Int) -> Unit,
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
        enableReorder = enableReorder,
        dragHandleContentDescription = dragHandleContentDescription,
        onReorderStep = onReorderStep,
        modifier = modifier,
    )
}
