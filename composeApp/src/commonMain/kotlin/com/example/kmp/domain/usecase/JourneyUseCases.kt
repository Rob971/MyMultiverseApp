package com.example.kmp.domain.usecase

import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow

class GetJourneysUseCase(private val repository: JourneyRepository) {
    operator fun invoke(): Flow<List<Journey>> = repository.getJourneys()
}

class ToggleTaskUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, taskId: String) = 
        repository.toggleTask(journeyId, taskId)
}

class CheerTaskUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, taskId: String) = 
        repository.cheerTask(journeyId, taskId)
}

class RefreshJourneysUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke() = repository.refreshJourneys()
}
