package app.mymultiverse.ammo.presentation.screens.home

import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.model.sharing.AddMemberResult
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMember
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.repository.GreetingRepository
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.usecase.GetGreetingUseCase
import app.mymultiverse.ammo.data.home.HomeFirstWinChecklistStore
import app.mymultiverse.ammo.data.home.HomeWeekPlanNudgeStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl
import app.mymultiverse.ammo.presentation.di.FakeAuthRepository
import app.mymultiverse.ammo.presentation.di.FakeHouseholdRepository
import app.mymultiverse.ammo.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.ammo.presentation.di.FakePersonalDataExporter
import app.mymultiverse.ammo.presentation.di.FakePushNotificationRegistrar
import app.mymultiverse.ammo.presentation.di.FakeHouseholdCollaborationRepository
import app.mymultiverse.ammo.presentation.screens.household.InviteActionMessage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class HomeScreenModelTest {

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

    private fun model(
        repository: FakeGreetingRepository,
        authRepository: FakeAuthRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "test-user", email = "test@example.com", displayName = "Test User"),
            ),
        ),
        householdRepository: FakeHouseholdRepository = FakeHouseholdRepository(),
        collaborationRepository: FakeHouseholdCollaborationRepository = FakeHouseholdCollaborationRepository(),
        personalDataExporter: FakePersonalDataExporter = FakePersonalDataExporter(),
        pushNotificationRegistrar: FakePushNotificationRegistrar = FakePushNotificationRegistrar(),
        sessionCoordinator: FakeNutritionSessionCoordinator = FakeNutritionSessionCoordinator(
            initialRepository = NutritionRepositoryImpl(MapSettings()),
        ),
        firstWinChecklistStore: HomeFirstWinChecklistStore = HomeFirstWinChecklistStore(MapSettings()),
        weekPlanNudgeStore: HomeWeekPlanNudgeStore = HomeWeekPlanNudgeStore(MapSettings()),
    ): HomeScreenModel =
        HomeScreenModel(
            getGreetingUseCase = GetGreetingUseCase(repository),
            authRepository = authRepository,
            householdRepository = householdRepository,
            collaborationRepository = collaborationRepository,
            sessionCoordinator = sessionCoordinator,
            personalDataExporter = personalDataExporter,
            pushNotificationRegistrar = pushNotificationRegistrar,
            firstWinChecklistStore = firstWinChecklistStore,
            weekPlanNudgeStore = weekPlanNudgeStore,
            logger = logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

    @Test
    fun userDisplayName_isAvailableImmediatelyForAuthenticatedSession() = runTest(testDispatcher) {
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "user-1", email = "roberto@example.com", displayName = "Roberto"),
                ),
            ),
        )

        assertEquals("Roberto", screenModel.userDisplayName.value)
    }

    @Test
    fun userDisplayName_reflectsAuthenticatedProfile() = runTest(testDispatcher) {
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "user-1", email = "roberto@example.com", displayName = "Roberto"),
                ),
            ),
        )

        advanceUntilIdle()

        assertEquals("Roberto", screenModel.userDisplayName.value)
    }

    @Test
    fun userDisplayName_fallsBackToEmailWhenDisplayNameMissing() = runTest(testDispatcher) {
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "user-1", email = "maria@example.com", displayName = null),
                ),
            ),
        )

        advanceUntilIdle()

        assertEquals("maria", screenModel.userDisplayName.value)
    }

    @Test
    fun userDisplayName_isNullWhenUnauthenticated() = runTest(testDispatcher) {
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = FakeAuthRepository(initialState = AuthState.Unauthenticated),
        )

        advanceUntilIdle()

        assertEquals(null, screenModel.userDisplayName.value)
    }

    @Test
    fun userDisplayName_persistsAcrossGreetingRefresh() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Initial"))
        val screenModel = model(
            repository = repository,
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "user-1", email = "roberto@example.com", displayName = "Roberto"),
                ),
            ),
        )
        advanceUntilIdle()

        repository.nextGreeting = Greeting("Refreshed")
        screenModel.refresh()
        advanceUntilIdle()

        assertEquals("Roberto", screenModel.userDisplayName.value)
        assertEquals("Refreshed", screenModel.greeting.value?.text)
    }

    @Test
    fun userDisplayName_clearsAfterSignOut() = runTest(testDispatcher) {
        val authRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "roberto@example.com", displayName = "Roberto"),
            ),
        )
        val screenModel = model(FakeGreetingRepository(Greeting("Welcome home")), authRepository)
        advanceUntilIdle()

        screenModel.signOut()
        advanceUntilIdle()

        assertEquals(null, screenModel.userDisplayName.value)
    }

    @Test
    fun init_loadsGreeting() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Welcome home"))
        val screenModel = model(repository)

        advanceUntilIdle()

        assertEquals("Welcome home", screenModel.greeting.value?.text)
        assertFalse(screenModel.isRefreshing.value)
    }

    @Test
    fun refresh_replacesGreetingWhenSuccessful() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Again"))
        val screenModel = model(repository)
        advanceUntilIdle()

        repository.nextGreeting = Greeting("Refreshed")
        screenModel.refresh()
        advanceUntilIdle()

        assertFalse(screenModel.isRefreshing.value)
        assertEquals("Refreshed", screenModel.greeting.value?.text)
    }

    @Test
    fun refresh_clearsRefreshingEvenWhenRepositoryFails() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Initial"))
        val screenModel = model(repository)
        advanceUntilIdle()

        repository.failOnLoad = true
        screenModel.refresh()
        advanceUntilIdle()

        assertFalse(screenModel.isRefreshing.value)
        assertEquals("Initial", screenModel.greeting.value?.text)
    }

    @Test
    fun refresh_clearsRefreshingEvenWhenPendingInvitesHang() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Welcome home"))
        val screenModel = HomeScreenModel(
            getGreetingUseCase = GetGreetingUseCase(repository),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "test-user", email = "test@example.com", displayName = "Test User"),
                ),
            ),
            householdRepository = FakeHouseholdRepository(),
            collaborationRepository = HangingHouseholdCollaborationRepository(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
            personalDataExporter = FakePersonalDataExporter(),
            pushNotificationRegistrar = FakePushNotificationRegistrar(),
            firstWinChecklistStore = HomeFirstWinChecklistStore(MapSettings()),
            weekPlanNudgeStore = HomeWeekPlanNudgeStore(MapSettings()),
            logger = logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        advanceUntilIdle()

        assertEquals("Welcome home", screenModel.greeting.value?.text)
        assertFalse(screenModel.isRefreshing.value)
    }

    @Test
    fun onAcceptInviteClicked_whenAffiliated_showsSwitchPrompt() = runTest(testDispatcher) {
        val screenModel = HomeScreenModel(
            getGreetingUseCase = GetGreetingUseCase(FakeGreetingRepository(Greeting("Welcome home"))),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "test-user", email = "test@example.com", displayName = "Test User"),
                ),
            ),
            householdRepository = FakeHouseholdRepository(),
            collaborationRepository = FakeHouseholdCollaborationRepository(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
            personalDataExporter = FakePersonalDataExporter(),
            pushNotificationRegistrar = FakePushNotificationRegistrar(),
            firstWinChecklistStore = HomeFirstWinChecklistStore(MapSettings()),
            weekPlanNudgeStore = HomeWeekPlanNudgeStore(MapSettings()),
            logger = logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        advanceUntilIdle()

        val invite = HouseholdInvite(
            id = "invite-1",
            householdId = "household-2",
            householdName = "Partner home",
            email = "test@example.com",
            role = HouseholdMemberRole.Editor,
            expiresAtEpochMillis = 4_102_444_800_000L,
        )
        screenModel.onAcceptInviteClicked(invite)
        advanceUntilIdle()

        assertEquals("Partner home", screenModel.switchHouseholdPrompt.value?.invitedHouseholdName)
        assertEquals("Our household", screenModel.switchHouseholdPrompt.value?.currentHouseholdName)
    }

    @Test
    fun refresh_stillLoadsPendingInvitesInBackground() = runTest(testDispatcher) {
        val collaborationRepository = TrackingHouseholdCollaborationRepository()
        val screenModel = HomeScreenModel(
            getGreetingUseCase = GetGreetingUseCase(FakeGreetingRepository(Greeting("Welcome home"))),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "test-user", email = "test@example.com", displayName = "Test User"),
                ),
            ),
            householdRepository = FakeHouseholdRepository(),
            collaborationRepository = collaborationRepository,
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
            personalDataExporter = FakePersonalDataExporter(),
            pushNotificationRegistrar = FakePushNotificationRegistrar(),
            firstWinChecklistStore = HomeFirstWinChecklistStore(MapSettings()),
            weekPlanNudgeStore = HomeWeekPlanNudgeStore(MapSettings()),
            logger = logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        advanceUntilIdle()

        assertEquals(2, collaborationRepository.refreshPendingInvitesCalls)
    }

    @Test
    fun acceptInvite_emitsJoinedMessage() = runTest(testDispatcher) {
        val collaborationRepository = FakeHouseholdCollaborationRepository()
        val screenModel = HomeScreenModel(
            getGreetingUseCase = GetGreetingUseCase(FakeGreetingRepository(Greeting("Welcome home"))),
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "test-user", email = "test@example.com", displayName = "Test User"),
                ),
            ),
            householdRepository = FakeHouseholdRepository(
                initialMembershipStatus = HouseholdMembershipStatus.None,
            ),
            collaborationRepository = collaborationRepository,
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
            personalDataExporter = FakePersonalDataExporter(),
            pushNotificationRegistrar = FakePushNotificationRegistrar(),
            firstWinChecklistStore = HomeFirstWinChecklistStore(MapSettings()),
            weekPlanNudgeStore = HomeWeekPlanNudgeStore(MapSettings()),
            logger = logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )
        advanceUntilIdle()

        collaborationRepository.addMemberByEmail(
            householdId = "household-1",
            email = "partner@example.com",
            role = HouseholdMemberRole.Editor,
        )
        val invite = collaborationRepository.latestOutboundInvite("household-1")
            ?: error("expected outbound invite")
        screenModel.acceptInvite(invite.id, invite.householdName)
        advanceUntilIdle()

        val message = screenModel.inviteActionMessage.value
        assertTrue(message is InviteActionMessage.Joined)
        assertEquals("Test Household", (message as InviteActionMessage.Joined).householdName)
    }

    @Test
    fun exportPersonalData_whenShareSucceeds_emitsSuccessMessage() = runTest(testDispatcher) {
        val exporter = FakePersonalDataExporter(shareResult = true)
        val authRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "test@example.com", displayName = "Test User"),
            ),
        )
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = authRepository,
            personalDataExporter = exporter,
        )

        screenModel.exportPersonalData()
        advanceUntilIdle()

        assertEquals(PersonalDataExportMessage.Success, screenModel.personalDataExportMessage.value)
        assertTrue(exporter.lastSharedContent?.contains("test@example.com") == true)
    }

    @Test
    fun exportPersonalData_whenShareUnavailable_emitsShareUnavailableMessage() = runTest(testDispatcher) {
        val exporter = FakePersonalDataExporter(shareResult = false)
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            personalDataExporter = exporter,
        )

        screenModel.exportPersonalData()
        advanceUntilIdle()

        assertEquals(PersonalDataExportMessage.ShareUnavailable, screenModel.personalDataExportMessage.value)
    }

    @Test
    fun exportPersonalData_whenRepositoryFails_emitsErrorMessage() = runTest(testDispatcher) {
        val authRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "test@example.com", displayName = "Test User"),
            ),
        ).apply {
            exportPersonalDataResult = Result.failure(IllegalStateException("export_failed"))
        }
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = authRepository,
        )

        screenModel.exportPersonalData()
        advanceUntilIdle()

        assertEquals(PersonalDataExportMessage.Error, screenModel.personalDataExportMessage.value)
    }

    @Test
    fun confirmDeleteAccount_onSuccess_deactivatesSessionAndDeletesAccount() = runTest(testDispatcher) {
        val authRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "test@example.com", displayName = "Test User"),
            ),
        )
        val sessionCoordinator = FakeNutritionSessionCoordinator(
            initialRepository = NutritionRepositoryImpl(MapSettings()),
        )
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = authRepository,
            sessionCoordinator = sessionCoordinator,
        )

        screenModel.requestDeleteAccount()
        assertTrue(screenModel.showDeleteAccountDialog.value)
        screenModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertFalse(screenModel.showDeleteAccountDialog.value)
        assertEquals(DeleteAccountMessage.Success, screenModel.deleteAccountMessage.value)
        assertEquals(1, sessionCoordinator.deactivateCount)
        assertEquals(1, authRepository.deleteAccountCalls)
    }

    @Test
    fun confirmDeleteAccount_whenOwnerMustTransfer_emitsOwnerMessage() = runTest(testDispatcher) {
        val authRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "test@example.com", displayName = "Test User"),
            ),
        ).apply {
            deleteAccountResult = Result.failure(
                IllegalStateException(CollaborationErrorCodes.OWNER_MUST_TRANSFER_OR_DISSOLVE),
            )
        }
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = authRepository,
        )

        screenModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertEquals(DeleteAccountMessage.OwnerMustTransfer, screenModel.deleteAccountMessage.value)
    }

    @Test
    fun confirmDeleteAccount_onGenericFailure_emitsErrorMessage() = runTest(testDispatcher) {
        val authRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "test@example.com", displayName = "Test User"),
            ),
        ).apply {
            deleteAccountResult = Result.failure(IllegalStateException("delete_failed"))
        }
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = authRepository,
        )

        screenModel.confirmDeleteAccount()
        advanceUntilIdle()

        assertEquals(DeleteAccountMessage.Error, screenModel.deleteAccountMessage.value)
    }

    @Test
    fun dismissDeleteAccountDialog_hidesDialogWithoutDeleting() = runTest(testDispatcher) {
        val authRepository = FakeAuthRepository(
            initialState = AuthState.Authenticated(
                AuthUser(id = "user-1", email = "test@example.com", displayName = "Test User"),
            ),
        )
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            authRepository = authRepository,
        )

        screenModel.requestDeleteAccount()
        screenModel.dismissDeleteAccountDialog()
        advanceUntilIdle()

        assertFalse(screenModel.showDeleteAccountDialog.value)
        assertNull(screenModel.deleteAccountMessage.value)
        assertEquals(0, authRepository.deleteAccountCalls)
    }

    @Test
    fun homePhase_isWelcomeWhenHouseholdActive() = runTest(testDispatcher) {
        val screenModel = model(FakeGreetingRepository(Greeting("Welcome home")))
        advanceUntilIdle()
        assertEquals(HomePhase.Welcome, screenModel.homePhase.value)
    }

    @Test
    fun homePhase_isOnboardingWhenNoHousehold() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
        )
        advanceUntilIdle()
        assertEquals(HomePhase.Onboarding, screenModel.homePhase.value)
    }

    @Test
    fun createHousehold_promotesToWelcome() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
        )
        advanceUntilIdle()
        screenModel.onHouseholdNameChange("Rossi home")
        advanceTimeBy(400)
        advanceUntilIdle()
        screenModel.createHousehold()
        advanceUntilIdle()
        assertEquals(HomePhase.Welcome, screenModel.homePhase.value)
        assertEquals("Rossi home", householdRepository.lastCreatedName)
    }

    @Test
    fun createHousehold_emitsPostCreateInvitePrompt() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
        )
        advanceUntilIdle()
        screenModel.onHouseholdNameChange("Rossi home")
        advanceTimeBy(400)
        advanceUntilIdle()
        screenModel.createHousehold()
        advanceUntilIdle()
        assertEquals("Rossi home", screenModel.postCreateInvitePrompt.value?.householdName)
        screenModel.clearPostCreateInvitePrompt()
        assertEquals(null, screenModel.postCreateInvitePrompt.value)
    }

    @Test
    fun nutritionHouseholdContext_emitsActiveHouseholdFromMembership() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository()
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
        )
        advanceUntilIdle()

        val context = screenModel.nutritionHouseholdContext.value
        assertEquals("household-1", context?.id)
        assertEquals("Our household", context?.name)
    }

    @Test
    fun createHousehold_ignoredWhenNameBlank() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
            authRepository = FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(id = "test-user", email = "", displayName = null),
                ),
            ),
        )
        advanceUntilIdle()
        screenModel.createHousehold()
        advanceUntilIdle()
        assertEquals(0, householdRepository.createCalls)
    }

    @Test
    fun pendingInvites_visibleDuringOnboarding() = runTest(testDispatcher) {
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
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
            collaborationRepository = collaboration,
        )
        advanceUntilIdle()
        assertEquals(HomePhase.Onboarding, screenModel.homePhase.value)
        assertEquals(1, screenModel.pendingInvites.value.size)
        assertEquals("invitee@example.com", screenModel.pendingInvites.value.single().email)
    }

    @Test
    fun createHousehold_showsFirstWinChecklist() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.None,
        )
        householdRepository.setMembershipStatus(HouseholdMembershipStatus.None)
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
        )
        advanceUntilIdle()
        screenModel.onHouseholdNameChange("Rossi home")
        advanceTimeBy(400)
        advanceUntilIdle()
        screenModel.createHousehold()
        advanceUntilIdle()
        assertTrue(screenModel.firstWinChecklist.value.visible)
        assertFalse(screenModel.firstWinChecklist.value.inviteComplete)
        assertFalse(screenModel.firstWinChecklist.value.nutritionComplete)
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
        val screenModel = model(
            repository = FakeGreetingRepository(Greeting("Welcome home")),
            householdRepository = householdRepository,
            sessionCoordinator = sessionCoordinator,
        )
        advanceUntilIdle()
        screenModel.onHouseholdNameChange("Rossi home")
        advanceTimeBy(400)
        advanceUntilIdle()
        screenModel.createHousehold()
        advanceUntilIdle()
        assertEquals("household-1", sessionCoordinator.activatedHouseholdId)
    }
}

