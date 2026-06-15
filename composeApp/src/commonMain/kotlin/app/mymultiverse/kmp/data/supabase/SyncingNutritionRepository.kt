package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.repository.NutritionStorageCodec
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import app.mymultiverse.kmp.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.kmp.domain.nutrition.WeekCalendar
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncingNutritionRepository(
    override val spaceId: String,
    private val client: SupabaseClient,
    private val settings: Settings,
    override val weekKey: String = WeekCalendar.currentWeekKey(),
) : NutritionRepository {

    private val groceryKey = "nutrition_${spaceId}_grocery_$weekKey"
    private val aiGroceryKey = "nutrition_${spaceId}_ai_grocery_$weekKey"
    private val mealPlanKey = "nutrition_${spaceId}_meal_plan_$weekKey"

    private val _groceryItems = MutableStateFlow(loadGrocery())
    private val _aiGroceryItems = MutableStateFlow(loadAiGrocery())
    private val _mealPlan = MutableStateFlow(loadMealPlan())

    override fun observeGroceryItems(): Flow<List<GroceryItem>> = _groceryItems.asStateFlow()

    override fun observeAiGroceryItems(): Flow<List<GroceryItem>> = _aiGroceryItems.asStateFlow()

    override fun observeMealPlan(): Flow<WeeklyMealPlan> = _mealPlan.asStateFlow()

    override suspend fun refreshFromRemote() {
        val rows = client.postgrest["nutrition_space_week_data"]
            .select(Columns.ALL) {
                filter {
                    eq("space_id", spaceId)
                    eq("week_key", weekKey)
                }
            }
            .decodeList<NutritionWeekDataRow>()

        rows.forEach { row ->
            when (row.dataKind) {
                "grocery" -> {
                    val items = NutritionStorageCodec.decodeGrocery(row.payload)
                    settings.putString(groceryKey, NutritionStorageCodec.encodeGrocery(items))
                    _groceryItems.value = items
                }
                "ai_grocery" -> {
                    val items = NutritionStorageCodec.decodeGrocery(row.payload)
                    settings.putString(aiGroceryKey, NutritionStorageCodec.encodeGrocery(items))
                    _aiGroceryItems.value = items
                }
                "meal_plan" -> {
                    val plan = NutritionStorageCodec.decodeMealPlan(weekKey, row.payload)
                    settings.putString(mealPlanKey, NutritionStorageCodec.encodeMealPlan(plan))
                    _mealPlan.value = plan
                }
            }
        }
    }

    override suspend fun saveGroceryItems(items: List<GroceryItem>) {
        val payload = NutritionStorageCodec.encodeGrocery(items)
        settings.putString(groceryKey, payload)
        _groceryItems.value = items
        upsertRemote(kind = "grocery", payload = payload)
    }

    override suspend fun saveAiGroceryItems(items: List<GroceryItem>) {
        val payload = NutritionStorageCodec.encodeGrocery(items)
        settings.putString(aiGroceryKey, payload)
        _aiGroceryItems.value = items
        upsertRemote(kind = "ai_grocery", payload = payload)
    }

    override suspend fun saveMealPlan(plan: WeeklyMealPlan) {
        val payload = NutritionStorageCodec.encodeMealPlan(plan)
        settings.putString(mealPlanKey, payload)
        _mealPlan.value = plan
        upsertRemote(kind = "meal_plan", payload = payload)
    }

    private suspend fun upsertRemote(kind: String, payload: String) {
        client.postgrest["nutrition_space_week_data"]
            .upsert(
                NutritionWeekDataRow(
                    spaceId = spaceId,
                    weekKey = weekKey,
                    dataKind = kind,
                    payload = payload,
                    updatedBy = client.auth.currentUserOrNull()?.id,
                ),
            ) {
                onConflict = "space_id,week_key,data_kind"
            }
    }

    private fun loadGrocery(): List<GroceryItem> =
        NutritionStorageCodec.decodeGrocery(settings.getStringOrNull(groceryKey))

    private fun loadAiGrocery(): List<GroceryItem> =
        NutritionStorageCodec.decodeGrocery(settings.getStringOrNull(aiGroceryKey))

    private fun loadMealPlan(): WeeklyMealPlan =
        NutritionStorageCodec.decodeMealPlan(weekKey, settings.getStringOrNull(mealPlanKey))
}
