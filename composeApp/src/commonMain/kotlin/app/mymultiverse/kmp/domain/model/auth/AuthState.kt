package app.mymultiverse.kmp.domain.model.auth

sealed interface AuthState {
    data object Loading : AuthState

    data object Unauthenticated : AuthState

    data class Authenticated(
        val user: AuthUser,
    ) : AuthState

    data object ConfigurationMissing : AuthState
}
