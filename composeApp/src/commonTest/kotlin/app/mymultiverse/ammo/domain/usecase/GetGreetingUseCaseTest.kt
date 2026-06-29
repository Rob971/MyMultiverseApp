package app.mymultiverse.ammo.domain.usecase

import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.domain.repository.GreetingRepository
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
