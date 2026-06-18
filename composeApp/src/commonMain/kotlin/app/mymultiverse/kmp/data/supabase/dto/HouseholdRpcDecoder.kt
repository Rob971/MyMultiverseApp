package app.mymultiverse.kmp.data.supabase.dto

import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object HouseholdRpcDecoder {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun decode(result: PostgrestResult): HouseholdRpcRow =
        decodePayload(result, "ensure_household_decode_failed")

    fun decodeMembership(result: PostgrestResult): HouseholdMembershipRpcRow =
        decodePayload(result, "household_membership_decode_failed")

    fun decodeInviteResult(result: PostgrestResult): InviteHouseholdMemberRpcRow =
        decodePayload(result, "invite_household_member_decode_failed")

    fun decodeInvitePreview(result: PostgrestResult): PreviewHouseholdInviteRpcRow =
        decodePayload(result, "preview_household_invite_decode_failed")

    fun decodePendingInvites(result: PostgrestResult): List<PendingHouseholdInviteRpcRow> =
        runCatching { result.decodeList<PendingHouseholdInviteRpcRow>() }
            .recoverCatching { listOf(json.decodeFromString<PendingHouseholdInviteRpcRow>(result.data)) }
            .recoverCatching { json.decodeFromString<List<PendingHouseholdInviteRpcRow>>(result.data) }
            .getOrElse { error ->
                throw IllegalStateException(
                    "list_my_pending_household_invites_decode_failed: ${error.message}",
                    error,
                )
            }

    private inline fun <reified T> decodePayload(
        result: PostgrestResult,
        failureLabel: String,
    ): T =
        runCatching { result.decodeSingle<T>() }
            .recoverCatching { result.decodeList<T>().single() }
            .recoverCatching { json.decodeFromString<T>(result.data) }
            .getOrElse { error ->
                throw IllegalStateException(
                    "$failureLabel: ${error.message}",
                    error,
                )
            }
}
