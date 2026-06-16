package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Presentation-facing port for nutrition data scoped to personal use or a sharing space.
 * Hides local/remote/sync wiring from screen models.
 */
interface NutritionSessionCoordinator {
    val nutrition: StateFlow<NutritionRepository>

    fun observeSyncStatus(): Flow<NutritionSyncStatus>

    suspend fun activateSpace(spaceId: String)

    fun deactivate()
}
