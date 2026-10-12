package app.mymultiverse.ammo.domain.sync

/**
 * High-level sync state for shared nutrition data (grocery / meal plan in a household).
 * Personal-only mode stays [Idle] with no remote session.
 */
sealed class NutritionSyncStatus {
    data object Idle : NutritionSyncStatus()

    data object Syncing : NutritionSyncStatus()

    /** Local edits are saved; one or more pushes are waiting for connectivity. */
    data class PendingPush(val pendingCount: Int) : NutritionSyncStatus()

    /** Remote API is unavailable (not configured or currently unreachable). */
    data object RemoteUnavailable : NutritionSyncStatus()

    /** Brief confirmation after pending edits were shared successfully. */
    data object Synced : NutritionSyncStatus()

    /**
     * Whether the sync banner should be rendered on list screens (grocery, meal plan).
     * Only persistent states that the user must know about are shown here;
     * transient Syncing/Synced states remain on the hub where nothing is being tapped.
     */
    fun shouldShowOnListScreen(): Boolean =
        this is PendingPush || this is RemoteUnavailable
}
