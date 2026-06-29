package app.mymultiverse.ammo.data.supabase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnconfiguredAuthRepositoryTest {

    @Test
    fun sendEmailOtp_failsWhenSupabaseIsNotConfigured() = runTest {
        val repository = UnconfiguredAuthRepository()

        val result = repository.sendEmailOtp("user@example.com")

        assertTrue(result.isFailure)
        assertEquals("supabase_not_configured", result.exceptionOrNull()?.message)
    }

    @Test
    fun verifyEmailOtp_failsWhenSupabaseIsNotConfigured() = runTest {
        val repository = UnconfiguredAuthRepository()

        val result = repository.verifyEmailOtp("user@example.com", "123456")

        assertTrue(result.isFailure)
        assertEquals("supabase_not_configured", result.exceptionOrNull()?.message)
    }
}
