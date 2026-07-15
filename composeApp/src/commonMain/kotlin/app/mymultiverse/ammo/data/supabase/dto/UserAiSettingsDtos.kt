package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row returned by the `get_gemini_api_key()` RPC (single-row table result). */
@Serializable
internal data class GeminiKeyRow(val key: String = "")

/** Parameters for the `upsert_gemini_api_key(p_key)` RPC. */
@Serializable
internal data class UpsertGeminiKeyParams(@SerialName("p_key") val key: String)
