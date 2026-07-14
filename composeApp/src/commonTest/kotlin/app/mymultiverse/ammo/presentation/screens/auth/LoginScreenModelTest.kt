package app.mymultiverse.ammo.presentation.screens.auth

import app.mymultiverse.ammo.domain.auth.AuthFailureCodes
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.presentation.registration.RegistrationData
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
        registrationData: RegistrationData = RegistrationData(),
    ) = LoginScreenModel(
        authRepository = authRepository,
        registrationData = registrationData,
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
    fun advanceToHouseholdStep_withBlankDisplayName_showsBlankNameError() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository)

        screenModel.toggleSignUpMode()
        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        // displayName left blank
        screenModel.advanceToHouseholdStep()
        advanceUntilIdle()

        assertEquals(
            LoginMessage.Error(LoginError.BlankDisplayName),
            screenModel.uiState.value.message,
        )
        assertEquals(LoginRegistrationStep.Credentials, screenModel.uiState.value.registrationStep)
        assertEquals(0, authRepository.signUpCalls)
    }

    @Test
    fun advanceToHouseholdStep_withValidStep1Fields_advancesToHouseholdSetupStep() = runTest(testDispatcher) {
        val screenModel = makeModel()

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Maria")
        screenModel.onEmailChange("maria@example.com")
        screenModel.onPasswordChange("secure99")
        screenModel.advanceToHouseholdStep()
        advanceUntilIdle()

        assertEquals(LoginRegistrationStep.HouseholdSetup, screenModel.uiState.value.registrationStep)
        assertNull(screenModel.uiState.value.message)
    }

    @Test
    fun advanceToHouseholdStep_withWeakPassword_showsWeakPasswordError() = runTest(testDispatcher) {
        val screenModel = makeModel()

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Maria")
        screenModel.onEmailChange("maria@example.com")
        screenModel.onPasswordChange("123")
        screenModel.advanceToHouseholdStep()
        advanceUntilIdle()

        assertEquals(
            LoginMessage.Error(LoginError.WeakPassword),
            screenModel.uiState.value.message,
        )
        assertEquals(LoginRegistrationStep.Credentials, screenModel.uiState.value.registrationStep)
    }

    // ---- sign-up step 2 ----

    @Test
    fun submitEmailAuth_signUpStep2_completesRegistrationWithDisplayName() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository)

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Lucia")
        screenModel.onEmailChange("lucia@example.com")
        screenModel.onPasswordChange("secure99")
        screenModel.advanceToHouseholdStep()
        screenModel.onHouseholdNameChange("Famiglia Rossi")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(1, authRepository.signUpCalls)
        assertEquals("Lucia", authRepository.lastDisplayName)
        assertNull(screenModel.uiState.value.message)
    }

    @Test
    fun submitEmailAuth_signUpStep2_storesPendingHouseholdNameOnSuccess() = runTest(testDispatcher) {
        val registrationData = RegistrationData()
        val screenModel = makeModel(registrationData = registrationData)

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Lucia")
        screenModel.onEmailChange("lucia@example.com")
        screenModel.onPasswordChange("secure99")
        screenModel.advanceToHouseholdStep()
        screenModel.onHouseholdNameChange("Famiglia Rossi")
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals("Famiglia Rossi", registrationData.pendingHouseholdName)
    }

    @Test
    fun skipHouseholdSetup_completesRegistrationWithoutStoringHouseholdName() = runTest(testDispatcher) {
        val registrationData = RegistrationData()
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository, registrationData)

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Paolo")
        screenModel.onEmailChange("paolo@example.com")
        screenModel.onPasswordChange("secure99")
        screenModel.advanceToHouseholdStep()
        screenModel.skipHouseholdSetup()
        advanceUntilIdle()

        assertEquals(1, authRepository.signUpCalls)
        assertNull(registrationData.pendingHouseholdName)
    }

    @Test
    fun goBackToCredentials_returnsToStep1() = runTest(testDispatcher) {
        val screenModel = makeModel()

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Maria")
        screenModel.onEmailChange("maria@example.com")
        screenModel.onPasswordChange("secure99")
        screenModel.advanceToHouseholdStep()
        advanceUntilIdle()
        assertEquals(LoginRegistrationStep.HouseholdSetup, screenModel.uiState.value.registrationStep)

        screenModel.goBackToCredentials()
        assertEquals(LoginRegistrationStep.Credentials, screenModel.uiState.value.registrationStep)
    }

    @Test
    fun submitEmailAuth_signUpShowsConfirmationMessageWhenNoSession() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository(
            signUpResult = Result.failure(IllegalStateException(AuthFailureCodes.EMAIL_CONFIRMATION_REQUIRED)),
        )
        val screenModel = makeModel(authRepository)

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Rosa")
        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("secret123")
        screenModel.advanceToHouseholdStep()
        screenModel.submitEmailAuth()
        advanceUntilIdle()

        assertEquals(1, authRepository.signUpCalls)
        assertEquals(LoginMessage.EmailConfirmationSent, screenModel.uiState.value.message)
    }

    @Test
    fun submitEmailAuth_signUpRejectsWeakPasswordBeforeRepositoryCall() = runTest(testDispatcher) {
        val authRepository = RecordingAuthRepository()
        val screenModel = makeModel(authRepository)

        screenModel.onEmailChange("user@example.com")
        screenModel.onPasswordChange("123")
        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Rosa")
        screenModel.advanceToHouseholdStep()
        advanceUntilIdle()

        assertEquals(0, authRepository.signUpCalls)
        assertEquals(
            LoginMessage.Error(LoginError.WeakPassword),
            screenModel.uiState.value.message,
        )
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
    fun canAdvanceToHouseholdStep_requiresNameEmailPassword() = runTest(testDispatcher) {
        val screenModel = makeModel()

        screenModel.toggleSignUpMode()
        assertFalse(screenModel.uiState.value.canAdvanceToHouseholdStep)

        screenModel.onDisplayNameChange("Marco")
        assertFalse(screenModel.uiState.value.canAdvanceToHouseholdStep)

        screenModel.onEmailChange("marco@example.com")
        assertFalse(screenModel.uiState.value.canAdvanceToHouseholdStep)

        screenModel.onPasswordChange("secret99")
        assertTrue(screenModel.uiState.value.canAdvanceToHouseholdStep)
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

    @Test
    fun toggleSignUpMode_resetsRegistrationStepToCredentials() = runTest(testDispatcher) {
        val screenModel = makeModel()

        screenModel.toggleSignUpMode()
        screenModel.onDisplayNameChange("Luca")
        screenModel.onEmailChange("luca@example.com")
        screenModel.onPasswordChange("pass123")
        screenModel.advanceToHouseholdStep()
        advanceUntilIdle()
        assertEquals(LoginRegistrationStep.HouseholdSetup, screenModel.uiState.value.registrationStep)

        screenModel.toggleSignUpMode()
        assertEquals(LoginRegistrationStep.Credentials, screenModel.uiState.value.registrationStep)
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
