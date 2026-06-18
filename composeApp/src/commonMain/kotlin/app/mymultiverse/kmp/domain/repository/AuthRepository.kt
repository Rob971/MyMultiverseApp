package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>

    suspend fun restoreSession()

    suspend fun signInWithEmail(email: String, password: String): Result<Unit>

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit>

    suspend fun sendEmailOtp(email: String): Result<Unit>

    suspend fun verifyEmailOtp(email: String, code: String): Result<Unit>

    suspend fun signInWithGoogle(): Result<Unit>

    suspend fun signInWithApple(): Result<Unit>

    suspend fun signOut()

    /** GDPR: exports profile and household affiliation metadata as JSON. */
    suspend fun exportPersonalData(): Result<String>

    /** GDPR: leaves/dissolves household, removes PII, deletes auth user via Edge Function. */
    suspend fun deleteAccount(): Result<Unit>
}
