package app.mymultiverse.kmp.ui

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InstrumentedFakeAuthRepository(
    initialState: AuthState,
) : AuthRepository {
    private val _authState = MutableStateFlow(initialState)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun restoreSession() = Unit

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun sendEmailOtp(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun verifyEmailOtp(email: String, code: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signInWithGoogle(): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signInWithApple(): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun exportPersonalData(): Result<String> = Result.success("{}")

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}
