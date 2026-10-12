package app.mymultiverse.ammo.presentation.screens.nutrition

import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus

/**
 * Single predicate for the list-screen sync banner: the item builder renders it
 * iff this is true, and the scroll indexes count it iff this is true, so the
 * two cannot drift. Transient Syncing/Synced states do not occupy an item slot.
 */
internal fun shouldShowSyncBannerOnListScreen(syncStatus: NutritionSyncStatus): Boolean =
    syncStatus.shouldShowOnListScreen()

/**
 * Computes the LazyColumn index of the first day card (daily-section-header),
 * from the REAL item order in the WeeklyMealPlanScreenContent builder:
 *   `week-selector` (0), `sync-banner`?, `week-overview`, `partner-nudge`?,
 *   `viewer-notice`?, `empty-state`?, `daily-section-header`.
 * The two always-present items (week-selector + week-overview) give the base of
 * 2; each rendered optional item adds exactly one slot. The banner decision is
 * derived here from [shouldShowSyncBannerOnListScreen] — the ONE predicate the
 * builder renders the banner on — so a call site cannot feed a different
 * condition (e.g. `status != Idle`) and drift the scroll.
 */
internal fun mealPlanActiveItemListIndex(
    syncStatus: NutritionSyncStatus,
    hasPartnerNudge: Boolean,
    hasViewerNotice: Boolean,
    hasEmptyWeek: Boolean,
): Int {
    var index = 2 // week-selector (0) + week-overview always present
    if (shouldShowSyncBannerOnListScreen(syncStatus)) index++
    if (hasPartnerNudge) index++
    if (hasViewerNotice) index++
    if (hasEmptyWeek) index++
    return index
}

/**
 * Computes the LazyColumn index of the first to-buy item row, from the REAL item
 * order in the grocery builder (`groceryListContent` + `groceryShoppingListItems`):
 *   `sync-banner`?, `week-selector` (0/1), `partner-nudge`?, `to-buy-header`,
 *   `viewer-notice`?, `ghost-pairing`?, `shopping-mode-toggle`?, item rows.
 * The two always-present items (week-selector + to-buy-header) give the base of
 * 2; each rendered optional item adds one slot, then the item's own offset.
 * An empty week renders no item rows, so the callers skip on -1. The banner is
 * derived from [shouldShowSyncBannerOnListScreen], the same predicate the
 * grocery builder uses for its banner item.
 */
internal fun groceryActiveItemListIndex(
    syncStatus: NutritionSyncStatus,
    hasPartnerNudge: Boolean,
    hasViewerNotice: Boolean,
    hasGhostPairing: Boolean,
    hasShoppingModeToggle: Boolean,
    hasEmptyState: Boolean,
    activeItemIndex: Int,
): Int {
    if (hasEmptyState) return -1
    var index = 2 // week-selector + to-buy-header always present
    if (shouldShowSyncBannerOnListScreen(syncStatus)) index++ // sync banner renders above the week selector
    if (hasPartnerNudge) index++ // partner nudge renders before the to-buy header
    if (hasViewerNotice) index++
    if (hasGhostPairing) index++
    if (hasShoppingModeToggle) index++
    return index + activeItemIndex
}