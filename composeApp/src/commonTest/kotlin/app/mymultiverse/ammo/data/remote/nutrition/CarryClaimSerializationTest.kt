package app.mymultiverse.ammo.data.remote.nutrition

import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Guards the wire shape of Supabase DTOs. The Supabase client is
 * installed with Postgrest defaults (SupabaseClientFactory.kt:35) and the
 * kotlinx default Json has no naming strategy, so the serialized keys come
 * straight from the DTO — they must match the table columns or PostgREST
 * answers 400.
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

    @Test
    fun versionFieldSerializesAsSnakeCase() {
        val row = NutritionWeekDataRow(
            householdId = "hh-1",
            weekKey = "2026-W40",
            dataKind = "grocery",
            payload = "a",
            version = 42,
        )
        val body = Json.encodeToString(row)
        assertTrue(
            body.contains("\"version\":"),
            "version field must serialize to snake_case 'version', got: $body",
        )
    }

    @Test
    fun versionFieldAcceptsNull() {
        val row = NutritionWeekDataRow(
            householdId = "hh-1",
            weekKey = "2026-W40",
            dataKind = "grocery",
            payload = "a",
        )
        val body = Json.encodeToString(row)
        assertTrue(
            !body.contains("\"version\":"),
            "null version should be omitted from JSON, got: $body",
        )
    }

    @Test
    fun versionRowDecodesSuccessBody() {
        val row = Json.decodeFromString<NutritionRemoteApi.VersionRow>("""{"version":2}""")
        assertEquals(2L, row.version)
    }

    @Test
    fun versionRowDecodesNullBody() {
        // Empty array [] is what PostgREST returns when 0 rows matched.
        // decodeSingleOrNull -> null; decodeList -> empty list.
        val rows = Json.decodeFromString<List<NutritionRemoteApi.VersionRow>>("""[]""")
        assertTrue(rows.isEmpty(), "empty array must decode to empty list")
        assertNull(rows.firstOrNull(), "firstOrNull on empty list returns null")
    }
}