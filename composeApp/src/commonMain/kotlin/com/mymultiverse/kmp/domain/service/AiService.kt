package com.mymultiverse.kmp.domain.service

import com.mymultiverse.kmp.domain.model.FinanceProfile
import com.mymultiverse.kmp.domain.model.HealthWellnessProfile
import com.mymultiverse.kmp.domain.model.LongTermProjectProfile
import com.mymultiverse.kmp.domain.model.MealPlanningProfile
import com.mymultiverse.kmp.domain.model.SmartGoalProposal

interface AiService {
    suspend fun refineDream(seed: String): Result<SmartGoalProposal>
    suspend fun generateWeeklyMealPlan(profile: MealPlanningProfile): Result<SmartGoalProposal>
    suspend fun generateFinancialBlueprint(profile: FinanceProfile): Result<SmartGoalProposal>
    suspend fun generateCouplesWellnessPlan(profile: HealthWellnessProfile): Result<SmartGoalProposal>
    suspend fun generateLongTermProjectBlueprint(profile: LongTermProjectProfile): Result<SmartGoalProposal>
}
