package app.mymultiverse.ammo.presentation.screens.auth

import app.mymultiverse.ammo.domain.auth.AuthFailureCodes
import app.mymultiverse.ammo.domain.auth.EmailAuthCredentials
import app.mymultiverse.ammo.domain.auth.EmailAuthValidationError
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.presentation.registration.RegistrationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginRegistrationStep { Credentials, HouseholdSetup }

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
    data object BlankDisplayName : LoginError
}

sealed interface LoginMessage {
    data class Error(val type: LoginError) : LoginMessage
    data object EmailConfirmationSent : LoginMessage
}

data class LoginUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val householdName: String = "",
    val isSignUpMode: Boolean = false,
    val registrationStep: LoginRegistrationStep = LoginRegistrationStep.Credentials,
    val isLoading: Boolean = false,
    val message: LoginMessage? = null,
) {
    val canSubmitEmailAuth: Boolean =
        email.isNotBlank() && password.isNotBlank()

    val canAdvanceToHouseholdStep: Boolean =
        isSignUpMode && displayName.isNotBlank() && email.isNotBlank() && password.isNotBlank()

    val isOnStep2: Boolean =
        isSignUpMode && registrationStep == LoginRegistrationStep.HouseholdSetup
}

class LoginScreenModel(
    private val authRepository: AuthRepository,
    private val registrationData: RegistrationData = RegistrationData(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(value: String) {
        _uiState.update { it.copy(displayName = value, message = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, message = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, message = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onHouseholdNameChange(value: String) {
        _uiState.update { it.copy(householdName = value, message = null) }
    }

    fun toggleSignUpMode() {
        _uiState.update {
            it.copy(
                isSignUpMode = !it.isSignUpMode,
                registrationStep = LoginRegistrationStep.Credentials,
                message = null,
            )
        }
    }

    /**
     * In sign-up mode step 1: validates name/email/password, then advances to step 2.
     * In sign-in mode: performs sign-in directly.
     * In sign-up mode step 2: creates the account.
     */
    fun submitEmailAuth() {
        val snapshot = _uiState.value
        if (snapshot.isLoading) return

        when {
            snapshot.isSignUpMode && snapshot.registrationStep == LoginRegistrationStep.Credentials -> {
                advanceToHouseholdStep()
            }
            snapshot.isSignUpMode && snapshot.registrationStep == LoginRegistrationStep.HouseholdSetup -> {
                performSignUp(snapshot)
            }
            else -> {
                performSignIn(snapshot)
            }
        }
    }

    /**
     * Validates step 1 fields and advances to the household setup step.
     * Called explicitly from the "Continue" button on step 1.
     */
    fun advanceToHouseholdStep() {
        val snapshot = _uiState.value
        if (snapshot.isLoading) return

        if (snapshot.displayName.isBlank()) {
            _uiState.update {
                it.copy(message = LoginMessage.Error(LoginError.BlankDisplayName))
            }
            return
        }

        EmailAuthCredentials.validationError(
            email = snapshot.email,
            password = snapshot.password,
            isSignUp = true,
        )?.let { validationError ->
            _uiState.update {
                it.copy(message = LoginMessage.Error(validationError.toLoginError()))
            }
            return
        }

        _uiState.update {
            it.copy(
                registrationStep = LoginRegistrationStep.HouseholdSetup,
                message = null,
            )
        }
    }

    /** Returns the user to step 1 of registration. */
    fun goBackToCredentials() {
        _uiState.update {
            it.copy(
                registrationStep = LoginRegistrationStep.Credentials,
                message = null,
            )
        }
    }

    /**
     * Skips the household name on step 2 and completes registration with no
     * household pre-fill. The HouseholdCreationScreen will use the default name.
     */
    fun skipHouseholdSetup() {
        val snapshot = _uiState.value
        if (snapshot.isLoading) return
        performSignUp(snapshot.copy(householdName = ""))
    }

    fun signInWithGoogle() {
        signInWithProvider { authRepository.signInWithGoogle() }
    }

    fun signInWithApple() {
        signInWithProvider { authRepository.signInWithApple() }
    }

    private fun performSignIn(snapshot: LoginUiState) {
        EmailAuthCredentials.validationError(
            email = snapshot.email,
            password = snapshot.password,
            isSignUp = false,
        )?.let { validationError ->
            _uiState.update {
                it.copy(message = LoginMessage.Error(validationError.toLoginError()))
            }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = authRepository.signInWithEmail(snapshot.email, snapshot.password)
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

    private fun performSignUp(snapshot: LoginUiState) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val householdName = snapshot.householdName.trim()
            val displayName = snapshot.displayName.trim().takeIf { it.isNotBlank() }
            val result = authRepository.signUpWithEmail(
                email = snapshot.email,
                password = snapshot.password,
                displayName = displayName,
            )
            if (result.isSuccess && householdName.isNotBlank()) {
                registrationData.pendingHouseholdName = householdName
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
