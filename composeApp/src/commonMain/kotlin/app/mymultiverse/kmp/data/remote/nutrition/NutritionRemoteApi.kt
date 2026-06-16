package app.mymultiverse.kmp.data.remote.nutrition

import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.data.supabase.dto.ProfileInsertRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.datetime.Clock

/**
 * Supabase PostgREST access for shared nutrition week payloads.
 * No local persistence — callers own offline/cache logic.
 */
class NutritionRemoteApi(
    private val client: SupabaseClient,
) : NutritionRemoteDataSource {
    override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> {
        requireAuthenticatedUserId()
        return client.postgrest["nutrition_space_week_data"]
            .select(Columns.ALL) {
                filter {
                    eq("space_id", spaceId)
                    eq("week_key", weekKey)
                }
            }
            .decodeList<NutritionWeekDataRow>()
    }

    override suspend fun upsert(
        spaceId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    ) {
        val userId = requireAuthenticatedUserId()
        ensureProfile(userId)
        client.postgrest["nutrition_space_week_data"]
            .upsert(
                NutritionWeekDataRow(
                    spaceId = spaceId,
                    weekKey = weekKey,
                    dataKind = dataKind,
                    payload = payload,
                    updatedAt = Clock.System.now().toString(),
                    updatedBy = userId,
                ),
            ) {
                onConflict = "space_id,week_key,data_kind"
            }
    }

    private suspend fun requireAuthenticatedUserId(): String {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("auth_required")
    }

    private suspend fun ensureProfile(userId: String) {
        val email = client.auth.currentUserOrNull()?.email
        client.postgrest["profiles"]
            .upsert(
                ProfileInsertRow(
                    id = userId,
                    email = email,
                    displayName = email?.substringBefore("@"),
                ),
            ) {
                onConflict = "id"
            }
    }
}
