package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
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

    @Test
    fun init_loadsGreeting() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Welcome home"))
        val model = HomeScreenModel(GetGreetingUseCase(repository))

        advanceUntilIdle()

        assertEquals("Welcome home", model.greeting.value?.text)
        assertFalse(model.isRefreshing.value)
    }

    @Test
    fun refresh_replacesGreetingWhenSuccessful() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Again"))
        val model = HomeScreenModel(GetGreetingUseCase(repository))
        advanceUntilIdle()

        repository.nextGreeting = Greeting("Refreshed")
        model.refresh()
        advanceUntilIdle()

        assertFalse(model.isRefreshing.value)
        assertEquals("Refreshed", model.greeting.value?.text)
    }

    @Test
    fun refresh_clearsRefreshingEvenWhenRepositoryFails() = runTest(testDispatcher) {
        val repository = FakeGreetingRepository(Greeting("Initial"))
        val model = HomeScreenModel(GetGreetingUseCase(repository))
        advanceUntilIdle()

        repository.failOnLoad = true
        model.refresh()
        advanceUntilIdle()

        assertFalse(model.isRefreshing.value)
        assertEquals("Initial", model.greeting.value?.text)
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
