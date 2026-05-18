package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.Journey
import app.mymultiverse.kmp.domain.model.FinanceBillEntry
import app.mymultiverse.kmp.domain.model.JourneyTask
import kotlinx.coroutines.flow.Flow

interface JourneyRepository {
    fun getJourneys(): Flow<List<Journey>>
    suspend fun upsertJourney(journey: Journey)
    suspend fun deleteJourney(id: String)
    suspend fun toggleTask(journeyId: String, taskId: String)
    suspend fun cheerTask(journeyId: String, taskId: String)
    suspend fun claimTask(journeyId: String, taskId: String, initials: String)
    suspend fun addTask(journeyId: String, task: JourneyTask)
    suspend fun updateTask(task: JourneyTask)
    suspend fun deleteTask(journeyId: String, taskId: String)
    suspend fun addFinanceBillEntry(journeyId: String, entry: FinanceBillEntry)
    suspend fun refreshJourneys()
}
