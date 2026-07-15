package app.mymultiverse.ammo.data.service

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GeminiModelConfigTest {

    @Test
    fun modelId_usesSupportedFlashLiteModel() {
        assertEquals("gemini-3.1-flash-lite", GeminiModelConfig.MODEL_ID)
    }

    @Test
    fun generateContentUrl_includesModelId() {
        assertContains(GeminiModelConfig.GENERATE_CONTENT_URL, GeminiModelConfig.MODEL_ID)
        assertContains(GeminiModelConfig.GENERATE_CONTENT_URL, "generativelanguage.googleapis.com")
        assertContains(GeminiModelConfig.GENERATE_CONTENT_URL, ":generateContent")
    }
}
