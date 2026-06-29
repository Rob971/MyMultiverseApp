package app.mymultiverse.ammo.data.repository

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow

/**
 * Personal (non-shared) nutrition data — local device storage only.
 */
class NutritionRepositoryImpl(
    settings: Settings,
    override val weekKey: String = WeekCalendar.currentWeekKey(),
) : NutritionRepository {

    override val householdId: String? = null

    private val localStore = NutritionLocalStore(
        settings = settings,
        householdId = null,
        weekKey = weekKey,
    )

    override fun observeGroceryItems(): Flow<List<GroceryItem>> = localStore.observeGroceryItems()

    override fun observeAiGroceryItems(): Flow<List<GroceryItem>> = localStore.observeAiGroceryItems()

    override fun observeMealPlan(): Flow<WeeklyMealPlan> = localStore.observeMealPlan()

    override suspend fun refreshFromRemote() = Unit

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        localStore.saveGroceryItems(items)
    }

    override suspend fun saveAiGroceryItems(items: List<GroceryItem>) {
        localStore.saveAiGroceryItems(items)
    }

    override suspend fun saveMealPlan(plan: WeeklyMealPlan) {
        localStore.saveMealPlan(plan)
    }
}
