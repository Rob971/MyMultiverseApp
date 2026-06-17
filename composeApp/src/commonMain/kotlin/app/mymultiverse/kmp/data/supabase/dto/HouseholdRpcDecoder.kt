package app.mymultiverse.kmp.data.supabase.dto

import io.github.jan.supabase.postgrest.result.PostgrestResult
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
