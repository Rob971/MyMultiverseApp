package app.mymultiverse.kmp.data.remote.nutrition

import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Supabase PostgREST access for shared nutrition week payloads.
 * No local persistence — callers own offline/cache logic.
 */
class NutritionRemoteApi(
    private val client: SupabaseClient,
) : NutritionRemoteDataSource {
    override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> =
        client.postgrest["nutrition_space_week_data"]
            .select(Columns.ALL) {
                filter {
                    eq("space_id", spaceId)
                    eq("week_key", weekKey)
                }
            }
            .decodeList<NutritionWeekDataRow>()

    override suspend fun upsert(
        spaceId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    ) {
        client.postgrest["nutrition_space_week_data"]
            .upsert(
                NutritionWeekDataRow(
                    spaceId = spaceId,
                    weekKey = weekKey,
                    dataKind = dataKind,
                    payload = payload,
                    updatedBy = client.auth.currentUserOrNull()?.id,
                ),
            ) {
                onConflict = "space_id,week_key,data_kind"
            }
    }
}
