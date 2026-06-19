package app.mymultiverse.kmp.presentation.invite

import app.mymultiverse.kmp.data.invite.InviteRedirectUrls
import app.mymultiverse.kmp.data.invite.InviteSessionStore
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import app.mymultiverse.kmp.data.observability.NoOpCrashReporter
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.presentation.di.FakeHouseholdCollaborationRepository
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeNutritionSessionCoordinator
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InviteJoinFlowCoordinatorTest {

    private val testDispatcher = StandardTestDispatcher()
    private val logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "test"))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun handleInviteRedirect_persistsTokenFromUrl() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings())
        val coordinator = createCoordinator(this, store = store)

        coordinator.handleInviteRedirect(InviteRedirectUrls.build("token-abc"))

        assertEquals("token-abc", store.getPendingInviteToken())
        assertEquals("token-abc", coordinator.pendingInviteToken.value)
    }

    @Test
    fun handleInviteRedirect_ignoresInvalidUrl() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings())
        val coordinator = createCoordinator(this, store = store)

        coordinator.handleInviteRedirect("app.mymultiverse.kmp://auth/callback")

        assertNull(store.getPendingInviteToken())
        assertNull(coordinator.pendingInviteToken.value)
    }

    @Test
    fun acceptPendingInviteIfNeeded_previewsAcceptsAndClearsToken() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings())
        store.setPendingInviteToken("token-abc")
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(samplePreview())
        collaboration.acceptInviteResult = Result.success(Unit)
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        val sessionCoordinator = FakeNutritionSessionCoordinator(
            NutritionRepositoryImpl(MapSettings()),
        )
        val coordinator = createCoordinator(
            scope = this,
            store = store,
            collaboration = collaboration,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
        )

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        assertEquals(1, collaboration.previewInviteCalls)
        assertEquals(1, collaboration.acceptInviteCalls)
        assertEquals(1, householdRepository.refreshCalls)
        assertNull(store.getPendingInviteToken())
        assertNull(coordinator.pendingInviteToken.value)
        assertIs<InviteJoinAcceptState.Succeeded>(coordinator.acceptState.value)
        assertEquals("Rossi family", (coordinator.acceptState.value as InviteJoinAcceptState.Succeeded).householdName)
    }

    @Test
    fun acceptPendingInviteIfNeeded_clearsTokenWhenPreviewFails() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings())
        store.setPendingInviteToken("token-abc")
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.failure(IllegalStateException("invite_expired"))
        val coordinator = createCoordinator(this, store = store, collaboration = collaboration)

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        assertEquals(1, collaboration.previewInviteCalls)
        assertEquals(0, collaboration.acceptInviteCalls)
        assertNull(store.getPendingInviteToken())
        assertIs<InviteJoinAcceptState.Failed>(coordinator.acceptState.value)
    }

    @Test
    fun acceptPendingInviteIfNeeded_mapsEmailMismatchFailure() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings())
        store.setPendingInviteToken("token-abc")
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(samplePreview())
        collaboration.acceptInviteResult = Result.failure(IllegalStateException("invite_email_mismatch"))
        val coordinator = createCoordinator(this, store = store, collaboration = collaboration)

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        assertEquals("token-abc", store.getPendingInviteToken())
        val failed = coordinator.acceptState.value
        assertIs<InviteJoinAcceptState.Failed>(failed)
        assertEquals(InviteJoinAcceptError.EmailMismatch, failed.error)
        assertEquals("invitee@example.com", failed.mismatchContext?.invitedEmail)
        assertEquals("Rossi family", failed.mismatchContext?.householdName)
    }

    @Test
    fun retryAfterEmailMismatch_clearsFailureState() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings())
        store.setPendingInviteToken("token-abc")
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.previewInviteResult = Result.success(samplePreview())
        collaboration.acceptInviteResult = Result.failure(IllegalStateException("invite_email_mismatch"))
        var signedOut = false
        val coordinator = createCoordinator(this, store = store, collaboration = collaboration)

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()
        assertIs<InviteJoinAcceptState.Failed>(coordinator.acceptState.value)

        coordinator.retryAfterEmailMismatch { signedOut = true }
        advanceUntilIdle()

        assertTrue(signedOut)
        assertIs<InviteJoinAcceptState.Idle>(coordinator.acceptState.value)
        assertEquals("token-abc", store.getPendingInviteToken())
    }

    private fun createCoordinator(
        scope: kotlinx.coroutines.test.TestScope,
        store: InviteSessionStore = InviteSessionStore(MapSettings()),
        collaboration: FakeHouseholdCollaborationRepository = FakeHouseholdCollaborationRepository(),
        householdRepository: FakeHouseholdRepository = FakeHouseholdRepository(),
        sessionCoordinator: FakeNutritionSessionCoordinator = FakeNutritionSessionCoordinator(
            NutritionRepositoryImpl(MapSettings()),
        ),
    ): InviteJoinFlowCoordinator =
        InviteJoinFlowCoordinator(
            inviteSessionStore = store,
            collaborationRepository = collaboration,
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
            logger = logger,
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
