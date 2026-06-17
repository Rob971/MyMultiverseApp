package app.mymultiverse.kmp.domain.sync

/**
 * High-level sync state for shared nutrition data (grocery / meal plan in a household).
 * Personal-only mode stays [Idle] with no remote session.
 */
sealed class NutritionSyncStatus {
    data object Idle : NutritionSyncStatus()

    data object Syncing : NutritionSyncStatus()

    /** Local edits are saved; one or more pushes are waiting for connectivity. */
    data class PendingPush(val pendingCount: Int) : NutritionSyncStatus()

    /** Remote API is unavailable (Supabase not configured or currently unreachable). */
    data object RemoteUnavailable : NutritionSyncStatus()
}
