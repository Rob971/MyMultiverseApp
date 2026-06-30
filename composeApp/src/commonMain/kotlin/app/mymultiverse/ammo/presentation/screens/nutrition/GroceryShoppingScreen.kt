package app.mymultiverse.ammo.presentation.screens.nutrition

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
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_week_next
import ammo.composeapp.generated.resources.nutrition_week_previous
import ammo.composeapp.generated.resources.nutrition_delete_item
import ammo.composeapp.generated.resources.nutrition_collaboration_actor_unknown
import ammo.composeapp.generated.resources.nutrition_collaboration_grocery_added
import ammo.composeapp.generated.resources.nutrition_collaboration_grocery_batch
import ammo.composeapp.generated.resources.nutrition_collaboration_grocery_checked
import ammo.composeapp.generated.resources.nutrition_grocery_add_button
import ammo.composeapp.generated.resources.nutrition_grocery_add_hint
import ammo.composeapp.generated.resources.nutrition_grocery_cancel_edit
import ammo.composeapp.generated.resources.nutrition_grocery_clear_checked
import ammo.composeapp.generated.resources.nutrition_grocery_clear_checked_undo
import ammo.composeapp.generated.resources.nutrition_grocery_drag_handle
import ammo.composeapp.generated.resources.nutrition_grocery_duplicate
import ammo.composeapp.generated.resources.nutrition_grocery_ghost_pairing_action
import ammo.composeapp.generated.resources.nutrition_grocery_ghost_pairing_dismiss
import ammo.composeapp.generated.resources.nutrition_grocery_ghost_pairing_title
import ammo.composeapp.generated.resources.nutrition_grocery_edit_hint
import ammo.composeapp.generated.resources.nutrition_grocery_edit_item
import ammo.composeapp.generated.resources.nutrition_grocery_empty
import ammo.composeapp.generated.resources.nutrition_grocery_empty_active
import ammo.composeapp.generated.resources.nutrition_grocery_empty_cta
import ammo.composeapp.generated.resources.nutrition_grocery_empty_title
import ammo.composeapp.generated.resources.nutrition_grocery_partner_nudge_action
import ammo.composeapp.generated.resources.nutrition_grocery_partner_nudge_body
import ammo.composeapp.generated.resources.nutrition_grocery_partner_nudge_cooldown
import ammo.composeapp.generated.resources.nutrition_grocery_partner_nudge_error
import ammo.composeapp.generated.resources.nutrition_grocery_partner_nudge_success
import ammo.composeapp.generated.resources.nutrition_grocery_partner_nudge_title
import ammo.composeapp.generated.resources.nutrition_grocery_partner_nudge_toolbar
import ammo.composeapp.generated.resources.nutrition_grocery_keep_screen_on
import ammo.composeapp.generated.resources.nutrition_grocery_keep_screen_off
import ammo.composeapp.generated.resources.nutrition_grocery_hide_checked
import ammo.composeapp.generated.resources.nutrition_grocery_show_checked
import ammo.composeapp.generated.resources.nutrition_grocery_progress
import ammo.composeapp.generated.resources.nutrition_grocery_save_edit
import ammo.composeapp.generated.resources.nutrition_grocery_section_completed_count
import ammo.composeapp.generated.resources.nutrition_grocery_section_to_buy
import ammo.composeapp.generated.resources.nutrition_grocery_title
import ammo.composeapp.generated.resources.nutrition_grocery_toggle_item
import ammo.composeapp.generated.resources.nutrition_grocery_undo_action
import ammo.composeapp.generated.resources.nutrition_grocery_undo_delete
import ammo.composeapp.generated.resources.nutrition_week_label
import org.jetbrains.compose.resources.stringResource
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.nutrition.GroceryGhostPairing
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.ammo.domain.nutrition.GroceryListPresentation
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.presentation.components.JourneyEmptyState
import app.mymultiverse.ammo.presentation.components.JourneyIcon
import app.mymultiverse.ammo.presentation.components.JourneyIconButton
import app.mymultiverse.ammo.presentation.components.JourneyTertiaryButton
import app.mymultiverse.ammo.presentation.components.WeekSelectorBanner
import app.mymultiverse.ammo.presentation.components.FamilyLogisticsSectionHeader
import app.mymultiverse.ammo.presentation.components.GroceryPartnerNudgeCard
import app.mymultiverse.ammo.presentation.components.GroceryGhostPairingBanner
import app.mymultiverse.ammo.presentation.components.GroceryPartnerNudgeTestTags
import app.mymultiverse.ammo.presentation.components.GroceryInputBar
import app.mymultiverse.ammo.presentation.components.HouseholdViewerReadOnlyNotice
import app.mymultiverse.ammo.presentation.components.GroceryItemRow
import app.mymultiverse.ammo.presentation.components.GroceryItemRowTestTags
import app.mymultiverse.ammo.presentation.components.JourneySnackbarHost
import app.mymultiverse.ammo.presentation.components.showJourneyActionSnackbar
import app.mymultiverse.ammo.presentation.components.NutritionScaffold
import app.mymultiverse.ammo.presentation.platform.KeepScreenOn
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.screenListPadding
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
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
    const val TO_BUY_SECTION = "grocery_to_buy_section"
    const val SECTION_ADD_ACTION = "grocery_section_add_action"
    /** @deprecated Use [TO_BUY_SECTION] */
    const val UPDATE_LIST_SECTION = TO_BUY_SECTION
    const val KEEP_SCREEN_ON_TOGGLE = "grocery_keep_screen_on_toggle"
    const val PARTNER_NUDGE_ACTION = "grocery_partner_nudge_action"
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
    val showPartnerNudge by screenModel.showGroceryPartnerNudge.collectAsState()
    val isNudgingPartners by screenModel.isNudgingPartners.collectAsState()
    val groceryPartnerNudgeResult by screenModel.groceryPartnerNudgeResult.collectAsState()
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
    val addButtonLabel = stringResource(Res.string.nutrition_grocery_add_button)
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
    val toBuySectionTitle = stringResource(Res.string.nutrition_grocery_section_to_buy)
    val toBuySectionSubtitle = if (totalCount > 0) {
        stringResource(Res.string.nutrition_grocery_progress, checkedCount, totalCount)
    } else {
        addHint
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
    val partnerNudgeTitle = stringResource(Res.string.nutrition_grocery_partner_nudge_title)
    val partnerNudgeBody = stringResource(Res.string.nutrition_grocery_partner_nudge_body)
    val partnerNudgeAction = stringResource(Res.string.nutrition_grocery_partner_nudge_action)
    val partnerNudgeToolbar = stringResource(Res.string.nutrition_grocery_partner_nudge_toolbar)
    val partnerNudgeSuccess = stringResource(Res.string.nutrition_grocery_partner_nudge_success)
    val partnerNudgeCooldown = stringResource(Res.string.nutrition_grocery_partner_nudge_cooldown)
    val partnerNudgeError = stringResource(Res.string.nutrition_grocery_partner_nudge_error)

    val partnerNudgeSnackbarMessage = when (groceryPartnerNudgeResult) {
        NutritionScreenModel.GroceryPartnerNudgeResult.Success -> partnerNudgeSuccess
        NutritionScreenModel.GroceryPartnerNudgeResult.Cooldown -> partnerNudgeCooldown
        NutritionScreenModel.GroceryPartnerNudgeResult.Error -> partnerNudgeError
        null -> null
    }

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

    LaunchedEffect(partnerNudgeSnackbarMessage) {
        val message = partnerNudgeSnackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        screenModel.consumeGroceryPartnerNudgeResult()
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
            val result = snackbarHostState.showJourneyActionSnackbar(
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
            val result = snackbarHostState.showJourneyActionSnackbar(
                message = clearCheckedMessage,
                actionLabel = undoLabel,
            )
            if (result == SnackbarResult.ActionPerformed) {
                screenModel.restoreGroceryItemsSnapshot(snapshot)
            }
        }
    }

    fun focusAddInput() {
        refocusInput = true
    }

    LaunchedEffect(sections.completed.size) {
        if (!completedExpandUserSet) {
            completedExpanded = sections.completed.size <= 3
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideLayout = maxWidth >= ScreenLayout.expandedMinWidth
        val showPhoneStickyInput = canWrite && !isWideLayout

        LaunchedEffect(items, pendingScrollLabel, canWrite, ghostPairingOffer, totalCount) {
            val label = pendingScrollLabel ?: return@LaunchedEffect
            if (items.none { it.label.equals(label, ignoreCase = true) }) return@LaunchedEffect

            val activeIndex = sections.active.indexOfFirst { it.label.equals(label, ignoreCase = true) }
            if (activeIndex < 0) return@LaunchedEffect

            val index = groceryActiveItemListIndex(
                canWrite = canWrite,
                hasViewerNotice = !canWrite,
                hasGhostPairing = canWrite && ghostPairingOffer != null,
                hasShoppingModeToggle = sections.completed.isNotEmpty() && totalCount > 0,
                hasEmptyState = totalCount == 0,
                activeItemIndex = activeIndex,
            )
            if (index < 0) return@LaunchedEffect

            listState.animateScrollToItem(index)
            pendingScrollLabel = null
        }

        NutritionScaffold(
            title = stringResource(Res.string.nutrition_grocery_title),
            onBack = onBack,
            showBackButton = !embeddedInTabs,
            actions = {
                if (showPartnerNudge) {
                    JourneyIconButton(
                        onClick = screenModel::nudgePartnersToUpdateGroceryList,
                        enabled = !isNudgingPartners,
                        modifier = Modifier.testTag(GroceryListTestTags.PARTNER_NUDGE_ACTION),
                    ) {
                        JourneyIcon(
                            role = AppIconRole.NotifyPartners,
                            contentDescription = partnerNudgeToolbar,
                        )
                    }
                }
                JourneyIconButton(
                    onClick = { keepScreenOn = !keepScreenOn },
                    modifier = Modifier.testTag(GroceryListTestTags.KEEP_SCREEN_ON_TOGGLE),
                ) {
                    JourneyIcon(
                        role = AppIconRole.KeepScreenOn,
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
                    aboveBottomBar = showPhoneStickyInput,
                )
            },
            bottomBar = {
                if (showPhoneStickyInput) {
                    GroceryInputBar(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        placeholder = addHint,
                        addContentDescription = addHint,
                        addButtonLabel = addButtonLabel,
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
                    showPhoneStickyInput = showPhoneStickyInput,
                    toBuySectionTitle = toBuySectionTitle,
                    toBuySectionSubtitle = toBuySectionSubtitle,
                    addButtonLabel = addButtonLabel,
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
                    onScrollToAddAndFocus = { focusAddInput() },
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
                    showPartnerNudge = showPartnerNudge,
                    partnerNudgeTitle = partnerNudgeTitle,
                    partnerNudgeBody = partnerNudgeBody,
                    partnerNudgeAction = partnerNudgeAction,
                    isNudgingPartners = isNudgingPartners,
                    onNudgePartners = screenModel::nudgePartnersToUpdateGroceryList,
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
                        addButtonLabel = addButtonLabel,
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

private fun groceryActiveItemListIndex(
    canWrite: Boolean,
    hasViewerNotice: Boolean,
    hasGhostPairing: Boolean,
    hasShoppingModeToggle: Boolean,
    hasEmptyState: Boolean,
    activeItemIndex: Int,
): Int {
    if (hasEmptyState) return -1
    var index = 1 // to-buy header after week selector (0)
    if (hasViewerNotice) index++
    if (hasGhostPairing) index++
    if (hasShoppingModeToggle) index++
    return index + activeItemIndex
}

private fun LazyListScope.groceryShoppingListItems(
    weekLabel: String,
    previousWeekLabel: String,
    nextWeekLabel: String,
    weekOffset: Int,
    screenModel: NutritionScreenModel,
    canWrite: Boolean,
    showPhoneStickyInput: Boolean,
    toBuySectionTitle: String,
    toBuySectionSubtitle: String,
    addButtonLabel: String,
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
    onScrollToAddAndFocus: () -> Unit,
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
    showPartnerNudge: Boolean = false,
    partnerNudgeTitle: String = "",
    partnerNudgeBody: String = "",
    partnerNudgeAction: String = "",
    isNudgingPartners: Boolean = false,
    onNudgePartners: () -> Unit = {},
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

    if (showPartnerNudge) {
        item(key = "partner-nudge") {
            GroceryPartnerNudgeCard(
                title = partnerNudgeTitle,
                body = partnerNudgeBody,
                actionLabel = partnerNudgeAction,
                onNudgePartners = onNudgePartners,
                loading = isNudgingPartners,
            )
        }
    }

    item(key = "to-buy-header") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FamilyLogisticsSectionHeader(
                title = toBuySectionTitle,
                titleModifier = Modifier.testTag(GroceryListTestTags.TO_BUY_SECTION),
                actionLabel = if (showPhoneStickyInput) addButtonLabel else null,
                actionModifier = Modifier.testTag(GroceryListTestTags.SECTION_ADD_ACTION),
                onAction = if (showPhoneStickyInput) onScrollToAddAndFocus else null,
            )
            Text(
                text = toBuySectionSubtitle,
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
                onPrimaryAction = if (canWrite) onScrollToAddAndFocus else null,
                primaryActionIcon = AppIcons.Add,
                primaryActionIconRole = AppIconRole.OnAccent,
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.nutrition_grocery_empty_active),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    )
                    if (canWrite) {
                        JourneyTertiaryButton(
                            onClick = onScrollToAddAndFocus,
                            label = stringResource(Res.string.nutrition_grocery_empty_cta),
                        )
                    }
                }
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
