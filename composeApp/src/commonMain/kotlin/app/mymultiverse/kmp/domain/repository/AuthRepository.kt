package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.auth.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun restoreSession()

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun signInWithApple(): Result<Unit>

    suspend fun signOut()
}
