package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NutritionRepositoryImpl(
    private val settings: Settings,
    override val weekKey: String = WeekCalendar.currentWeekKey(),
) : NutritionRepository {

    private val groceryKey = "nutrition_grocery_$weekKey"
    private val mealPlanKey = "nutrition_meal_plan_$weekKey"

    private val _groceryItems = MutableStateFlow(loadGrocery())
    private val _mealPlan = MutableStateFlow(loadMealPlan())

    override fun observeGroceryItems(): Flow<List<GroceryItem>> = _groceryItems.asStateFlow()

    override fun observeMealPlan(): Flow<WeeklyMealPlan> = _mealPlan.asStateFlow()

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        settings.putString(groceryKey, NutritionStorageCodec.encodeGrocery(items))
        _groceryItems.value = items
    }

    override suspend fun saveMealPlan(plan: WeeklyMealPlan) {
        settings.putString(mealPlanKey, NutritionStorageCodec.encodeMealPlan(plan))
        _mealPlan.value = plan
    }

    private fun loadGrocery(): List<GroceryItem> =
        NutritionStorageCodec.decodeGrocery(settings.getStringOrNull(groceryKey))

    private fun loadMealPlan(): WeeklyMealPlan =
        NutritionStorageCodec.decodeMealPlan(weekKey, settings.getStringOrNull(mealPlanKey))
}
