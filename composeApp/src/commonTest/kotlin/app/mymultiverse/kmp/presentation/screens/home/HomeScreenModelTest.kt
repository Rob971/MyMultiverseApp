package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.model.sharing.AddMemberResult
import app.mymultiverse.kmp.domain.model.sharing.SpaceInvite
import app.mymultiverse.kmp.domain.model.sharing.SpaceMember
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.presentation.di.FakeAuthRepository
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.kmp.presentation.di.FakeSpaceCollaborationRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()

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
    ): HomeScreenModel =
        HomeScreenModel(
            getGreetingUseCase = GetGreetingUseCase(repository),
            authRepository = authRepository,
            householdRepository = FakeHouseholdRepository(),
            collaborationRepository = FakeSpaceCollaborationRepository(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
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
            collaborationRepository = HangingSpaceCollaborationRepository(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        advanceUntilIdle()

        assertEquals("Welcome home", screenModel.greeting.value?.text)
        assertFalse(screenModel.isRefreshing.value)
    }

    @Test
    fun refresh_stillLoadsPendingInvitesInBackground() = runTest(testDispatcher) {
        val collaborationRepository = TrackingSpaceCollaborationRepository()
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
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        advanceUntilIdle()

        assertEquals(1, collaborationRepository.refreshPendingInvitesCalls)
    }
}

private class HangingSpaceCollaborationRepository : SpaceCollaborationRepository {
    private val pendingInvites = MutableStateFlow<List<SpaceInvite>>(emptyList())

    override fun observeMembers(spaceId: String): Flow<List<SpaceMember>> =
        MutableStateFlow<List<SpaceMember>>(emptyList()).asStateFlow()

    override fun observePendingInvites(): Flow<List<SpaceInvite>> = pendingInvites.asStateFlow()

    override fun observeOutboundInvites(spaceId: String): Flow<List<SpaceInvite>> =
        MutableStateFlow<List<SpaceInvite>>(emptyList()).asStateFlow()

    override suspend fun refreshMembers(spaceId: String, ownerId: String, ownerDisplayName: String) = Unit

    override suspend fun refreshPendingInvites() {
        suspendCancellableCoroutine<Unit> { }
    }

    override suspend fun refreshOutboundInvites(spaceId: String) = Unit

    override suspend fun addMemberByEmail(
        spaceId: String,
        email: String,
        role: SpaceMemberRole,
    ): Result<AddMemberResult> = Result.failure(UnsupportedOperationException())

    override suspend fun removeMember(memberId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun acceptInvite(inviteId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    override suspend fun declineInvite(inviteId: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())
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

private class TrackingSpaceCollaborationRepository :
    SpaceCollaborationRepository by FakeSpaceCollaborationRepository() {
    var refreshPendingInvitesCalls = 0

    override suspend fun refreshPendingInvites() {
        refreshPendingInvitesCalls++
    }
}
