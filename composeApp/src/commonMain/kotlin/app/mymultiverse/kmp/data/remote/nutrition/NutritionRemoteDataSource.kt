package app.mymultiverse.kmp.data.remote.nutrition

import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow

interface NutritionRemoteDataSource {
    suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow>

    suspend fun upsert(
        spaceId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    )
}
