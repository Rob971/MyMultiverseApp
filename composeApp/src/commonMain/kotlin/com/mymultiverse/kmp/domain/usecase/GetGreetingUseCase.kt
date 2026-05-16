package com.mymultiverse.kmp.domain.usecase

import com.mymultiverse.kmp.domain.model.Greeting
import com.mymultiverse.kmp.domain.repository.GreetingRepository

class GetGreetingUseCase(
    private val greetingRepository: GreetingRepository,
) {
    suspend operator fun invoke(): Greeting = greetingRepository.loadGreeting()
}
