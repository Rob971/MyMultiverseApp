package app.mymultiverse.kmp.data.local.nutrition

import app.mymultiverse.kmp.data.repository.NutritionStorageCodec
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Device-local cache for one nutrition scope (personal or household + week).
 */
class NutritionLocalStore(
    private val settings: Settings,
    val householdId: String?,
    val weekKey: String,
) {
    private val groceryKey = NutritionStorageKeys.grocery(householdId, weekKey)
    private val aiGroceryKey = NutritionStorageKeys.aiGrocery(householdId, weekKey)
    private val mealPlanKey = NutritionStorageKeys.mealPlan(householdId, weekKey)

    private val _groceryItems = MutableStateFlow(loadGrocery())
    private val _aiGroceryItems = MutableStateFlow(loadAiGrocery())
    private val _mealPlan = MutableStateFlow(loadMealPlan())

    fun observeGroceryItems(): Flow<List<GroceryItem>> = _groceryItems.asStateFlow()

    fun observeAiGroceryItems(): Flow<List<GroceryItem>> = _aiGroceryItems.asStateFlow()

    fun observeMealPlan(): Flow<WeeklyMealPlan> = _mealPlan.asStateFlow()

    fun saveGroceryItems(items: List<GroceryItem>) {
        val payload = NutritionStorageCodec.encodeGrocery(items)
        settings.putString(groceryKey, payload)
        _groceryItems.value = items
    }

    fun saveAiGroceryItems(items: List<GroceryItem>) {
        val payload = NutritionStorageCodec.encodeGrocery(items)
        settings.putString(aiGroceryKey, payload)
        _aiGroceryItems.value = items
    }

    fun saveMealPlan(plan: WeeklyMealPlan) {
        val payload = NutritionStorageCodec.encodeMealPlan(plan)
        settings.putString(mealPlanKey, payload)
        _mealPlan.value = plan
    }

    fun encodeGrocery(items: List<GroceryItem>): String = NutritionStorageCodec.encodeGrocery(items)

    fun encodeMealPlan(plan: WeeklyMealPlan): String = NutritionStorageCodec.encodeMealPlan(plan)

    fun applyPayload(dataKind: String, payload: String) {
        when (dataKind) {
            "grocery" -> saveGroceryItems(NutritionStorageCodec.decodeGrocery(payload))
            "ai_grocery" -> saveAiGroceryItems(NutritionStorageCodec.decodeGrocery(payload))
            "meal_plan" -> saveMealPlan(NutritionStorageCodec.decodeMealPlan(weekKey, payload))
        }
    }

    private fun loadGrocery(): List<GroceryItem> =
        NutritionStorageCodec.decodeGrocery(settings.getStringOrNull(groceryKey))

    private fun loadAiGrocery(): List<GroceryItem> =
        NutritionStorageCodec.decodeGrocery(settings.getStringOrNull(aiGroceryKey))

    private fun loadMealPlan(): WeeklyMealPlan =
        NutritionStorageCodec.decodeMealPlan(weekKey, settings.getStringOrNull(mealPlanKey))
}
