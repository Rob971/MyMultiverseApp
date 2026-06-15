package app.mymultiverse.kmp.presentation.screens.auth

import app.mymultiverse.kmp.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LoginError {
    data object Generic : LoginError

    data object ConfigMissing : LoginError

    data object ProviderComingSoon : LoginError
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: LoginError? = null,
)

class LoginScreenModel(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun toggleSignUpMode() {
        _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
    }

    fun submitEmailAuth() {
        val snapshot = _uiState.value
        if (snapshot.isLoading) return

        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (snapshot.isSignUpMode) {
                authRepository.signUpWithEmail(snapshot.email, snapshot.password)
            } else {
                authRepository.signInWithEmail(snapshot.email, snapshot.password)
            }
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.let { mapAuthFailure(it) },
                )
            }
        }
    }

    fun signInWithGoogle() {
        signInWithProvider { authRepository.signInWithGoogle() }
    }

    fun signInWithApple() {
        signInWithProvider { authRepository.signInWithApple() }
    }

    private fun signInWithProvider(action: suspend () -> Result<Unit>) {
        if (_uiState.value.isLoading) return

        scope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = action()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.let { mapAuthFailure(it) },
                )
            }
        }
    }

    private fun mapAuthFailure(throwable: Throwable): LoginError =
        when (throwable.message) {
            "supabase_not_configured" -> LoginError.ConfigMissing
            "google_oauth_not_configured", "apple_oauth_not_configured" -> LoginError.ProviderComingSoon
            else -> LoginError.Generic
        }
}
