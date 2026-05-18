package app.mymultiverse.kmp.domain.service

import app.mymultiverse.kmp.domain.model.FinanceProfile
import app.mymultiverse.kmp.domain.model.HealthWellnessProfile
import app.mymultiverse.kmp.domain.model.LongTermProjectProfile
import app.mymultiverse.kmp.domain.model.MealPlanningProfile
import app.mymultiverse.kmp.domain.model.SmartGoalProposal

interface AiService {
    suspend fun refineDream(seed: String): Result<SmartGoalProposal>
    suspend fun generateWeeklyMealPlan(profile: MealPlanningProfile): Result<SmartGoalProposal>
    suspend fun generateFinancialBlueprint(profile: FinanceProfile): Result<SmartGoalProposal>
    suspend fun generateCouplesWellnessPlan(profile: HealthWellnessProfile): Result<SmartGoalProposal>
    suspend fun generateLongTermProjectBlueprint(profile: LongTermProjectProfile): Result<SmartGoalProposal>
}
