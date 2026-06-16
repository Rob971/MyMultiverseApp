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
        runCatching { result.decodeSingle<HouseholdRpcRow>() }
            .recoverCatching { result.decodeList<HouseholdRpcRow>().single() }
            .recoverCatching { json.decodeFromString<HouseholdRpcRow>(result.data) }
            .getOrElse { error ->
                throw IllegalStateException(
                    "ensure_household_decode_failed: ${error.message}",
                    error,
                )
            }
}
