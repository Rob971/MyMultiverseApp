package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository(
    initialState: AuthState = AuthState.Unauthenticated,
) : AuthRepository {
    private val _authState = MutableStateFlow(initialState)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun restoreSession() = Unit

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("invalid_credentials"))
        }
        _authState.value = AuthState.Authenticated(
            AuthUser(
                id = "fake-user",
                email = email.trim(),
                displayName = "Test User",
            ),
        )
        return Result.success(Unit)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        signInWithEmail(email, password)

    override suspend fun signInWithGoogle(): Result<Unit> =
        Result.failure(UnsupportedOperationException("google_oauth_not_configured"))

    override suspend fun signInWithApple(): Result<Unit> =
        Result.failure(UnsupportedOperationException("apple_oauth_not_configured"))

    override suspend fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }
}
