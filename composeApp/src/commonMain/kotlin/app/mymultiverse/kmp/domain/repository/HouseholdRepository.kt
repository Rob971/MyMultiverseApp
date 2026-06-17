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

    /** Marks the current user as having left their household (non-owners only). */
    suspend fun leaveHousehold(): Result<Unit>

    /** Hard-deletes the household when the caller is the sole owner with no other members. */
    suspend fun dissolveHousehold(): Result<Unit>

    /** Transfers nutrition household ownership to another active member (owners only). */
    suspend fun transferOwnership(newOwnerUserId: String): Result<Unit>
}
