package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthSessionMappingTest {

    private val storedUser = AuthUser(
        id = "user-1",
        email = "user@example.com",
        displayName = "Test User",
    )

    @Test
    fun refreshFailure_keepsAuthenticatedWhenSessionStillPresent() {
        val state = mapSupabaseSessionToAuthState(
            status = SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(RuntimeException("offline"))),
            currentUser = storedUser,
        )
        assertEquals(AuthState.Authenticated(storedUser), state)
    }

    @Test
    fun refreshFailure_withoutSession_showsLoadingWhileRetrying() {
        val state = mapSupabaseSessionToAuthState(
            status = SessionStatus.RefreshFailure(RefreshFailureCause.NetworkError(RuntimeException("offline"))),
            currentUser = null,
        )
        assertEquals(AuthState.Loading, state)
    }

    @Test
    fun notAuthenticated_mapsToLogin() {
        val state = mapSupabaseSessionToAuthState(
            status = SessionStatus.NotAuthenticated(isSignOut = true),
            currentUser = null,
        )
        assertEquals(AuthState.Unauthenticated, state)
    }

    @Test
    fun initializing_withStoredSession_keepsUserInApp() {
        val state = mapSupabaseSessionToAuthState(
            status = SessionStatus.Initializing,
            currentUser = storedUser,
        )
        assertEquals(AuthState.Authenticated(storedUser), state)
    }
}
