package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembership
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeHouseholdRepository(
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
    private val role: HouseholdMemberRole = HouseholdMemberRole.Owner,
    private val initialMembershipStatus: HouseholdMembershipStatus? = null,
    private val ensureFailure: Throwable? = null,
    private val refreshFailure: Throwable? = null,
    private val createFailure: Throwable? = null,
) : HouseholdRepository {
    private val state = MutableStateFlow<Household?>(household)
    private val membership = MutableStateFlow(
        initialMembershipStatus ?: if (household != null) {
            HouseholdMembershipStatus.Active(
                HouseholdMembership(household = household, role = role),
            )
        } else {
            HouseholdMembershipStatus.None
        },
    )
    var ensureCalls = 0
    var refreshCalls = 0
    var createCalls = 0
    var leaveCalls = 0
    var dissolveCalls = 0
    var transferCalls = 0
    var lastTransferTargetId: String? = null
    var lastCreatedName: String? = null
    var leaveFailure: Throwable? = null
    var dissolveFailure: Throwable? = null

    override fun observeHousehold(): Flow<Household?> = state.asStateFlow()

    override fun observeMembershipStatus(): Flow<HouseholdMembershipStatus> = membership.asStateFlow()

    override suspend fun refreshMembership(): Result<HouseholdMembershipStatus> {
        refreshCalls++
        refreshFailure?.let { return Result.failure(it) }
        val current = membership.value
        if (current is HouseholdMembershipStatus.Active) {
            state.update { current.household }
        }
        return Result.success(current)
    }

    override suspend fun createHousehold(name: String): Result<Household> {
        createCalls++
        lastCreatedName = name
        createFailure?.let { return Result.failure(it) }
        val created = household.copy(name = name)
        val active = HouseholdMembershipStatus.Active(
            HouseholdMembership(household = created, role = HouseholdMemberRole.Owner),
        )
        state.update { created }
        membership.update { active }
        return Result.success(created)
    }

    override suspend fun ensureHousehold(): Result<Household> {
        ensureCalls++
        ensureFailure?.let { return Result.failure(it) }
        state.update { household }
        return Result.success(household)
    }

    override suspend fun leaveHousehold(): Result<Unit> {
        leaveCalls++
        leaveFailure?.let { return Result.failure(it) }
        membership.update { HouseholdMembershipStatus.None }
        state.update { null }
        return Result.success(Unit)
    }

    override suspend fun dissolveHousehold(): Result<Unit> {
        dissolveCalls++
        dissolveFailure?.let { return Result.failure(it) }
        membership.update { HouseholdMembershipStatus.None }
        state.update { null }
        return Result.success(Unit)
    }

    override suspend fun transferOwnership(newOwnerUserId: String): Result<Unit> {
        transferCalls++
        lastTransferTargetId = newOwnerUserId
        val current = membership.value
        if (current is HouseholdMembershipStatus.Active) {
            val updated = current.household.copy(ownerId = newOwnerUserId)
            membership.update {
                HouseholdMembershipStatus.Active(
                    HouseholdMembership(household = updated, role = HouseholdMemberRole.Editor),
                )
            }
            state.update { updated }
        }
        return Result.success(Unit)
    }

    fun setMembershipStatus(status: HouseholdMembershipStatus) {
        membership.update { status }
        when (status) {
            is HouseholdMembershipStatus.Active -> state.update { status.household }
            HouseholdMembershipStatus.None -> state.update { null }
            else -> Unit
        }
    }
}
