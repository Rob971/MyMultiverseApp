package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun restoreSession()

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun signInWithApple(): Result<Unit>

    suspend fun signOut()

    /** GDPR: exports profile and household affiliation metadata as JSON. */
    suspend fun exportPersonalData(): Result<String>
}