private class HangingHouseholdCollaborationRepository : HouseholdCollaborationRepository {
    private val pendingInvites = MutableStateFlow<List<HouseholdInvite>>(emptyList())

    override fun observeMembers(householdId: String): Flow<List<HouseholdMember>> =
        MutableStateFlow<List<HouseholdMember>>(emptyList()).asStateFlow()

    override fun observePendingInvites(): Flow<List<HouseholdInvite>> = pendingInvites.asStateFlow()

    override fun observeOutboundInvites(householdId: String): Flow<List<HouseholdInvite>> =
        MutableStateFlow<List<HouseholdInvite>>(emptyList()).asStateFlow()

    override suspend fun refreshMembers(householdId: String, ownerId: String, ownerDisplayName: String) = Unit

    override suspend fun refreshPendingInvites() {
        suspendCancellableCoroutine<Unit> { }
    }

    override suspend fun previewInvite(token: String): Result<app.mymultiverse.ammo.domain.model.sharing.HouseholdInvitePreview> =
        Result.failure(UnsupportedOperationException())

    override suspend fun refreshOutboundInvites(householdId: String) = Unit

    override suspend fun addMemberByEmail(
        householdId: String,
        email: String,
        role: HouseholdMemberRole,
    ): Result<AddMemberResult> = Result.failure(UnsupportedOperationException())

