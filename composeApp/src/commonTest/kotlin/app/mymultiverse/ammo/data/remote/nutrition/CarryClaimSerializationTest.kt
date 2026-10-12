package app.mymultiverse.ammo.data.remote.nutrition

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Guards the wire shape of the carry-claim insert body. The Supabase client is
 * installed with Postgrest defaults (SupabaseClientFactory.kt:35) and the
 * kotlinx default Json has no naming strategy, so the serialized keys come
 * straight from the DTO — they must match the table columns household_id /
 * week_key or PostgREST answers 400 and every claim is rethrown (no carry ever).
 */
class CarryClaimSerializationTest {

    @Test
    fun serializesWithSnakeCaseColumnNames() {
        val body = Json.encodeToString(
            NutritionRemoteApi.CarryClaim(householdId = "household-1", weekKey = "2026-06-15"),
        )
        assertTrue(
            body.contains("\"household_id\":"),
            "insert body must contain household_id column key, got: $body",
        )
        assertTrue(
            body.contains("\"week_key\":"),
            "insert body must contain week_key column key, got: $body",
        )
        assertTrue(
            !body.contains("\"householdId\"") && !body.contains("\"weekKey\""),
            "camelCase keys must not leak into the insert body, got: $body",
        )
    }
}