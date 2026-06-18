package app.mymultiverse.kmp.presentation.screens.invite

import app.mymultiverse.kmp.domain.auth.AuthFailureCodes
import app.mymultiverse.kmp.domain.auth.EmailOtpCredentials
import app.mymultiverse.kmp.domain.auth.EmailOtpValidationError
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.kmp.presentation.platform.isAppleSignInAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface JoinPreviewState {
    data object Loading : JoinPreviewState

    data class Ready(
        val preview: HouseholdInvitePreview,
    ) : JoinPreviewState

    data class Error(val type: JoinPreviewError) : JoinPreviewState
}

enum class JoinPreviewError {
    NotFound,
    Expired,
    Declined,
    AlreadyAccepted,
    Generic,
}

enum class JoinOtpStep {
    Email,
    Code,
}

sealed interface JoinHouseholdError {
    data object ConfigMissing : JoinHouseholdError

    data object ProviderComingSoon : JoinHouseholdError

    data object InvalidEmail : JoinHouseholdError

    data object OtpInvalid : JoinHouseholdError

    data object OtpExpired : JoinHouseholdError

    data object OtpRateLimited : JoinHouseholdError

    data object Generic : JoinHouseholdError
}

sealed interface JoinHouseholdMessage {
    data object OtpSent : JoinHouseholdMessage

    data class Error(val type: JoinHouseholdError) : JoinHouseholdMessage

    data class Authenticated(val preview: HouseholdInvitePreview) : JoinHouseholdMessage
}

data class JoinHouseholdUiState(
    val previewState: JoinPreviewState = JoinPreviewState.Loading,
    val step: JoinOtpStep = JoinOtpStep.Email,
    val email: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val message: JoinHouseholdMessage? = null,
    val resendCooldownSeconds: Int = 0,
    val showAppleSignIn: Boolean = false,
) {
    val showEmailWarning: Boolean
        get() {
            val preview = (previewState as? JoinPreviewState.Ready)?.preview ?: return false
            return email.trim().lowercase() != preview.inviteeEmail.trim().lowercase()
        }

    val canContinueEmail: Boolean = email.isNotBlank() && !isLoading

    val canVerifyOtp: Boolean =
        otpCode.isNotBlank() && !isLoading && (previewState is JoinPreviewState.Ready)

    val canResendOtp: Boolean = resendCooldownSeconds == 0 && !isLoading && step == JoinOtpStep.Code
}

