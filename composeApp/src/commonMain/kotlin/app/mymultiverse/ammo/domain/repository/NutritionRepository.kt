package app.mymultiverse.ammo.domain.repository

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    val householdId: String?
    val weekKey: String

    /**
     * Pulls remote state into local and flushes queued pushes.
     * Collaboration activities observed on this pull are delivered via the
     * [OfflineFirstNutritionRepository] activity sink so every caller (Home refresh,
     * pull-to-refresh, activation) emits with no caller code required.
     */
    suspend fun refreshFromRemote()

    fun observeGroceryItems(): Flow<List<GroceryItem>>
    fun observeAiGroceryItems(): Flow<List<GroceryItem>>
    fun observeMealPlan(): Flow<WeeklyMealPlan>

    suspend fun saveGroceryItems(items: List<GroceryItem>)
    suspend fun saveAiGroceryItems(items: List<GroceryItem>)
    suspend fun saveMealPlan(plan: WeeklyMealPlan)
}
