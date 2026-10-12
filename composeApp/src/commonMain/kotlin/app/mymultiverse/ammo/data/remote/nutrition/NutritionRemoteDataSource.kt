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

    /**
     * Claims this (household, week) as carried — the server ensures the claim
     * succeeds at most once.  Returns true when the call created the claim row,
     * false when another device already claimed the carry (unique violation).
     */
    suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean
}
