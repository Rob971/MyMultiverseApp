package com.example.kmp.data.repository

import com.example.kmp.database.AppDatabase
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.domain.repository.JourneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class JourneyRepositoryImpl(
    private val database: AppDatabase
) : JourneyRepository {

    private val queries = database.appDatabaseQueries

    override fun getJourneys(): Flow<List<Journey>> {
        return queries.selectAllJourneys()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { journeyEntities ->
                journeyEntities.map { entity ->
                    val tasks = queries.selectTasksByJourneyId(entity.id).executeAsList().map { taskEntity ->
                        JourneyTask(
                            id = taskEntity.id,
                            journeyId = taskEntity.journeyId,
                            title = taskEntity.title,
                            planning = taskEntity.planning,
                            isCompleted = taskEntity.isCompleted == 1L,
                            label = taskEntity.label,
                            scheduledDays = taskEntity.scheduledDays.split(",").filter { it.isNotEmpty() }.map { it.toInt() },
                            reminderTime = taskEntity.reminderTime,
                            claimedByInitials = taskEntity.claimedByInitials,
                            cheersCount = taskEntity.cheersCount.toInt()
                        )
                    }
                    Journey(
                        id = entity.id,
                        title = entity.title,
                        subtitle = entity.subtitle,
                        progress = entity.progress.toFloat(),
                        participantInitials = entity.participantInitials.split(","),
                        familyStreak = entity.familyStreak.toInt(),
                        specificGoal = entity.specificGoal,
                        measurableOutcome = entity.measurableOutcome,
                        achievablePlan = entity.achievablePlan,
                        relevanceToFamily = entity.relevanceToFamily,
                        timeBoundDeadline = entity.timeBoundDeadline,
                        colorHex = entity.colorHex,
                        tasks = tasks
                    )
                }
            }
    }

    override suspend fun toggleTask(journeyId: String, taskId: String) {
        val task = queries.selectAllTasks().executeAsList().find { it.id == taskId } ?: return
        val newStatus = if (task.isCompleted == 1L) 0L else 1L
        queries.updateTaskCompletion(newStatus, taskId)
        updateJourneyProgress(journeyId)
    }

    override suspend fun cheerTask(journeyId: String, taskId: String) {
        queries.updateTaskCheers(taskId)
    }

    override suspend fun claimTask(journeyId: String, taskId: String, initials: String) {
        queries.updateTaskClaim(initials, taskId)
    }

    override suspend fun refreshJourneys() {
        if (queries.selectAllJourneys().executeAsList().isEmpty()) {
            seedDatabase()
        }
    }

    private fun updateJourneyProgress(journeyId: String) {
        val tasks = queries.selectTasksByJourneyId(journeyId).executeAsList()
        val completedCount = tasks.count { it.isCompleted == 1L }
        val progress = if (tasks.isEmpty()) 0f else completedCount.toFloat() / tasks.size
        val j = queries.selectAllJourneys().executeAsList().find { it.id == journeyId } ?: return
        queries.insertJourney(
            id = j.id,
            title = j.title,
            subtitle = j.subtitle,
            progress = progress.toDouble(),
            participantInitials = j.participantInitials,
            familyStreak = j.familyStreak,
            specificGoal = j.specificGoal,
            measurableOutcome = j.measurableOutcome,
            achievablePlan = j.achievablePlan,
            relevanceToFamily = j.relevanceToFamily,
            timeBoundDeadline = j.timeBoundDeadline,
            colorHex = j.colorHex
        )
    }

    private fun seedDatabase() {
        queries.insertJourney(
            "vesuvian-vitality", "Vesuvian Vitality", "Salvatore & Anna's Healthy Plan", 0.35, "S,A", 12,
            "Transform daily nutrition...", "5 core habits...", "Gradual changes...", "Salute e Benessere...", "90-Day Phase", "E2725B"
        )
        queries.insertTask("v-1", "vesuvian-vitality", "La Spesa", "Visit Mercato...", 1, "Weekly", "6", "09:00", "S", 5)
        queries.insertTask("v-2", "vesuvian-vitality", "Daily Ratios", "Ensure half plate...", 0, "Daily", "1,2,3,4,5,6,7", "19:00", "A", 2)
        queries.insertTask("v-3", "vesuvian-vitality", "Dolce Vita", "Weekly Pizza...", 1, "Mixed", "5,7", "20:30", null, 12)
        queries.insertTask("v-4", "vesuvian-vitality", "Porticese Lifestyle", "Daily sunset walk...", 0, "Daily", "1,2,3,4,5,6,7", "18:00", "S", 4)
        queries.insertTask("v-5", "vesuvian-vitality", "Logistics & Prep", "Sunday batch-cooking...", 0, "Weekly", "7", "10:30", null, 1)

        queries.insertJourney(
            "financial-masterplan", "Financial Masterplan", "Costruiamo il Nostro Futuro", 0.20, "S,A", 5,
            "Secure $10k down payment...", "Monthly reviews...", "50/50 split...", "Transparency...", "Annual Roadmap", "C9A66B"
        )
        queries.insertTask("f-1", "financial-masterplan", "Monthly Check-in", "Define goals...", 1, "Monthly", "1", "18:00", "S", 3)
        queries.insertTask("f-2", "financial-masterplan", "Log as You Go", "Categorize spending...", 0, "Daily", "1,2,3,4,5,6,7", "21:00", "A", 8)
        queries.insertTask("f-3", "financial-masterplan", "Secure Transfer", "Execute transfer...", 0, "Monthly", "5", "10:00", null, 15)
        queries.insertTask("f-4", "financial-masterplan", "Sinking Funds", "Review funds...", 1, "Weekly", "7", "11:00", null, 2)
        queries.insertTask("f-5", "financial-masterplan", "Transparency Talk", "Weekly talk...", 0, "Weekly", "4", "20:00", "S", 6)

        queries.insertJourney(
            "motore-unita", "Il Motore dell'Unità", "Maria & Antonio's Dolce Vita Sana", 0.50, "M,A", 8,
            "Build strength...", "Quarterly shifts...", "80% Rule...", "Shared movement...", "2025 Seasonal", "4F7942"
        )
        queries.insertTask("m-1", "motore-unita", "Primavera: Forza", "3x functional strength...", 1, "Weekly", "1,3,5", "07:30", "A", 12)
        queries.insertTask("m-2", "motore-unita", "Estate: Energia", "Open water swimming...", 0, "Daily", "2,4,6,7", "08:00", null, 20)
        queries.insertTask("m-3", "motore-unita", "Autunno: Cuore", "Weekend hiking...", 0, "Weekly", "6,7", "09:00", "M", 8)
        queries.insertTask("m-4", "motore-unita", "Inverno: Equilibrio", "Fresh market visits...", 1, "Daily", "1,2,3,4,5,6,7", "08:30", null, 4)
        queries.insertTask("m-5", "motore-unita", "Emulazione Supporto", "Daily check-in...", 1, "Daily", "1,2,3,4,5,6,7", "21:30", "A", 15)
    }
}
