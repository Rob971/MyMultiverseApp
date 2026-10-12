package app.mymultiverse.ammo.data.remote.nutrition

import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow

/**
 * Result of a conditional write (compare-and-swap by version).
 * ok = true -> write succeeded, newVersion is the server's updated version.
 * ok = false, failed = false -> CAS conflict (stale version or duplicate insert).
 * ok = false, failed = true -> network/transient error, do not retry loop.
 */
data class PushResult(
    val ok: Boolean,
    val newVersion: Long = 0,
    val failed: Boolean = false,
) {
    companion object {
        fun conflict(): PushResult = PushResult(false)
        fun ok(newVersion: Long): PushResult = PushResult(true, newVersion)
        fun failed(): PushResult = PushResult(false, failed = true)
    }
}

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

    /**
     * Compare-and-swap write of grocery/ai_grocery payload.
     *
     * @param expectedVersion the version the client last saw for this row,
     *   or null if the client has never seen a row (should insert).
     * @return PushResult(ok=true, newVersion) on success,
     *   PushResult(ok=false) on stale-version or duplicate insert,
     *   PushResult(failed=true) on network/transient error.
     *
     * Default implementation delegates to blind [upsert] for backward
     * compatibility with fakes that do not test CAS. Override in the real
     * implementation ([NutritionRemoteApi]) and in CAS-capable fakes.
     */
    suspend fun pushIfVersion(
        householdId: String,
        weekKey: String,
        dataKind: String,
        payload: String,
        expectedVersion: Long?,
    ): PushResult {
        upsert(householdId, weekKey, dataKind, payload)
        return PushResult.ok(0)
    }
}
