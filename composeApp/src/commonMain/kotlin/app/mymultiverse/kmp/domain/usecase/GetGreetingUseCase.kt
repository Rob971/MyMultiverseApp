package app.mymultiverse.kmp.domain.usecase

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.repository.GreetingRepository

class GetGreetingUseCase(
    private val greetingRepository: GreetingRepository,
) {
    suspend operator fun invoke(): Greeting = greetingRepository.loadGreeting()
}
