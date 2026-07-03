package app.mymultiverse.ammo.data.supabase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthMetadataDisplayNameTest {

    @Test
    fun authMetadataDisplayName_prefersFullName() {
        val metadata = buildJsonObject {
            put("full_name", "Roberto Cornano")
            put("given_name", "Roberto")
            put("family_name", "Cornano")
        }
        assertEquals("Roberto Cornano", authMetadataDisplayName(metadata))
    }

    @Test
    fun authMetadataDisplayName_combinesGivenAndFamilyNames() {
        val metadata = buildJsonObject {
            put("given_name", "Roberto")
            put("family_name", "Cornano")
        }
        assertEquals("Roberto Cornano", authMetadataDisplayName(metadata))
    }

    @Test
    fun authMetadataDisplayName_ignoresDeletedSentinel() {
        val metadata = buildJsonObject {
            put("full_name", DELETED_PROFILE_DISPLAY_NAME)
            put("nickname", "Nico")
        }
        assertEquals("Nico", authMetadataDisplayName(metadata))
    }
}
