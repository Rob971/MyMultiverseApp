package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnconfiguredHouseholdRepository : HouseholdRepository {
    private val household = MutableStateFlow<Household?>(null)
    private val membershipStatus = MutableStateFlow<HouseholdMembershipStatus>(
        HouseholdMembershipStatus.Error(
            app.mymultiverse.kmp.domain.model.sharing.HouseholdGateError.NotConfigured,
        ),
    )

    override fun observeHousehold(): Flow<Household?> = household.asStateFlow()

    override fun observeMembershipStatus(): Flow<HouseholdMembershipStatus> = membershipStatus.asStateFlow()

    override suspend fun refreshMembership(): Result<HouseholdMembershipStatus> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun createHousehold(name: String): Result<Household> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun ensureHousehold(): Result<Household> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun leaveHousehold(): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))

    override suspend fun dissolveHousehold(): Result<Unit> =
        Result.failure(IllegalStateException("supabase_not_configured"))
}
