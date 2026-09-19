package app.mymultiverse.ammo.data.service

import app.mymultiverse.ammo.domain.service.GeminiApiException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AiProxyGeminiRouteTest {

    @Test
    fun target_isTheAiGenerateFunction_withTheUsersSession_andNoGeminiKey() = runTest {
        val route = AiProxyGeminiRoute("https://project.supabase.co/", "anon-key") { "user-session-token" }

        val target = route.target()

        assertEquals("https://project.supabase.co/functions/v1/ai-generate", target.url)
        // Exactly these headers: the app sends no x-goog-api-key.
        assertEquals(mapOf("Authorization" to "Bearer user-session-token", "apikey" to "anon-key"), target.headers)
    }

    @Test
    fun target_withoutASession_failsAsAnAuthError() = runTest {
        val route = AiProxyGeminiRoute("https://project.supabase.co", "anon-key") { null }

        val error = assertFailsWith<GeminiApiException> { route.target() }

        assertTrue(error.isAuthError)
    }
}
