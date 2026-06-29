package app.mymultiverse.ammo.data.remote.nutrition

import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow

interface NutritionRemoteDataSource {
    suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow>

    suspend fun upsert(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    )
}
