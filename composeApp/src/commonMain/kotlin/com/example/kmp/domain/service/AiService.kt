package com.example.kmp.domain.service

import com.example.kmp.domain.model.FinanceProfile
import com.example.kmp.domain.model.HealthWellnessProfile
import com.example.kmp.domain.model.LongTermProjectProfile
import com.example.kmp.domain.model.MealPlanningProfile
import com.example.kmp.domain.model.SmartGoalProposal

interface AiService {
    suspend fun refineDream(seed: String): Result<SmartGoalProposal>
    suspend fun generateWeeklyMealPlan(profile: MealPlanningProfile): Result<SmartGoalProposal>
    suspend fun generateFinancialBlueprint(profile: FinanceProfile): Result<SmartGoalProposal>
    suspend fun generateCouplesWellnessPlan(profile: HealthWellnessProfile): Result<SmartGoalProposal>
    suspend fun generateLongTermProjectBlueprint(profile: LongTermProjectProfile): Result<SmartGoalProposal>
}
