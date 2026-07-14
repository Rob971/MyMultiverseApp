package app.mymultiverse.ammo.presentation.screens.invite

import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.presentation.di.FakeHouseholdCollaborationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JoinHouseholdScreenModelTest {

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
    fun loadPreview_success_prefillsInvitedEmail() = runTest(testDispatcher) {
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(samplePreview())
        val model = createModel(this, collaboration)

        model.loadPreview("token-abc")
        advanceUntilIdle()

        val state = model.uiState.value
        assertIs<JoinPreviewState.Ready>(state.previewState)
        assertEquals("invitee@example.com", state.email)
        assertEquals(1, collaboration.previewInviteCalls)
    }

    @Test
    fun loadPreview_mapsExpiredInvite() = runTest(testDispatcher) {
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.failure(IllegalStateException("invite_expired"))
        val model = createModel(this, collaboration)

        model.loadPreview("token-abc")
        advanceUntilIdle()

        assertEquals(
            JoinPreviewState.Error(JoinPreviewError.Expired),
            model.uiState.value.previewState,
        )
    }

    @Test
    fun continueWithEmail_sendsOtpAndMovesToCodeStep() = runTest(testDispatcher) {
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(samplePreview())
        val auth = RecordingJoinAuthRepository()
        val model = createModel(this, collaboration, auth)

        model.loadPreview("token-abc")
        advanceUntilIdle()
        model.continueWithEmail()
        runCurrent()

        assertEquals(1, auth.sendEmailOtpCalls)
        assertEquals(JoinOtpStep.Code, model.uiState.value.step)
        assertEquals(JoinHouseholdMessage.OtpSent, model.uiState.value.message)
        assertFalse(model.uiState.value.canResendOtp)
        assertEquals(60, model.uiState.value.resendCooldownSeconds)
    }

    @Test
    fun verifyOtp_success_emitsAuthenticatedMessage() = runTest(testDispatcher) {
        val preview = samplePreview()
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(preview)
        val auth = RecordingJoinAuthRepository()
        val model = createModel(this, collaboration, auth)

        model.loadPreview("token-abc")
        advanceUntilIdle()
        model.onOtpCodeChange("123456")
        model.verifyOtp()
        advanceUntilIdle()

        assertEquals(1, auth.verifyEmailOtpCalls)
        val message = model.uiState.value.message
        assertIs<JoinHouseholdMessage.Authenticated>(message)
        assertEquals(preview, message.preview)
        assertIs<AuthState.Authenticated>(auth.authState.value)
    }

    @Test
    fun showEmailWarning_whenEditedEmailDiffersFromInvite() = runTest(testDispatcher) {
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(samplePreview())
        val model = createModel(this, collaboration)

        model.loadPreview("token-abc")
        advanceUntilIdle()
        assertFalse(model.uiState.value.showEmailWarning)

        model.onEmailChange("other@example.com")
        assertTrue(model.uiState.value.showEmailWarning)
    }

    @Test
    fun resendOtp_respectsCooldown() = runTest(testDispatcher) {
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(samplePreview())
        val auth = RecordingJoinAuthRepository()
        val model = createModel(this, collaboration, auth)

        model.loadPreview("token-abc")
        advanceUntilIdle()
        model.continueWithEmail()
        runCurrent()

        model.resendOtp()
        runCurrent()
        assertEquals(1, auth.sendEmailOtpCalls)

        model.resendOtp()
        runCurrent()
        assertEquals(1, auth.sendEmailOtpCalls)

        advanceTimeBy(60_000)
        runCurrent()
        model.resendOtp()
        runCurrent()
        assertEquals(2, auth.sendEmailOtpCalls)
    }

    private fun createModel(
        scope: TestScope,
        collaboration: FakeHouseholdCollaborationRepository,
        auth: AuthRepository = RecordingJoinAuthRepository(),
    ): JoinHouseholdScreenModel =
        JoinHouseholdScreenModel(
            collaborationRepository = collaboration,
            authRepository = auth,
            scope = scope,
        )

    private fun samplePreview(): HouseholdInvitePreview =
        HouseholdInvitePreview(
            inviteId = "invite-1",
            householdId = "household-1",
            householdName = "Rossi family",
            inviterName = "Marco",
            inviteeEmail = "invitee@example.com",
            role = HouseholdMemberRole.Editor,
            expiresAtEpochMillis = null,
        )
}

private class RecordingJoinAuthRepository : AuthRepository {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var sendEmailOtpCalls = 0
    var verifyEmailOtpCalls = 0

    override suspend fun restoreSession() = Unit

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String?,
    ): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun sendEmailOtp(email: String): Result<Unit> {
        sendEmailOtpCalls++
        return Result.success(Unit)
    }

    override suspend fun verifyEmailOtp(email: String, code: String): Result<Unit> {
        verifyEmailOtpCalls++
        _authState.value = AuthState.Authenticated(
            AuthUser(id = "user-1", email = email.trim(), displayName = null),
        )
        return Result.success(Unit)
    }

    override suspend fun signInWithGoogle(): Result<Unit> =
        Result.failure(UnsupportedOperationException("google_oauth_not_configured"))

    override suspend fun signInWithApple(): Result<Unit> =
        Result.failure(UnsupportedOperationException("apple_oauth_not_configured"))

    override suspend fun signOut() {
        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun exportPersonalData(): Result<String> = Result.success("{}")

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
}
