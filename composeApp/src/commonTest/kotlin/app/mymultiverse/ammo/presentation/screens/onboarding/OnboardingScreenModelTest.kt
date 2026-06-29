package app.mymultiverse.ammo.presentation.screens.onboarding

import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.presentation.di.FakeAuthRepository
import app.mymultiverse.ammo.presentation.di.FakeHouseholdCollaborationRepository
import app.mymultiverse.ammo.presentation.screens.auth.LoginError
import app.mymultiverse.ammo.presentation.screens.auth.LoginMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingScreenModelTest {

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
    fun loadInvitePreview_setsReadyStateOnSuccess() = runTest(testDispatcher) {
        val collaborationRepository = FakeHouseholdCollaborationRepository().apply {
            previewInviteResult = Result.success(
                HouseholdInvitePreview(
                    inviteId = "invite-1",
                    householdId = "household-1",
                    householdName = "Rivera Family",
                    inviterName = "Alex",
                    inviteeEmail = "guest@example.com",
                    role = HouseholdMemberRole.Editor,
                    expiresAtEpochMillis = null,
                ),
            )
        }
        val model = OnboardingScreenModel(
            authRepository = FakeAuthRepository(),
            collaborationRepository = collaborationRepository,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + SupervisorJob()),
        )

        model.loadInvitePreview("token-1")
        advanceUntilIdle()

        val state = model.uiState.value
        assertIs<InvitePreviewState.Ready>(state.invitePreviewState)
        assertEquals("Rivera Family", state.inviteHouseholdName)
    }

    @Test
    fun signInWithGoogle_mapsProviderNotConfiguredError() = runTest(testDispatcher) {
        val model = OnboardingScreenModel(
            authRepository = FakeAuthRepository(),
            collaborationRepository = FakeHouseholdCollaborationRepository(),
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + SupervisorJob()),
        )

        model.signInWithGoogle()
        advanceUntilIdle()

        assertEquals(
            LoginMessage.Error(LoginError.ProviderComingSoon),
            model.uiState.value.message,
        )
    }

    @Test
    fun loadInvitePreview_withBlankToken_clearsPreview() = runTest(testDispatcher) {
        val model = OnboardingScreenModel(
            authRepository = FakeAuthRepository(),
            collaborationRepository = FakeHouseholdCollaborationRepository(),
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + SupervisorJob()),
        )

        model.loadInvitePreview("   ")
        advanceUntilIdle()

        assertEquals(InvitePreviewState.Idle, model.uiState.value.invitePreviewState)
        assertNull(model.uiState.value.inviteHouseholdName)
    }
}
