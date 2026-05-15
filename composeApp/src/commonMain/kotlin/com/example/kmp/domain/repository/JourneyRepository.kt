package com.example.kmp.domain.repository

import com.example.kmp.domain.model.Journey
import kotlinx.coroutines.flow.Flow

interface JourneyRepository {
    fun getJourneys(): Flow<List<Journey>>
    suspend fun toggleTask(journeyId: String, taskId: String)
    suspend fun cheerTask(journeyId: String, taskId: String)
    suspend fun claimTask(journeyId: String, taskId: String, initials: String)
    suspend fun refreshJourneys()
}
