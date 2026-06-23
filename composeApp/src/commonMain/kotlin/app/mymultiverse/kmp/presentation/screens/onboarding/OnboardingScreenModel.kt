package app.mymultiverse.kmp.presentation.screens.onboarding

import app.mymultiverse.kmp.domain.auth.AuthFailureCodes
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.kmp.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.kmp.presentation.platform.isAppleSignInAvailable
import app.mymultiverse.kmp.presentation.screens.auth.LoginError
import app.mymultiverse.kmp.presentation.screens.auth.LoginMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface InvitePreviewState {
    data object Idle : InvitePreviewState

    data object Loading : InvitePreviewState

    data class Ready(val preview: HouseholdInvitePreview) : InvitePreviewState

    data object Unavailable : InvitePreviewState
}

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val message: LoginMessage? = null,
    val invitePreviewState: InvitePreviewState = InvitePreviewState.Idle,
    val showAppleSignIn: Boolean = false,
) {
    val inviteHouseholdName: String?
        get() = (invitePreviewState as? InvitePreviewState.Ready)?.preview?.householdName
}

class OnboardingScreenModel(
    private val authRepository: AuthRepository,
    private val collaborationRepository: HouseholdCollaborationRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _uiState = MutableStateFlow(
        OnboardingUiState(showAppleSignIn = isAppleSignInAvailable()),
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun loadInvitePreview(inviteToken: String?) {
        val token = inviteToken?.takeIf { it.isNotBlank() } ?: run {
            _uiState.update { it.copy(invitePreviewState = InvitePreviewState.Idle) }
            return
        }

        scope.launch {
            _uiState.update { it.copy(invitePreviewState = InvitePreviewState.Loading) }
            collaborationRepository.previewInvite(token)
                .onSuccess { preview ->
                    _uiState.update {
                        it.copy(invitePreviewState = InvitePreviewState.Ready(preview))
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            invitePreviewState = if (isRecoverablePreviewError(throwable)) {
                                InvitePreviewState.Unavailable
                            } else {
                                InvitePreviewState.Unavailable
                            },
                        )
                    }
                }
        }
    }

    fun signInWithGoogle() {
        signInWithProvider { authRepository.signInWithGoogle() }
    }

    fun signInWithApple() {
        if (!isAppleSignInAvailable()) return
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
            AuthFailureCodes.INVALID_CREDENTIALS -> LoginMessage.Error(LoginError.InvalidCredentials)
            AuthFailureCodes.INVALID_EMAIL -> LoginMessage.Error(LoginError.InvalidEmail)
            AuthFailureCodes.WEAK_PASSWORD -> LoginMessage.Error(LoginError.WeakPassword)
            AuthFailureCodes.USER_ALREADY_EXISTS -> LoginMessage.Error(LoginError.UserAlreadyExists)
            AuthFailureCodes.EMAIL_NOT_CONFIRMED -> LoginMessage.Error(LoginError.EmailNotConfirmed)
            AuthFailureCodes.SIGN_UP_DISABLED -> LoginMessage.Error(LoginError.SignUpDisabled)
            else -> LoginMessage.Error(LoginError.Generic)
        }
    }

    private fun isRecoverablePreviewError(throwable: Throwable): Boolean =
        CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_NOT_FOUND, throwable.message) ||
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_EXPIRED, throwable.message) ||
            CollaborationErrorCodes.messageContains(CollaborationErrorCodes.INVITE_DECLINED, throwable.message) ||
            CollaborationErrorCodes.messageContains(
                CollaborationErrorCodes.INVITE_ALREADY_ACCEPTED,
                throwable.message,
            )
}
