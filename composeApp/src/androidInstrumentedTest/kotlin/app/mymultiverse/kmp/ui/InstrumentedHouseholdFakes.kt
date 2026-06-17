package app.mymultiverse.kmp.ui

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InstrumentedHouseholdRepository(
    private val household: Household = Household(
        id = "household-space-1",
        name = "Our household",
        ownerId = "test-user",
        ownerDisplayName = "Test User",
        nutritionFeatures = setOf(
            NutritionSharingFeature.Grocery,
            NutritionSharingFeature.MealPlan,
            NutritionSharingFeature.AiAdvice,
        ),
    ),
    private val ensureFailure: Throwable? = null,
    private val refreshFailure: Throwable? = null,
) : HouseholdRepository {
    private val state = MutableStateFlow<Household?>(household)
    private val membership = MutableStateFlow<HouseholdMembershipStatus>(
        HouseholdMembershipStatus.Active(
            app.mymultiverse.kmp.domain.model.sharing.HouseholdMembership(
                household = household,
                role = app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole.Owner,
            ),
        ),
    )
    var ensureCalls = 0
    var refreshCalls = 0

    override fun observeHousehold(): Flow<Household?> = state.asStateFlow()

    override fun observeMembershipStatus(): Flow<HouseholdMembershipStatus> = membership.asStateFlow()

    override suspend fun refreshMembership(): Result<HouseholdMembershipStatus> {
        refreshCalls++
        refreshFailure?.let { return Result.failure(it) }
        return Result.success(membership.value)
    }

    override suspend fun createHousehold(name: String): Result<Household> =
        Result.success(household.copy(name = name))

    override suspend fun ensureHousehold(): Result<Household> {
        ensureCalls++
        ensureFailure?.let { return Result.failure(it) }
        state.update { household }
        return Result.success(household)
    }

    override suspend fun leaveHousehold(): Result<Unit> = Result.success(Unit)

    override suspend fun dissolveHousehold(): Result<Unit> = Result.success(Unit)
}