    override suspend fun removeMember(memberId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun acceptInvite(inviteId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun declineInvite(inviteId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun addDependant(householdId: String, displayName: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun removeDependant(dependantId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun nudgePartnersToUpdateGroceryList(
        householdId: String,
        weekKey: String,
    ): Result<Unit> = Result.failure(UnsupportedOperationException())

    override suspend fun nudgePartnersToUpdateMealPlan(
        householdId: String,
        weekKey: String,
    ): Result<Unit> = Result.failure(UnsupportedOperationException())

    override suspend fun updateMemberRole(
        memberId: String,
        role: HouseholdMemberRole,
    ): Result<Unit> = Result.failure(UnsupportedOperationException())

    override suspend fun updateMemberAvatar(
        householdId: String,
        member: HouseholdMember,
        imageBytes: ByteArray,
        contentType: String,
    ): Result<Unit> = Result.failure(UnsupportedOperationException())
}

private class FakeGreetingRepository(
    initial: Greeting,
) : GreetingRepository {
    var nextGreeting: Greeting = initial
    var failOnLoad: Boolean = false

    override suspend fun loadGreeting(): Greeting {
        if (failOnLoad) error("load failed")
        return nextGreeting
    }
}

private class TrackingHouseholdCollaborationRepository :
    HouseholdCollaborationRepository by FakeHouseholdCollaborationRepository() {
    var refreshPendingInvitesCalls = 0

    override suspend fun refreshPendingInvites() {
        refreshPendingInvitesCalls++
    }
}
