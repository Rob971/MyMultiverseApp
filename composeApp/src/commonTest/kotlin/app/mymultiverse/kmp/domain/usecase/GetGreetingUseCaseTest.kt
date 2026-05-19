package app.mymultiverse.kmp.domain.usecase

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetGreetingUseCaseTest {

    @Test
    fun invoke_returnsRepositoryGreeting() = runTest {
        val expected = Greeting("Ciao famiglia")
        val useCase = GetGreetingUseCase(
            object : GreetingRepository {
                override suspend fun loadGreeting(): Greeting = expected
            },
        )

        assertEquals(expected, useCase())
    }
}
