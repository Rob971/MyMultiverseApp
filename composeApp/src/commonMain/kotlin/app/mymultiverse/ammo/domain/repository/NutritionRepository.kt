package app.mymultiverse.ammo.domain.repository

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    val householdId: String?
    val weekKey: String

    /**
     * Pulls remote state into local and flushes queued pushes.
     *
     * @return collaboration activities observed on this pull (currently only
     *   [NutritionCollaborationActivityKind.GroceryRemoved], and only when a base
     *   snapshot existed before the pull). Empty on a first sync or when nothing changed.
     */
    suspend fun refreshFromRemote(): List<NutritionCollaborationActivity>

    fun observeGroceryItems(): Flow<List<GroceryItem>>
    fun observeAiGroceryItems(): Flow<List<GroceryItem>>
    fun observeMealPlan(): Flow<WeeklyMealPlan>

    suspend fun saveGroceryItems(items: List<GroceryItem>)
    suspend fun saveAiGroceryItems(items: List<GroceryItem>)
    suspend fun saveMealPlan(plan: WeeklyMealPlan)
}
