package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnconfiguredAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.ConfigurationMissing)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun restoreSession() = Unit

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun signInWithGoogle(): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun signInWithApple(): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun signOut() = Unit
}
