package app.mymultiverse.kmp.presentation.screens.household

import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import app.mymultiverse.kmp.data.observability.NoOpCrashReporter
import app.mymultiverse.kmp.presentation.di.FakeAuthRepository
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.kmp.presentation.di.FakeSpaceCollaborationRepository
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

    private fun model(
        householdRepository: FakeHouseholdRepository,
    ): HouseholdGateScreenModel =
        HouseholdGateScreenModel(
            householdRepository = householdRepository,
            collaborationRepository = FakeSpaceCollaborationRepository(),
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
