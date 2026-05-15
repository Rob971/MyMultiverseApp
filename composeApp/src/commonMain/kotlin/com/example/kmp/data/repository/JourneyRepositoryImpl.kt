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

    override suspend fun upsertJourney(journey: Journey) {
        queries.insertJourney(
            id = journey.id,
            title = journey.title,
            subtitle = journey.subtitle,
            progress = journey.progress.toDouble(),
            participantInitials = journey.participantInitials.joinToString(","),
            familyStreak = journey.familyStreak.toLong(),
            specificGoal = journey.specificGoal,
            measurableOutcome = journey.measurableOutcome,
            achievablePlan = journey.achievablePlan,
            relevanceToFamily = journey.relevanceToFamily,
            timeBoundDeadline = journey.timeBoundDeadline,
            colorHex = journey.colorHex
        )
        // Also insert/update tasks
        journey.tasks.forEach { task ->
            queries.insertTask(
                id = task.id,
                journeyId = task.journeyId,
                title = task.title,
                planning = task.planning,
                isCompleted = if (task.isCompleted) 1L else 0L,
                label = task.label,
                scheduledDays = task.scheduledDays.joinToString(","),
                reminderTime = task.reminderTime,
                claimedByInitials = task.claimedByInitials,
                cheersCount = task.cheersCount.toLong()
            )
        }
    }

    override suspend fun deleteJourney(id: String) {
        queries.deleteJourney(id)
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

    override suspend fun addTask(journeyId: String, task: JourneyTask) {
        queries.insertTask(
            id = task.id,
            journeyId = journeyId,
            title = task.title,
            planning = task.planning,
            isCompleted = if (task.isCompleted) 1L else 0L,
            label = task.label,
            scheduledDays = task.scheduledDays.joinToString(","),
            reminderTime = task.reminderTime,
            claimedByInitials = task.claimedByInitials,
            cheersCount = task.cheersCount.toLong()
        )
        updateJourneyProgress(journeyId)
    }

    override suspend fun updateTask(task: JourneyTask) {
        queries.insertTask(
            id = task.id,
            journeyId = task.journeyId,
            title = task.title,
            planning = task.planning,
            isCompleted = if (task.isCompleted) 1L else 0L,
            label = task.label,
            scheduledDays = task.scheduledDays.joinToString(","),
            reminderTime = task.reminderTime,
            claimedByInitials = task.claimedByInitials,
            cheersCount = task.cheersCount.toLong()
        )
        updateJourneyProgress(task.journeyId)
    }

    override suspend fun deleteTask(journeyId: String, taskId: String) {
        queries.deleteTask(taskId)
        updateJourneyProgress(journeyId)
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
        // Vesuvian Vitality - TerracottaOrange (E2725B)
        queries.insertJourney(
            "vesuvian-vitality", "Vesuvian Vitality", "Piano Nutrizionale Salvatore & Anna", 0.35, "S,A", 12,
            "Trasforma la nutrizione quotidiana usando il framework 'Italia Portici'.", 
            "5 abitudini core tracciate settimanalmente.", 
            "Cambiamenti graduali: espresso senza zucchero in 7 giorni.", 
            "Garantire salute e benessere a lungo termine per la nostra famiglia.", 
            "Fase iniziale di 90 giorni", "E2725B"
        )
        queries.insertTask("v-1", "vesuvian-vitality", "La Spesa", "Mercato di Portici ogni sabato mattina.", 1, "Weekly", "6", "09:00", "S", 24)
        queries.insertTask("v-2", "vesuvian-vitality", "Daily Ratios", "50% verdure, 25% proteine, 25% carboidrati.", 0, "Daily", "1,2,3,4,5,6,7", "19:00", "A", 2)
        queries.insertTask("v-3", "vesuvian-vitality", "Dolce Vita", "Pizza Marinara settimanale. Sfogliatella la domenica.", 1, "Mixed", "5,7", "20:30", null, 12)
        queries.insertTask("v-4", "vesuvian-vitality", "Porticese Lifestyle", "Passeggiata al tramonto alla Reggia di Portici.", 0, "Daily", "1,2,3,4,5,6,7", "18:00", "S", 4)
        queries.insertTask("v-5", "vesuvian-vitality", "Logistics & Prep", "Batch-cooking di legumi e pasta integrale.", 0, "Weekly", "7", "10:30", null, 1)

        // Financial Masterplan - LemonZestYellow (F4D03F)
        queries.insertJourney(
            "financial-masterplan", "Financial Masterplan", "Costruiamo il Nostro Futuro Qui (Napoli)", 0.20, "S,A", 5,
            "Assicurare 10k per il deposito casa e gestire le categorie di spesa core.", 
            "Revisioni mensili e tracciamento 'log-as-you-go'.", 
            "Modello 50/50 o percentuale proporzionale al reddito.", 
            "Trasparenza, benessere familiare e decisioni basate sui valori.", 
            "Roadmap annuale con date assegnate", "F4D03F"
        )
        queries.insertTask("f-1", "financial-masterplan", "Monthly Check-in", "Definiamo gli obiettivi per i prossimi 30 giorni.", 1, "Monthly", "1", "18:00", "S", 34)
        queries.insertTask("f-2", "financial-masterplan", "Log as You Go", "Categorizza le spese quotidiane (Cibo, Utenze, Attività).", 0, "Daily", "1,2,3,4,5,6,7", "21:00", "A", 8)
        queries.insertTask("f-3", "financial-masterplan", "Secure Transfer", "Trasferimento mensile al fondo deposito casa.", 0, "Monthly", "5", "10:00", null, 15)
        queries.insertTask("f-4", "financial-masterplan", "Sinking Funds", "Revisione fondi: Vacanze, Regali, Assicurazioni.", 1, "Weekly", "7", "11:00", null, 2)
        queries.insertTask("f-5", "financial-masterplan", "Transparency Talk", "Discussione settimanale per fiducia e trasparenza.", 0, "Weekly", "4", "20:00", "S", 6)

        // Il Motore dell'Unità - MediterraneanTeal (005F6B)
        queries.insertJourney(
            "motore-unita", "Il Motore dell'Unità", "Forza e Salute: Maria & Antonio", 0.50, "M,A", 8,
            "Costruire forza funzionale e salute del cuore tramite attività stagionali.", 
            "Obiettivi trimestrali (Forza, Mare, Resistenza, Equilibrio).", 
            "Regola dell'80% (va bene!): costanza sulla perfezione.", 
            "Rafforzare l'unione tramite il movimento condiviso.", 
            "Roadmap Stagionale 2025 (Target Kayak Gaiola)", "005F6B"
        )
        queries.insertTask("m-1", "motore-unita", "Primavera: Forza", "3x forza funzionale in palestra o Crossfit.", 1, "Weekly", "1,3,5", "07:30", "A", 59)
        queries.insertTask("m-2", "motore-unita", "Estate: Energia", "Nuoto a Posillipo e corsa sul Lungomare.", 0, "Daily", "2,4,6,7", "08:00", null, 20)
        queries.insertTask("m-3", "motore-unita", "Autunno: Cuore", "Hiking sul Vesuvio e Sentiero degli Dei.", 0, "Weekly", "6,7", "09:00", "M", 8)
        queries.insertTask("m-4", "motore-unita", "Inverno: Equilibrio", "Mercati freschi e colazioni culturali.", 1, "Daily", "1,2,3,4,5,6,7", "08:30", null, 4)
        queries.insertTask("m-5", "motore-unita", "Emulazione Supporto", "Check-in quotidiano di supporto reciproco.", 1, "Daily", "1,2,3,4,5,6,7", "21:30", "A", 15)
    }
}
