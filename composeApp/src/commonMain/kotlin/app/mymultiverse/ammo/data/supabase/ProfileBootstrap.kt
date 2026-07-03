package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.data.supabase.dto.ProfileDisplayNameUpdateRow
import app.mymultiverse.ammo.data.supabase.dto.ProfileInsertRow
import app.mymultiverse.ammo.data.supabase.dto.ProfileRow
import app.mymultiverse.ammo.domain.auth.resolvedDisplayName
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

internal suspend fun SupabaseClient.ensureCurrentProfile(userId: String) {
    val rpcResult = runCatching { postgrest.rpc("ensure_current_profile") }
    if (rpcResult.isFailure) {
        upsertProfileFallback(userId)
    }
    restoreDeletedProfileDisplayNameIfNeeded(userId)
}

private suspend fun SupabaseClient.upsertProfileFallback(userId: String) {
    val email = auth.currentUserOrNull()?.email
        ?: auth.currentSessionOrNull()?.user?.email
    val restoredName = auth.currentUserOrNull()?.toAuthUser()?.resolvedDisplayName()
        ?: email?.substringBefore("@")?.trim()?.takeIf { it.isNotEmpty() }
    postgrest["profiles"]
        .upsert(
            ProfileInsertRow(
                id = userId,
                email = email,
                displayName = restoredName,
            ),
        ) {
            onConflict = "id"
        }
}

private suspend fun SupabaseClient.restoreDeletedProfileDisplayNameIfNeeded(userId: String) {
    val profile = postgrest["profiles"]
        .select(Columns.ALL) {
            filter { eq("id", userId) }
        }
        .decodeSingleOrNull<ProfileRow>()
        ?: return
    if (!isDeletedProfileDisplayName(profile.displayName)) return

    val restored = auth.currentUserOrNull()?.toAuthUser()?.resolvedDisplayName()
        ?: profile.email?.substringBefore("@")?.trim()?.takeIf { it.isNotEmpty() }
        ?: return

    postgrest["profiles"]
        .update(ProfileDisplayNameUpdateRow(displayName = restored)) {
            filter { eq("id", userId) }
        }
}
