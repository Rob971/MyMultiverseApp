package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository(
    initialState: AuthState = AuthState.Unauthenticated,
) : AuthRepository {
    private val _authState = MutableStateFlow(initialState)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var exportPersonalDataResult: Result<String>? = null
    var deleteAccountResult: Result<Unit>? = null
    var deleteAccountCalls: Int = 0
        private set

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

    override suspend fun sendEmailOtp(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun verifyEmailOtp(email: String, code: String): Result<Unit> =
        signInWithEmail(email, code)

    override suspend fun signInWithGoogle(): Result<Unit> =
        Result.failure(UnsupportedOperationException("google_oauth_not_configured"))

    override suspend fun signInWithApple(): Result<Unit> =
        Result.failure(UnsupportedOperationException("apple_oauth_not_configured"))

    override suspend fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun exportPersonalData(): Result<String> =
        exportPersonalDataResult ?: Result.success("""{"exported_at":"test","profile":{"email":"test@example.com"}}""")

    override suspend fun deleteAccount(): Result<Unit> {
        deleteAccountCalls++
        return deleteAccountResult ?: Result.success(Unit)
    }
}
