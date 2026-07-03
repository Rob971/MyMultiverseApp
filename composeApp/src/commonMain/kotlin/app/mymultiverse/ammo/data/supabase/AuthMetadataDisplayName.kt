package app.mymultiverse.ammo.data.supabase

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal fun authMetadataDisplayName(userMetadata: JsonObject?): String? {
    if (userMetadata == null) return null

    listOf("full_name", "name", "preferred_username", "nickname").forEach { key ->
        userMetadata[key]?.jsonPrimitive?.contentOrNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !isDeletedProfileDisplayName(it) }
            ?.let { return it }
    }

    val given = userMetadata["given_name"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    val family = userMetadata["family_name"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
    val combined = listOf(given, family).filter { it.isNotEmpty() }.joinToString(" ")
    return combined.takeIf { it.isNotEmpty() && !isDeletedProfileDisplayName(it) }
}
