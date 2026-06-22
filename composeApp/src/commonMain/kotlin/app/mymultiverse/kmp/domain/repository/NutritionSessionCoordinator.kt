package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.nutrition.NutritionCollaborationActivity
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Presentation-facing port for nutrition data scoped to personal use or a household.
 * Hides local/remote/sync wiring from screen models.
 */
interface NutritionSessionCoordinator {
    val nutrition: StateFlow<NutritionRepository>

    fun observeSyncStatus(): Flow<NutritionSyncStatus>

    fun observeCollaborationActivity(): Flow<NutritionCollaborationActivity>

    suspend fun activateHousehold(householdId: String)

    suspend fun selectWeek(weekKey: String)

    fun deactivate()
}
