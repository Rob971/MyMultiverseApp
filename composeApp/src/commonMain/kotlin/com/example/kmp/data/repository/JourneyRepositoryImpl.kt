package com.example.kmp.data.repository

import com.example.kmp.database.AppDatabase
import com.example.kmp.domain.model.FinanceProfile
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.model.JourneyCategory
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.domain.model.MealPlanningProfile
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
                        category = JourneyCategory.fromStorageKey(entity.category),
                        progress = entity.progress.toFloat(),
                        participantInitials = entity.participantInitials.split(","),
                        familyStreak = entity.familyStreak.toInt(),
                        specificGoal = entity.specificGoal,
                        measurableOutcome = entity.measurableOutcome,
                        achievablePlan = entity.achievablePlan,
                        relevanceToFamily = entity.relevanceToFamily,
                        timeBoundDeadline = entity.timeBoundDeadline,
                        colorHex = entity.colorHex,
                        mealPlanningProfile = MealPlanningProfile(
                            cookingFor = entity.mealCookingFor.orEmpty(),
                            dietaryRestrictions = entity.mealDietaryRestrictions.toListValue(),
                            dislikedIngredients = entity.mealDislikedIngredients.orEmpty(),
                            busyWeeknightCookTime = entity.mealBusyWeeknightCookTime.orEmpty(),
                            cookingSkillLevel = entity.mealCookingSkillLevel.orEmpty(),
                            lunchPreference = entity.mealLunchPreference.orEmpty(),
                            rightNowGoal = entity.mealRightNowGoal.orEmpty(),
                            locationPreference = entity.mealLocationPreference.orEmpty(),
                            manualLocation = entity.mealManualLocation.orEmpty()
                        ).takeIf { it.hasAnswers },
                        financeProfile = FinanceProfile(
                            financeSplit = entity.financeSplit.orEmpty(),
                            billManager = entity.financeBillManager.orEmpty(),
                            dailyAnnoyance = entity.financeDailyAnnoyance.orEmpty(),
                            partnerASpendingStyle = entity.financePartnerASpendingStyle.orEmpty(),
                            partnerBSpendingStyle = entity.financePartnerBSpendingStyle.orEmpty(),
                            moneyTalkFrequency = entity.financeMoneyTalkFrequency.orEmpty(),
                            primaryGoal = entity.financePrimaryGoal.orEmpty(),
                            irregularExpensePlan = entity.financeIrregularExpensePlan.orEmpty(),
                            billSplitStrategy = entity.financeBillSplitStrategy.orEmpty(),
                            settleWorkflow = entity.financeSettleWorkflow.orEmpty(),
                            recurringBills = entity.financeRecurringBills.toListValue(),
                            billPainPoint = entity.financeBillPainPoint.orEmpty(),
                            partnerAIncome = entity.financePartnerAIncome.orEmpty(),
                            partnerBIncome = entity.financePartnerBIncome.orEmpty(),
                            customSplitPercentages = entity.financeCustomSplitPercentages.orEmpty(),
                            monthlyHousingSpend = entity.financeMonthlyHousingSpend.orEmpty(),
                            monthlyUtilitiesSpend = entity.financeMonthlyUtilitiesSpend.orEmpty(),
                            monthlyConnectivitySpend = entity.financeMonthlyConnectivitySpend.orEmpty(),
                            monthlySubscriptionsSpend = entity.financeMonthlySubscriptionsSpend.orEmpty(),
                            monthlyInsuranceSpend = entity.financeMonthlyInsuranceSpend.orEmpty(),
                            monthlyKidsPetsSpend = entity.financeMonthlyKidsPetsSpend.orEmpty(),
                            monthlyOtherSpend = entity.financeMonthlyOtherSpend.orEmpty(),
                        ).takeIf { it.hasAnswers },
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
            category = journey.category.storageKey,
            progress = journey.progress.toDouble(),
            participantInitials = journey.participantInitials.joinToString(","),
            familyStreak = journey.familyStreak.toLong(),
            specificGoal = journey.specificGoal,
            measurableOutcome = journey.measurableOutcome,
            achievablePlan = journey.achievablePlan,
            relevanceToFamily = journey.relevanceToFamily,
            timeBoundDeadline = journey.timeBoundDeadline,
            colorHex = journey.colorHex,
            mealCookingFor = journey.mealPlanningProfile?.cookingFor,
            mealDietaryRestrictions = journey.mealPlanningProfile?.dietaryRestrictions?.joinToString(","),
            mealDislikedIngredients = journey.mealPlanningProfile?.dislikedIngredients,
            mealBusyWeeknightCookTime = journey.mealPlanningProfile?.busyWeeknightCookTime,
            mealCookingSkillLevel = journey.mealPlanningProfile?.cookingSkillLevel,
            mealLunchPreference = journey.mealPlanningProfile?.lunchPreference,
            mealRightNowGoal = journey.mealPlanningProfile?.rightNowGoal,
            mealLocationPreference = journey.mealPlanningProfile?.locationPreference,
            mealManualLocation = journey.mealPlanningProfile?.manualLocation,
            financeSplit = journey.financeProfile?.financeSplit,
            financeBillManager = journey.financeProfile?.billManager,
            financeDailyAnnoyance = journey.financeProfile?.dailyAnnoyance,
            financePartnerASpendingStyle = journey.financeProfile?.partnerASpendingStyle,
            financePartnerBSpendingStyle = journey.financeProfile?.partnerBSpendingStyle,
            financeMoneyTalkFrequency = journey.financeProfile?.moneyTalkFrequency,
            financePrimaryGoal = journey.financeProfile?.primaryGoal,
            financeIrregularExpensePlan = journey.financeProfile?.irregularExpensePlan,
            financeBillSplitStrategy = journey.financeProfile?.billSplitStrategy,
            financeSettleWorkflow = journey.financeProfile?.settleWorkflow,
            financeRecurringBills = journey.financeProfile?.recurringBills?.joinToString(","),
            financeBillPainPoint = journey.financeProfile?.billPainPoint,
            financePartnerAIncome = journey.financeProfile?.partnerAIncome,
            financePartnerBIncome = journey.financeProfile?.partnerBIncome,
            financeCustomSplitPercentages = journey.financeProfile?.customSplitPercentages,
            financeMonthlyHousingSpend = journey.financeProfile?.monthlyHousingSpend,
            financeMonthlyUtilitiesSpend = journey.financeProfile?.monthlyUtilitiesSpend,
            financeMonthlyConnectivitySpend = journey.financeProfile?.monthlyConnectivitySpend,
            financeMonthlySubscriptionsSpend = journey.financeProfile?.monthlySubscriptionsSpend,
            financeMonthlyInsuranceSpend = journey.financeProfile?.monthlyInsuranceSpend,
            financeMonthlyKidsPetsSpend = journey.financeProfile?.monthlyKidsPetsSpend,
            financeMonthlyOtherSpend = journey.financeProfile?.monthlyOtherSpend
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
        // Journeys are created by users and loaded from persisted storage.
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
            category = j.category,
            progress = progress.toDouble(),
            participantInitials = j.participantInitials,
            familyStreak = j.familyStreak,
            specificGoal = j.specificGoal,
            measurableOutcome = j.measurableOutcome,
            achievablePlan = j.achievablePlan,
            relevanceToFamily = j.relevanceToFamily,
            timeBoundDeadline = j.timeBoundDeadline,
            colorHex = j.colorHex,
            mealCookingFor = j.mealCookingFor,
            mealDietaryRestrictions = j.mealDietaryRestrictions,
            mealDislikedIngredients = j.mealDislikedIngredients,
            mealBusyWeeknightCookTime = j.mealBusyWeeknightCookTime,
            mealCookingSkillLevel = j.mealCookingSkillLevel,
            mealLunchPreference = j.mealLunchPreference,
            mealRightNowGoal = j.mealRightNowGoal,
            mealLocationPreference = j.mealLocationPreference,
            mealManualLocation = j.mealManualLocation,
            financeSplit = j.financeSplit,
            financeBillManager = j.financeBillManager,
            financeDailyAnnoyance = j.financeDailyAnnoyance,
            financePartnerASpendingStyle = j.financePartnerASpendingStyle,
            financePartnerBSpendingStyle = j.financePartnerBSpendingStyle,
            financeMoneyTalkFrequency = j.financeMoneyTalkFrequency,
            financePrimaryGoal = j.financePrimaryGoal,
            financeIrregularExpensePlan = j.financeIrregularExpensePlan,
            financeBillSplitStrategy = j.financeBillSplitStrategy,
            financeSettleWorkflow = j.financeSettleWorkflow,
            financeRecurringBills = j.financeRecurringBills,
            financeBillPainPoint = j.financeBillPainPoint,
            financePartnerAIncome = j.financePartnerAIncome,
            financePartnerBIncome = j.financePartnerBIncome,
            financeCustomSplitPercentages = j.financeCustomSplitPercentages,
            financeMonthlyHousingSpend = j.financeMonthlyHousingSpend,
            financeMonthlyUtilitiesSpend = j.financeMonthlyUtilitiesSpend,
            financeMonthlyConnectivitySpend = j.financeMonthlyConnectivitySpend,
            financeMonthlySubscriptionsSpend = j.financeMonthlySubscriptionsSpend,
            financeMonthlyInsuranceSpend = j.financeMonthlyInsuranceSpend,
            financeMonthlyKidsPetsSpend = j.financeMonthlyKidsPetsSpend,
            financeMonthlyOtherSpend = j.financeMonthlyOtherSpend
        )
    }
}

private fun String?.toListValue(): List<String> {
    return orEmpty()
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
