package app.mymultiverse.kmp.data.supabase.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class HouseholdMembershipRpcMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodesNoneMembershipStatus() {
        val row = json.decodeFromString<HouseholdMembershipRpcRow>(
            """{"status":"none"}""",
        )

        assertEquals("none", row.status)
    }

    @Test
    fun decodesActiveMembershipStatus() {
        val row = json.decodeFromString<HouseholdMembershipRpcRow>(
            """
            {
              "status": "active",
              "space_id": "space-1",
              "space_name": "Rossi home",
              "owner_id": "owner-1",
              "owner_display_name": "Roberto",
              "role": "owner",
              "features": ["grocery", "meal_plan"]
            }
            """.trimIndent(),
        )

        assertEquals("active", row.status)
        assertEquals("space-1", row.spaceId)
        assertEquals("owner", row.role)
    }
}