class JoinHouseholdScreenModel(
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(
        JoinHouseholdUiState(showAppleSignIn = isAppleSignInAvailable()),
    )
    val uiState: StateFlow<JoinHouseholdUiState> = _uiState.asStateFlow()

    private var resendCooldownJob: Job? = null

    fun loadPreview(inviteToken: String) {
        if (_uiState.value.isLoading && _uiState.value.previewState is JoinPreviewState.Loading) return

        scope.launch {
            _uiState.update {
                it.copy(
                    previewState = JoinPreviewState.Loading,
                    message = null,
                    step = JoinOtpStep.Email,
                    otpCode = "",
                )
            }

            collaborationRepository.previewInvite(inviteToken)
                .onSuccess { preview ->
                    _uiState.update {
                        it.copy(
                            previewState = JoinPreviewState.Ready(preview),
                            email = preview.inviteeEmail,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(previewState = JoinPreviewState.Error(mapPreviewError(throwable)))
                    }
                }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, message = null) }
    }

    fun onOtpCodeChange(value: String) {
        val digitsOnly = value.filter(Char::isDigit).take(OTP_CODE_LENGTH)
        _uiState.update { it.copy(otpCode = digitsOnly, message = null) }
    }

    fun continueWithEmail() {
        val snapshot = _uiState.value
        if (snapshot.isLoading || snapshot.previewState !is JoinPreviewState.Ready) return

        val validationError = EmailOtpCredentials.emailValidationError(snapshot.email)
        if (validationError != null) {
            _uiState.update {
                it.copy(message = JoinHouseholdMessage.Error(validationError.toJoinError()))
            }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            authRepository.sendEmailOtp(snapshot.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = JoinOtpStep.Code,
                            message = JoinHouseholdMessage.OtpSent,
                        )
                    }
                    startResendCooldown()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = JoinHouseholdMessage.Error(mapAuthFailure(throwable)),
                        )
                    }
                }
        }
    }

    fun verifyOtp() {
        val snapshot = _uiState.value
        if (snapshot.isLoading || snapshot.previewState !is JoinPreviewState.Ready) return

        EmailOtpCredentials.validationError(snapshot.email, snapshot.otpCode)?.let { validationError ->
            _uiState.update {
                it.copy(message = JoinHouseholdMessage.Error(validationError.toJoinError()))
            }
            return
        }

        val preview = snapshot.previewState.preview
        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            authRepository.verifyEmailOtp(snapshot.email, snapshot.otpCode)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = JoinHouseholdMessage.Authenticated(preview),
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = JoinHouseholdMessage.Error(mapAuthFailure(throwable)),
                        )
                    }
                }
        }
    }

    fun resendOtp() {
        if (!_uiState.value.canResendOtp) return
        continueWithEmail()
    }

    fun backToEmailStep() {
        resendCooldownJob?.cancel()
        _uiState.update {
            it.copy(
                step = JoinOtpStep.Email,
                otpCode = "",
                message = null,
                resendCooldownSeconds = 0,
            )
        }
    }

    fun signInWithGoogle() {
        signInWithProvider { authRepository.signInWithGoogle() }
    }

    fun signInWithApple() {
        if (!isAppleSignInAvailable()) return
        signInWithProvider { authRepository.signInWithApple() }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun signInWithProvider(action: suspend () -> Result<Unit>) {
        val preview = (_uiState.value.previewState as? JoinPreviewState.Ready)?.preview ?: return
        if (_uiState.value.isLoading) return

        scope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            action()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = JoinHouseholdMessage.Authenticated(preview),
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = JoinHouseholdMessage.Error(mapAuthFailure(throwable)),
                        )
                    }
                }
        }
    }

    private fun startResendCooldown() {
        resendCooldownJob?.cancel()
        resendCooldownJob = scope.launch {
            var remaining = RESEND_COOLDOWN_SECONDS
            while (isActive && remaining > 0) {
                _uiState.update { it.copy(resendCooldownSeconds = remaining) }
                delay(1_000)
                remaining -= 1
            }
            _uiState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }

    private fun mapPreviewError(throwable: Throwable): JoinPreviewError =
        when {
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_NOT_FOUND, throwable.message) ->
                JoinPreviewError.NotFound
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_EXPIRED, throwable.message) ->
                JoinPreviewError.Expired
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_DECLINED, throwable.message) ->
                JoinPreviewError.Declined
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.INVITE_ALREADY_ACCEPTED,
                throwable.message,
            ) -> JoinPreviewError.AlreadyAccepted
            else -> JoinPreviewError.Generic
        }

    private fun mapAuthFailure(throwable: Throwable): JoinHouseholdError {
        if (throwable.message == "supabase_not_configured") {
            return JoinHouseholdError.ConfigMissing
        }
        if (throwable.message == "google_oauth_not_configured" ||
            throwable.message == "apple_oauth_not_configured"
        ) {
            return JoinHouseholdError.ProviderComingSoon
        }

        return when (AuthFailureCodes.fromThrowable(throwable)) {
            AuthFailureCodes.INVALID_EMAIL -> JoinHouseholdError.InvalidEmail
            AuthFailureCodes.OTP_INVALID -> JoinHouseholdError.OtpInvalid
            AuthFailureCodes.OTP_EXPIRED -> JoinHouseholdError.OtpExpired
            AuthFailureCodes.OTP_RATE_LIMITED -> JoinHouseholdError.OtpRateLimited
            else -> JoinHouseholdError.Generic
        }
    }

    private fun EmailOtpValidationError.toJoinError(): JoinHouseholdError =
        when (this) {
            EmailOtpValidationError.MissingFields,
            EmailOtpValidationError.InvalidEmail,
            -> JoinHouseholdError.InvalidEmail
            EmailOtpValidationError.InvalidCode -> JoinHouseholdError.OtpInvalid
        }

    private companion object {
        const val OTP_CODE_LENGTH = 6
        const val RESEND_COOLDOWN_SECONDS = 60
    }
}
