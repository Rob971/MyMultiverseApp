package app.mymultiverse.ammo.ui

import app.mymultiverse.ammo.domain.model.sharing.Household
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InstrumentedHouseholdRepository(
    private val household: Household = Household(
        id = "household-1",
        name = "Our household",
        ownerId = "test-user",
        ownerDisplayName = "Test User",
        nutritionFeatures = setOf(
            NutritionSharingFeature.Grocery,
            NutritionSharingFeature.MealPlan,
            NutritionSharingFeature.AiAdvice,
        ),
    ),
    private val role: app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole =
        app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole.Owner,
    private val ensureFailure: Throwable? = null,
    private val refreshFailure: Throwable? = null,
    initialMembershipStatus: HouseholdMembershipStatus? = null,
) : HouseholdRepository {
    private val state = MutableStateFlow<Household?>(
        if (initialMembershipStatus is HouseholdMembershipStatus.Active) household else null,
    )
    private val membership = MutableStateFlow<HouseholdMembershipStatus>(
        initialMembershipStatus ?: HouseholdMembershipStatus.Active(
            app.mymultiverse.ammo.domain.model.sharing.HouseholdMembership(
                household = household,
                role = role,
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

    override suspend fun checkHouseholdNameAvailable(
        name: String,
        excludeHouseholdId: String?,
    ): Result<Boolean> = Result.success(true)

    override suspend fun renameHousehold(newName: String): Result<Household> =
        Result.success(household.copy(name = newName))

    override suspend fun ensureHousehold(): Result<Household> {
        ensureCalls++
        ensureFailure?.let { return Result.failure(it) }
        state.update { household }
        return Result.success(household)
    }

    override suspend fun leaveHousehold(): Result<Unit> = Result.success(Unit)

    override suspend fun dissolveHousehold(): Result<Unit> = Result.success(Unit)

    override suspend fun transferOwnership(newOwnerUserId: String): Result<Unit> = Result.success(Unit)
}
