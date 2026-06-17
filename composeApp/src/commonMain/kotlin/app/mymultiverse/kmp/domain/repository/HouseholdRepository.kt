package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import kotlinx.coroutines.flow.Flow

interface HouseholdRepository {
    fun observeHousehold(): Flow<Household?>

    fun observeMembershipStatus(): Flow<HouseholdMembershipStatus>

    suspend fun refreshMembership(): Result<HouseholdMembershipStatus>

    suspend fun createHousehold(name: String): Result<Household>

    /** Resolves the active household; fails with [household_required] when none exists. */
    suspend fun ensureHousehold(): Result<Household>
}
