package app.mymultiverse.ammo.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeminiApiExceptionTest {

    @Test
    fun authError_isFlaggedForKeyPrompt() {
        val error = GeminiApiException(GeminiApiException.Reason.AUTH_ERROR, httpStatus = 403)

        assertTrue(error.isAuthError)
        assertEquals("gemini_auth_error", error.message)
        assertEquals(403, error.httpStatus)
    }

    @Test
    fun networkError_isNotAuthError() {
        val error = GeminiApiException(GeminiApiException.Reason.NETWORK)

        assertFalse(error.isAuthError)
        assertEquals("gemini_network_error", error.message)
    }
}
