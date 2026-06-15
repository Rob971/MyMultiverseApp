package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class SupabaseAuthRepository(
    private val client: SupabaseClient,
    private val scope: CoroutineScope,
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        client.auth.sessionStatus
            .onEach { status -> _authState.update { mapSessionStatus(status) } }
            .launchIn(scope)

        AuthRedirectEvents.urls
            .onEach { url -> handleAuthDeeplink(client, url) }
            .launchIn(scope)
    }

    override suspend fun restoreSession() {
        client.auth.awaitInitialization()
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        runCatching {
            client.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = password
            }
        }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> =
        runCatching {
            client.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = password
            }
        }

    override suspend fun signInWithGoogle(): Result<Unit> =
        runCatching {
            client.auth.signInWith(Google)
        }

    override suspend fun signInWithApple(): Result<Unit> =
        runCatching {
            client.auth.signInWith(Apple)
        }

    override suspend fun signOut() {
        client.auth.signOut()
    }

    private fun mapSessionStatus(status: SessionStatus): AuthState =
        when (status) {
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.Authenticated -> {
                val user = status.session.user
                if (user != null) {
                    AuthState.Authenticated(user.toAuthUser())
                } else {
                    AuthState.Unauthenticated
                }
            }
            is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
            is SessionStatus.RefreshFailure -> AuthState.Unauthenticated
        }

    private fun UserInfo.toAuthUser(): AuthUser =
        AuthUser(
            id = id,
            email = email,
            displayName = userMetadata?.get("full_name")?.toString()
                ?: userMetadata?.get("name")?.toString(),
        )
}
