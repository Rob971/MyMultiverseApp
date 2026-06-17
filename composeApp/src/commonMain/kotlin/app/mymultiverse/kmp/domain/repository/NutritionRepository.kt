package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    val householdId: String?
    val weekKey: String

    suspend fun refreshFromRemote()

    fun observeGroceryItems(): Flow<List<GroceryItem>>
    fun observeAiGroceryItems(): Flow<List<GroceryItem>>
    fun observeMealPlan(): Flow<WeeklyMealPlan>

    suspend fun saveGroceryItems(items: List<GroceryItem>)
    suspend fun saveAiGroceryItems(items: List<GroceryItem>)
    suspend fun saveMealPlan(plan: WeeklyMealPlan)
}
