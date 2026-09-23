package app.mymultiverse.ammo.presentation.invite

import app.mymultiverse.ammo.data.invite.InviteRedirectUrls
import app.mymultiverse.ammo.data.invite.InviteSessionStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview
import app.mymultiverse.ammo.domain.model.sharing.Household
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembership
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl
import app.mymultiverse.ammo.presentation.di.FakeHouseholdCollaborationRepository
import app.mymultiverse.ammo.presentation.di.FakeHouseholdRepository
import app.mymultiverse.ammo.presentation.di.FakeNutritionSessionCoordinator
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CancellationException
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

        coordinator.handleInviteRedirect("app.mymultiverse.ammo://auth/callback")

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

    @Test
    fun acceptPendingInvite_cancellationResetsToIdle_notFailed() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings()).also { it.setPendingInviteToken("token-abc") }
        val collaboration = FakeHouseholdCollaborationRepository().also {
            it.previewInviteResult = Result.failure(CancellationException("scope cancelled"))
        }
        val coordinator = createCoordinator(this, store = store, collaboration = collaboration)

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        // Cancellation must NOT leave the UI stuck on Failed (would show error sheet permanently).
        assertIs<InviteJoinAcceptState.Idle>(coordinator.acceptState.value)
    }

    @Test
    fun acceptPendingInvite_transientPreviewError_retainsToken() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings()).also { it.setPendingInviteToken("token-abc") }
        val collaboration = FakeHouseholdCollaborationRepository().also {
            it.previewInviteResult = Result.failure(IllegalStateException("network timeout"))
        }
        val coordinator = createCoordinator(this, store = store, collaboration = collaboration)

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        // Transient failure must NOT destroy the invitation.
        assertEquals("token-abc", store.getPendingInviteToken())
        assertIs<InviteJoinAcceptState.Failed>(coordinator.acceptState.value)
    }

    @Test
    fun acceptPendingInvite_terminalPreviewError_clearsToken() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings()).also { it.setPendingInviteToken("token-abc") }
        val collaboration = FakeHouseholdCollaborationRepository().also {
            it.previewInviteResult = Result.failure(
                IllegalStateException(CollaborationErrorCodes.INVITE_NOT_FOUND),
            )
        }
        val coordinator = createCoordinator(this, store = store, collaboration = collaboration)

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        assertNull(store.getPendingInviteToken())
        assertNull(coordinator.pendingInviteToken.value)
    }

    @Test
    fun acceptPendingInvite_alreadyAccepted_reconcilesMembership() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings()).also { it.setPendingInviteToken("token-abc") }
        val collaboration = FakeHouseholdCollaborationRepository().also {
            it.previewInviteResult = Result.success(samplePreview())
            it.acceptInviteResult = Result.failure(
                IllegalStateException(CollaborationErrorCodes.INVITE_ALREADY_ACCEPTED),
            )
        }
        val active = HouseholdMembershipStatus.Active(
            HouseholdMembership(
                household = Household(
                    id = "household-1",
                    name = "Rossi family",
                    ownerId = "owner-1",
                    ownerDisplayName = "Owner",
                    nutritionFeatures = emptySet(),
                ),
                role = HouseholdMemberRole.Editor,
            ),
        )
        val householdRepository = FakeHouseholdRepository(initialMembershipStatus = active)
        val coordinator = createCoordinator(
            this, store = store, collaboration = collaboration, householdRepository = householdRepository,
        )

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        // Already-accepted: token cleared, no failure surfaced.
        assertNull(store.getPendingInviteToken())
        assertIs<InviteJoinAcceptState.Succeeded>(coordinator.acceptState.value)
    }

    @Test
    fun acceptPendingInvite_alreadyAcceptedUnrelatedMembership_isFailure() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings()).also { it.setPendingInviteToken("token-abc") }
        val collaboration = FakeHouseholdCollaborationRepository().also {
            it.previewInviteResult = Result.success(samplePreview())
            it.acceptInviteResult = Result.failure(
                IllegalStateException(CollaborationErrorCodes.INVITE_ALREADY_ACCEPTED),
            )
        }
        val unrelatedActive = HouseholdMembershipStatus.Active(
            HouseholdMembership(
                household = Household(
                    id = "OTHER-household",
                    name = "Someone else",
                    ownerId = "owner-2",
                    ownerDisplayName = "Owner 2",
                    nutritionFeatures = emptySet(),
                ),
                role = HouseholdMemberRole.Owner,
            ),
        )
        val householdRepository = FakeHouseholdRepository(initialMembershipStatus = unrelatedActive)
        val coordinator = createCoordinator(
            this, store = store, collaboration = collaboration, householdRepository = householdRepository,
        )

        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        // Active in a DIFFERENT household must not count as joining the invited one.
        assertIs<InviteJoinAcceptState.Failed>(coordinator.acceptState.value)
    }

    @Test
    fun pendingInviteToken_survivesCoordinatorRecreation() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings()).also { it.setPendingInviteToken("token-abc") }
        val first = createCoordinator(this, store = store)
        assertEquals("token-abc", first.pendingInviteToken.value)

        // A fresh coordinator over the same persistent store (process death / recreation)
        // must still hold the token.
        val second = createCoordinator(this, store = store)
        assertEquals("token-abc", second.pendingInviteToken.value)
    }

    @Test
    fun acceptPendingInvite_repeatedRetry_acceptsOnce() = runTest(testDispatcher) {
        val store = InviteSessionStore(MapSettings()).also { it.setPendingInviteToken("token-abc") }
        val collaboration = FakeHouseholdCollaborationRepository().also {
            it.previewInviteResult = Result.success(samplePreview())
            it.acceptInviteResult = Result.success(Unit)
        }
        val coordinator = createCoordinator(this, store = store, collaboration = collaboration)

        coordinator.acceptPendingInviteIfNeeded()
        coordinator.acceptPendingInviteIfNeeded()
        advanceUntilIdle()

        assertEquals(1, collaboration.acceptInviteCalls)
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
