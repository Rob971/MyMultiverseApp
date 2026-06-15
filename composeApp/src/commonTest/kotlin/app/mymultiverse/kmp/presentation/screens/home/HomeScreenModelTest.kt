package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.presentation.di.FakeAuthRepository
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

    private fun model(repository: FakeGreetingRepository): HomeScreenModel =
        HomeScreenModel(
            getGreetingUseCase = GetGreetingUseCase(repository),
            authRepository = FakeAuthRepository(),
            collaborationRepository = FakeSpaceCollaborationRepository(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = NutritionRepositoryImpl(MapSettings()),
            ),
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

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
