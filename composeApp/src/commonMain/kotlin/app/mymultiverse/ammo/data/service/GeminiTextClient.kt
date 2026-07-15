package app.mymultiverse.ammo.data.service

/** Generic Gemini text-completion contract used by AI feature services. */
internal interface GeminiTextClient {
    /**
     * Sends [prompt] to Gemini and returns the model's plain-text output.
     * Returns [Result.failure] on any HTTP, network, or parse error.
     */
    suspend fun complete(
        prompt: String,
        maxOutputTokens: Int = 512,
        temperature: Double = 0.4,
    ): Result<String>
}
