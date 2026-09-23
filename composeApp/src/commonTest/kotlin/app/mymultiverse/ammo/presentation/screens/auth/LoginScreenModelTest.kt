package app.mymultiverse.ammo.presentation.screens.auth

import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.auth.AuthFailureCodes
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeModel(
        authRepository: AuthRepository = RecordingAuthRepository(),
    ) = LoginScreenModel(
        authRepository = authRepository,
        logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "test")),
        scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
    )

    // ---- sign-in mode ----

    @Test
    fun submitEmailAuth_rejectsBlankCredentialsWithoutCallingRepository() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository)

        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(0, authRepository.signInCalls)
        assertEquals(
            LoginMessage.Error(LoginError.InvalidCredentials),
            screenModel.uiState.value.message,
        )
    }

    @Test
    fun submitEmailAuth_signInSuccess_clearsMessageAndUpdatesAuthState() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository)

        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(1, authRepository.signInCalls)
        assertFalse(screenModel.uiState.value.isLoading)
        assertNull(screenModel.uiState.value.message)
        assertIs<AuthState.Authenticated>(authRepository.authState.value)
    }

    // ---- sign-up step 1 ----

    @Test
    fun submitEmailAuth_signUpWithBlankDisplayName_showsBlankNameError() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository)

        screenModel.toggleSignUpMode()
        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(
            LoginMessage.Error(LoginError.BlankDisplayName),
            screenModel.uiState.value.message,
        )
        assertEquals(0, authRepository.signUpCalls)
    }

    @Test
    fun submitEmailAuth_signUpWithWeakPassword_showsWeakPasswordError() = runTest(testDispatcher) {
        val screenModel = makeModel()

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Maria")
        screenModel.onEmailChange("maria@example.com")
        screenModel.onPasswordChange("123")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(
            LoginMessage.Error(LoginError.WeakPassword),
            screenModel.uiState.value.message,
        )
    }

    @Test
    fun submitEmailAuth_signUpCompletesRegistrationWithDisplayName() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository)

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Lucia")
        screenModel.onEmailChange("lucia@example.com")
        screenModel.onPasswordChange("secure99")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(1, authRepository.signUpCalls)
        assertEquals("Lucia", authRepository.lastDisplayName)
        assertNull(screenModel.uiState.value.message)
    }

    @Test
    fun submitEmailAuth_signUpConfirmation_transitionsToSignInMode() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository(
            signUpResult = Result.failure(IllegalStateException(AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED)),
        )
        val screenModel = makeModel(authRepository)

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Rosa")
        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertFalse(screenModel.uiState.value.isSignUpMode)
        assertTrue(screenModel.uiState.value.awaitingEmailConfirmation)
        assertNull(screenModel.uiState.value.message)
        assertEquals("user@example.com", screenModel.uiState.value.email)
        assertEquals("", screenModel.uiState.value.password)
    }

    @Test
    fun awaitingEmailConfirmation_clearsWhenUserEditsEmail() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository(
            signUpResult = Result.failure(IllegalStateException(AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED)),
        )
        val screenModel = makeModel(authRepository)

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Rosa")
        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertTrue(screenModel.uiState.value.awaitingEmailConfirmation)

        screenModel.onEmailChange("other@example.com")
        assertFalse(screenModel.uiState.value.awaitingEmailConfirmation)
    }

    @Test
    fun canSubmitEmailAuth_requiresNonBlankFields() = runTest(testDispatcher) {
        val screenModel = makeModel()

        assertFalse(screenModel.uiState.value.canSubmitEmailAuth)

        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        assertTrue(screenModel.uiState.value.canSubmitEmailAuth)
    }

    @Test
    fun togglePasswordVisibility_togglesState() = runTest(testDispatcher) {
        val screenModel = makeModel()

        assertFalse(screenModel.uiState.value.isPasswordVisible)
        screenModel.togglePasswordVisibility()
        assertTrue(screenModel.uiState.value.isPasswordVisible)
        screenModel.togglePasswordVisibility()
        assertFalse(screenModel.uiState.value.isPasswordVisible)
    }
}

private class RecordingAuthRepository(
    private val signUpResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var signInCalls = 0
    var signUpCalls = 0
    var lastDisplayName: String? = null

    override suspend fun restoreSession() = Unit

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        signInCalls++
        _authState.value = AuthState.Authenticated(
            AuthUser(id = "user-1", email = email.trim(), displayName = null),
        )
        return Result.success(Unit)
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): Result<Unit> {
        signUpCalls++
        lastDisplayName = displayName
        return signUpResult
    }

    override suspend fun sendEmailOtp(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun verifyEmailOtp(email: String, code: String): Result<Unit> =
        signInWithEmail(email, code)

    override suspend fun signInWithGoogle(): Result<Unit> =
        Result.failure(UnsupportedOperationException("google_oauth_not_configured"))

    override suspend fun signInWithApple(): Result<Unit> =
        Result.failure(UnsupportedOperationException("apple_oauth_not_configured"))

    override suspend fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun exportPersonalData(): Result<String> =
        Result.success("""{"exported_at":"test"}""")

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}
