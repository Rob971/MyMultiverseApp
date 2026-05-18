package app.mymultiverse.kmp.domain.usecase

import app.mymultiverse.kmp.domain.model.Journey
import app.mymultiverse.kmp.domain.model.FinanceBillEntry
import app.mymultiverse.kmp.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow

class GetJourneysUseCase(private val repository: JourneyRepository) {
    operator fun invoke(): Flow<List<Journey>> = repository.getJourneys()
}

class UpsertJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journey: Journey) = repository.upsertJourney(journey)
}

class DeleteJourneyUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(id: String) = repository.deleteJourney(id)
}

class ToggleTaskUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, taskId: String) = 
        repository.toggleTask(journeyId, taskId)
}

class CheerTaskUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, taskId: String) = 
        repository.cheerTask(journeyId, taskId)
}

class AddTaskUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, task: app.mymultiverse.kmp.domain.model.JourneyTask) = 
        repository.addTask(journeyId, task)
}

class UpdateTaskUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(task: app.mymultiverse.kmp.domain.model.JourneyTask) = 
        repository.updateTask(task)
}

class DeleteTaskUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, taskId: String) = 
        repository.deleteTask(journeyId, taskId)
}

class AddFinanceBillEntryUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke(journeyId: String, entry: FinanceBillEntry) =
        repository.addFinanceBillEntry(journeyId, entry)
}

class RefreshJourneysUseCase(private val repository: JourneyRepository) {
    suspend operator fun invoke() = repository.refreshJourneys()
}
