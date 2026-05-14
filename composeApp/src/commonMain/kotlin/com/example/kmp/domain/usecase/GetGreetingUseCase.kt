package com.example.kmp.domain.usecase

import com.example.kmp.domain.model.Greeting
import com.example.kmp.domain.repository.GreetingRepository

class GetGreetingUseCase(
    private val greetingRepository: GreetingRepository,
) {
    suspend operator fun invoke(): Greeting = greetingRepository.loadGreeting()
}
