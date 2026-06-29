package app.mymultiverse.ammo.domain.usecase

import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.domain.repository.GreetingRepository

class GetGreetingUseCase(
    private val greetingRepository: GreetingRepository,
) {
    suspend operator fun invoke(): Greeting = greetingRepository.loadGreeting()
}
