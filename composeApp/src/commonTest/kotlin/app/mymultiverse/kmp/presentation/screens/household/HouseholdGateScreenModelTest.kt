package app.mymultiverse.kmp.presentation.screens.household

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import app.mymultiverse.kmp.data.observability.NoOpCrashReporter
import app.mymultiverse.kmp.presentation.di.FakeAuthRepository
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.kmp.presentation.di.FakeHouseholdCollaborationRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdGateScreenModelTest {

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
    fun refreshMembership_setsActiveWhenHouseholdExists() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository()
        val model = model(householdRepository)

        advanceUntilIdle()

        assertIs<HouseholdMembershipStatus.Active>(model.uiState.value.membershipStatus)
    }

    @Test
    fun refreshMembership_setsNoneWhenNoHousehold() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val model = model(householdRepository)

        advanceUntilIdle()

        assertEquals(HouseholdMembershipStatus.None, model.uiState.value.membershipStatus)
    }

    @Test
    fun createHousehold_promotesToActive() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val model = model(householdRepository)

        advanceUntilIdle()
        model.onHouseholdNameChange("Rossi home")
        model.createHousehold()
        advanceUntilIdle()

        assertIs<HouseholdMembershipStatus.Active>(model.uiState.value.membershipStatus)
        assertEquals("Rossi home", householdRepository.lastCreatedName)
    }

    @Test
    fun createHousehold_ignoredWhenNameBlank() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val model = model(householdRepository)

        advanceUntilIdle()
        model.createHousehold()
        advanceUntilIdle()

        assertEquals(0, householdRepository.createCalls)
    }

    @Test
    fun pendingInvites_visibleWhenMembershipIsNone() = runTest(testDispatcher) {
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.inboundProfileEmail = "invitee@example.com"
        collaboration.addMemberByEmail(
            householdId = "household-1",
            email = "invitee@example.com",
            role = HouseholdMemberRole.Editor,
        )

        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val model = model(
            householdRepository = householdRepository,
            collaborationRepository = collaboration,
        )

        advanceUntilIdle()

        assertEquals(HouseholdMembershipStatus.None, model.uiState.value.membershipStatus)
        assertEquals(1, model.uiState.value.pendingInvites.size)
        assertEquals("invitee@example.com", model.uiState.value.pendingInvites.single().email)
    }

    @Test
    fun acceptInvite_emitsJoinedMessage() = runTest(testDispatcher) {
        val collaboration = FakeHouseholdCollaborationRepository()
        collaboration.inboundProfileEmail = "invitee@example.com"
        collaboration.addMemberByEmail(
            householdId = "household-1",
            email = "invitee@example.com",
            role = HouseholdMemberRole.Editor,
        )
        collaboration.refreshPendingInvites()

        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val model = model(
            householdRepository = householdRepository,
            collaborationRepository = collaboration,
        )

        advanceUntilIdle()
        val inviteId = model.uiState.value.pendingInvites.single().id
        model.acceptInvite(inviteId)
        advanceUntilIdle()

        val message = model.uiState.value.inviteActionMessage
        assertTrue(message is InviteActionMessage.Joined)
        assertEquals("Test Household", (message as InviteActionMessage.Joined).householdName)
    }

    @Test
    fun createHousehold_activatesNutritionSession() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val sessionCoordinator = FakeNutritionSessionCoordinator(
            initialRepository = NutritionRepositoryImpl(MapSettings()),
        )
        val model = HouseholdGateScreenModel(
            householdRepository = householdRepository,
            collaborationRepository = FakeHouseholdCollaborationRepository(),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "test-user", email = "test@example.com", displayName = "Test User"),
                ),
            ),
            sessionCoordinator = sessionCoordinator,
            logger = logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        advanceUntilIdle()
        model.onHouseholdNameChange("Rossi home")
        model.createHousehold()
        advanceUntilIdle()

        assertEquals("household-1", sessionCoordinator.activatedHouseholdId)
    }

    private fun model(
        householdRepository: FakeHouseholdRepository,
        collaborationRepository: FakeHouseholdCollaborationRepository = FakeHouseholdCollaborationRepository(),
    ): HouseholdGateScreenModel =
        HouseholdGateScreenModel(
            householdRepository = householdRepository,
            collaborationRepository = collaborationRepository,
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "test-user", email = "test@example.com", displayName = "Test User"),
                ),
            ),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
            logger = logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
}
