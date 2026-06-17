package app.mymultiverse.kmp.presentation.screens.auth

import app.mymultiverse.kmp.domain.auth.AuthFailureCodes
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.repository.AuthRepository
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

    @Test
    fun submitEmailAuth_rejectsBlankCredentialsWithoutCallingRepository() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = LoginScreenModel(
            authRepository = authRepository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

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
        val screenModel = LoginScreenModel(
            authRepository = authRepository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(1, authRepository.signInCalls)
        assertFalse(screenModel.uiState.value.isLoading)
        assertNull(screenModel.uiState.value.message)
        assertIs<AuthState.Authenticated>(authRepository.authState.value)
    }

    @Test
    fun submitEmailAuth_signUpShowsConfirmationMessageWhenNoSession() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository(
            signUpResult = Result.failure(IllegalStateException(AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED)),
        )
        val screenModel = LoginScreenModel(
            authRepository = authRepository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        screenModel.toggleSignUpMode()
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(1, authRepository.signUpCalls)
        assertEquals(LoginMessage.EmailConfirmationSent, screenModel.uiState.value.message)
    }

    @Test
    fun submitEmailAuth_signUpRejectsWeakPasswordBeforeRepositoryCall() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = LoginScreenModel(
            authRepository = authRepository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("123")
        screenModel.toggleSignUpMode()
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(0, authRepository.signUpCalls)
        assertEquals(
            LoginMessage.Error(LoginError.WeakPassword),
            screenModel.uiState.value.message,
        )
    }

    @Test
    fun canSubmitEmailAuth_requiresNonBlankFields() = runTest(testDispatcher) {
        val screenModel = LoginScreenModel(
            authRepository = RecordingAuthRepository(),
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        assertFalse(screenModel.uiState.value.canSubmitEmailAuth)

        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        assertTrue(screenModel.uiState.value.canSubmitEmailAuth)
    }
}

private class RecordingAuthRepository(
    private val signUpResult: Result<Unit> = Result.success(Unit),
) : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var signInCalls = 0
    var signUpCalls = 0

    override suspend fun restoreSession() = Unit

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        signInCalls++
        _authState.value = AuthState.Authenticated(
            AuthUser(id = "user-1", email = email.trim(), displayName = null),
        )
        return Result.success(Unit)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<Unit> {
        signUpCalls++
        return signUpResult
    }

    override suspend fun signInWithGoogle(): Result<Unit> =
        Result.failure(UnsupportedOperationException("google_oauth_not_configured"))

    override suspend fun signInWithApple(): Result<Unit> =
        Result.failure(UnsupportedOperationException("apple_oauth_not_configured"))

    override suspend fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun exportPersonalData(): Result<String> =
        Result.success("""{"exported_at":"test"}""")
}
