package app.mymultiverse.kmp.presentation.di

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser

class FakeAuthRepositoryOtpTest {

    @Test
    fun verifyEmailOtp_authenticatesWithTrimmedEmail() = runTest {
        val repository = FakeAuthRepository()

        val result = repository.verifyEmailOtp("  user@example.com  ", "123456")

        assertTrue(result.isSuccess)
        val authenticated = repository.authState.value
        assertIs<AuthState.Authenticated>(authenticated)
        assertEquals(
            AuthUser(id = "fake-user", email = "user@example.com", displayName = "Test User"),
            authenticated.user,
        )
    }

    @Test
    fun sendEmailOtp_succeedsWithoutChangingAuthState() = runTest {
        val repository = FakeAuthRepository()

        val result = repository.sendEmailOtp("user@example.com")

        assertTrue(result.isSuccess)
        assertEquals(AuthState.Unauthenticated, repository.authState.value)
    }
}
