package app.mymultiverse.kmp.presentation.screens.auth

import app.mymultiverse.kmp.domain.auth.AuthFailureCodes
import app.mymultiverse.kmp.domain.auth.EmailAuthCredentials
import app.mymultiverse.kmp.domain.auth.EmailAuthValidationError
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

    data object InvalidCredentials : LoginError

    data object InvalidEmail : LoginError

    data object WeakPassword : LoginError

    data object UserAlreadyExists : LoginError

    data object EmailNotConfirmed : LoginError

    data object SignUpDisabled : LoginError
}

sealed interface LoginMessage {
    data class Error(val type: LoginError) : LoginMessage

    data object EmailConfirmationSent : LoginMessage
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val message: LoginMessage? = null,
) {
    val canSubmitEmailAuth: Boolean =
        email.isNotBlank() && password.isNotBlank()
}

class LoginScreenModel(
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, message = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, message = null) }
    }

    fun toggleSignUpMode() {
        _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, message = null) }
    }

    fun submitEmailAuth() {
        val snapshot = _uiState.value
        if (snapshot.isLoading) return

        EmailAuthCredentials.validationError(
            email = snapshot.email,
            password = snapshot.password,
            isSignUp = snapshot.isSignUpMode,
        )?.let { validationError ->
            _uiState.update {
                it.copy(message = LoginMessage.Error(validationError.toLoginError()))
            }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = if (snapshot.isSignUpMode) {
                authRepository.signUpWithEmail(snapshot.email, snapshot.password)
            } else {
                authRepository.signInWithEmail(snapshot.email, snapshot.password)
            }
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    message = result.fold(
                        onSuccess = { null },
                        onFailure = { throwable -> mapAuthFailure(throwable) },
                    ),
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
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = action()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    message = result.exceptionOrNull()?.let { mapAuthFailure(it) },
                )
            }
        }
    }

    private fun mapAuthFailure(throwable: Throwable): LoginMessage {
        if (throwable.message == "supabase_not_configured") {
            return LoginMessage.Error(LoginError.ConfigMissing)
        }
        if (throwable.message == "google_oauth_not_configured" ||
            throwable.message == "apple_oauth_not_configured"
        ) {
            return LoginMessage.Error(LoginError.ProviderComingSoon)
        }

        return when (AuthFailureCodes.fromThrowable(throwable)) {
            AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED -> LoginMessage.EmailConfirmationSent
            AuthFailureCodes.INVALID_CREDENTIALS -> LoginMessage.Error(LoginError.InvalidCredentials)
            AuthFailureCodes.INVALID_EMAIL -> LoginMessage.Error(LoginError.InvalidEmail)
            AuthFailureCodes.WEAK_PASSWORD -> LoginMessage.Error(LoginError.WeakPassword)
            AuthFailureCodes.USER_ALREADY_EXISTS -> LoginMessage.Error(LoginError.UserAlreadyExists)
            AuthFailureCodes.EMAIL_NOT_CONFIRMED -> LoginMessage.Error(LoginError.EmailNotConfirmed)
            AuthFailureCodes.SIGN_UP_DISABLED -> LoginMessage.Error(LoginError.SignUpDisabled)
            else -> LoginMessage.Error(LoginError.Generic)
        }
    }

    private fun EmailAuthValidationError.toLoginError(): LoginError =
        when (this) {
            EmailAuthValidationError.MissingFields -> LoginError.InvalidCredentials
            EmailAuthValidationError.InvalidEmail -> LoginError.InvalidEmail
            EmailAuthValidationError.WeakPassword -> LoginError.WeakPassword
        }
}
