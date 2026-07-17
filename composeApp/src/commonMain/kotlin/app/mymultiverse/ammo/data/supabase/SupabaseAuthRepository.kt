package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.domain.auth.AuthFailureCodes
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.coroutines.runCatchingCancellable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SupabaseAuthRepository(
    private val client: SupabaseClient,
    private val scope: CoroutineScope,
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        client.auth.sessionStatus
            .onEach { status ->
                _authState.value = mapSessionStatus(status)
                if (status is SessionStatus.Authenticated) {
                    scope.launch { bootstrapCurrentProfile() }
                }
            }
            .launchIn(scope)

        AuthRedirectEvents.urls
            .onEach { url -> processOAuthRedirect(url) }
            .launchIn(scope)
    }

    override suspend fun restoreSession() {
        try {
            client.auth.awaitInitialization()
            AuthRedirectEvents.consumePending()?.let { processOAuthRedirect(it) }
            syncAuthStateFromCurrentSession()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Session restore is best-effort; fall through to the Unauthenticated guard below.
        }
        if (_authState.value == AuthState.Loading && currentAuthUser() == null) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatchingCancellable {
            client.auth.awaitInitialization()
            client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
            syncAuthStateFromCurrentSession()
            checkNotNull(currentAuthUser()) { "sign_in_failed" }
        }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): Result<Unit> =
        runCatchingCancellable {
            client.auth.awaitInitialization()
            client.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password
                if (!displayName.isNullOrBlank()) {
                    this.data = buildJsonObject { put("full_name", displayName.trim()) }
                }
            }
            val user = currentAuthUser()
            if (user != null) {
                syncAuthStateFromCurrentSession()
            } else {
                throw IllegalStateException(AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED)
            }
        }

    override suspend fun sendEmailOtp(email: String): Result<Unit> =
        runCatchingCancellable {
            client.auth.awaitInitialization()
            client.auth.signInWith(OTP) {
                this.email = email.trim()
            }
        }

    override suspend fun verifyEmailOtp(email: String, code: String): Result<Unit> =
        runCatchingCancellable {
            client.auth.awaitInitialization()
            client.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = email.trim(),
                token = code.trim(),
            )
            syncAuthStateFromCurrentSession()
            checkNotNull(currentAuthUser()) { "sign_in_failed" }
        }

    override suspend fun signInWithGoogle(): Result<Unit> =
        runCatchingCancellable {
            client.auth.signInWith(Google, redirectUrl = AuthRedirectUrls.REDIRECT)
        }

    override suspend fun signInWithApple(): Result<Unit> =
        runCatchingCancellable {
            client.auth.signInWith(Apple, redirectUrl = AuthRedirectUrls.REDIRECT)
        }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    override suspend fun exportPersonalData(): Result<String> = runCatchingCancellable {
        client.auth.awaitInitialization()
        client.postgrest.rpc("export_my_personal_data").data
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatchingCancellable {
        client.auth.awaitInitialization()
        client.functions.invoke("delete-account")
        client.auth.signOut()
    }

    private suspend fun processOAuthRedirect(url: String) {
        if (!AuthRedirectEvents.isAuthRedirect(url)) return

        if (currentAuthUser() == null) {
            _authState.value = AuthState.Loading
        }
        client.auth.awaitInitialization()
        try { handleAuthDeeplink(client, url) } catch (e: CancellationException) { throw e } catch (_: Exception) { }
        syncAuthStateFromCurrentSession()
    }

    private suspend fun syncAuthStateFromCurrentSession() {
        val user = currentAuthUser() ?: return
        bootstrapCurrentProfile()
        _authState.value = AuthState.Authenticated(user)
    }

    private suspend fun bootstrapCurrentProfile() {
        val userId = client.auth.currentUserOrNull()?.id
            ?: client.auth.currentSessionOrNull()?.user?.id
            ?: return
        try { client.ensureCurrentProfile(userId) } catch (e: CancellationException) { throw e } catch (_: Exception) { }
    }

    private fun mapSessionStatus(status: SessionStatus): AuthState =
        mapSupabaseSessionToAuthState(status, currentAuthUser())

    private fun currentAuthUser(): AuthUser? =
        client.auth.currentSessionOrNull()?.user?.toAuthUser()
}
