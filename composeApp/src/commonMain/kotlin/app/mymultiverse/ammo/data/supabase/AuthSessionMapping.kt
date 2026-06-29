package app.mymultiverse.ammo.data.supabase

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import io.github.jan.supabase.auth.status.SessionStatus

/**
 * Maps Supabase [SessionStatus] to app [AuthState].
 *
 * When the SDK still holds a session (including during token refresh), the user stays
 * [AuthState.Authenticated] so the app remains usable offline or on flaky networks.
 */
internal fun mapSupabaseSessionToAuthState(
    status: SessionStatus,
    currentUser: AuthUser?,
): AuthState =
    when (status) {
        is SessionStatus.Initializing -> currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Loading
        is SessionStatus.Authenticated -> {
            val user = status.session.user
            when {
                user != null -> AuthState.Authenticated(user.toAuthUser())
                currentUser != null -> AuthState.Authenticated(currentUser)
                else -> AuthState.Unauthenticated
            }
        }
        is SessionStatus.RefreshFailure -> {
            // RefreshFailure means a retry is in progress — not a sign-out.
            currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Loading
        }
        is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
    }

internal fun io.github.jan.supabase.auth.user.UserInfo.toAuthUser(): AuthUser =
    AuthUser(
        id = id,
        email = email,
        displayName = userMetadata?.get("full_name")?.toString()
            ?: userMetadata?.get("name")?.toString(),
    )
