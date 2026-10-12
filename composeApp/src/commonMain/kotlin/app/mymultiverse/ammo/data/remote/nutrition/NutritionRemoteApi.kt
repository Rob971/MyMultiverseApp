package app.mymultiverse.ammo.data.remote.nutrition

import app.mymultiverse.ammo.data.supabase.ensureCurrentProfile
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase PostgREST access for shared nutrition week payloads.
 * No local persistence — callers own offline/cache logic.
 */
class NutritionRemoteApi(
    private val client: SupabaseClient,
) : NutritionRemoteDataSource {
    override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
        requireAuthenticatedUserId()
        return client.postgrest["nutrition_household_week_data"]
            .select(Columns.ALL) {
                filter {
                    eq("household_id", householdId)
                    eq("week_key", weekKey)
                }
            }
            .decodeList<NutritionWeekDataRow>()
    }

    override suspend fun upsert(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
    ) {
        val userId = requireAuthenticatedUserId()
        ensureProfile(userId)
        client.postgrest["nutrition_household_week_data"]
            .upsert(
                NutritionWeekDataRow(
                    householdId = householdId,
                    weekKey = weekKey,
                    dataKind = dataKind,
                    payload = payload,
                    updatedAt = Clock.System.now().toString(),
                    updatedBy = userId,
                ),
            ) {
                onConflict = "household_id,week_key,data_kind"
            }
    }

    override suspend fun pushIfVersion(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
        expectedVersion: Long?,
    ): PushResult {
        if (expectedVersion == null) {
            // First write: INSERT. 23505 = another device already created the row.
            val userId = requireAuthenticatedUserId()
            ensureProfile(userId)
            return try {
                client.postgrest["nutrition_household_week_data"]
                    .insert(
                        NutritionWeekDataRow(
                            householdId = householdId,
                            weekKey = weekKey,
                            dataKind = dataKind,
                            payload = payload,
                            updatedAt = Clock.System.now().toString(),
                            updatedBy = userId,
                        ),
                    ) {
                        select(Columns.raw("version"))
                    }
                    .decodeSingleOrNull<VersionRow>()
                // insert succeeded; version = 1 (column default)
                PushResult.ok(1)
            } catch (e: PostgrestRestException) {
                if (e.code == "23505") PushResult.conflict()
                else throw e
            }
        }
        // Conditional UPDATE: only if version matches what the client last saw.
        val userId = requireAuthenticatedUserId()
        ensureProfile(userId)
        val result = client.postgrest["nutrition_household_week_data"]
            .update(
                NutritionWeekDataRow(
                    householdId = householdId,
                    weekKey = weekKey,
                    dataKind = dataKind,
                    payload = payload,
                    updatedAt = Clock.System.now().toString(),
                    updatedBy = userId,
                ),
            ) {
                filter {
                    eq("household_id", householdId)
                    eq("week_key", weekKey)
                    eq("data_kind", dataKind)
                    eq("version", expectedVersion.toString())
                }
                select(Columns.raw("version"))
            }
            .decodeSingleOrNull<VersionRow>()
        return if (result != null) PushResult.ok(result.version) else PushResult.conflict()
    }

    override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean {
        val userId = requireAuthenticatedUserId()
        ensureProfile(userId)
        return try {
            client.postgrest["nutrition_household_week_carry"]
                .insert(CarryClaim(householdId = householdId, weekKey = weekKey))
            true
        } catch (e: PostgrestRestException) {
            // 23505 = unique_violation — someone already claimed this week
            if (e.code == "23505") false
            else throw e
        }
    }

    @Serializable
    internal data class CarryClaim(
        @SerialName("household_id") val householdId: String,
        @SerialName("week_key") val weekKey: String,
    )

    /**
     * PostgREST response when selecting only the version column.
     * The returned JSON is [{"version": N}].
     */
    @Serializable
    internal data class VersionRow(
        val version: Long,
    )

    private suspend fun requireAuthenticatedUserId(): String {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("auth_required")
    }

    private suspend fun ensureProfile(userId: String) {
        client.ensureCurrentProfile(userId)
    }
}
