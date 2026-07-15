package app.mymultiverse.ammo.data.service

/**
 * Gemini REST model used for nutrition AI (meal plans, grocery lists, ingredients, advice).
 *
 * `gemini-2.0-flash-lite` was retired 2026-06-01; keep [MODEL_ID] on a supported free-tier model.
 */
internal object GeminiModelConfig {
    const val MODEL_ID = "gemini-3.1-flash-lite"

    const val GENERATE_CONTENT_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_ID:generateContent"
}
