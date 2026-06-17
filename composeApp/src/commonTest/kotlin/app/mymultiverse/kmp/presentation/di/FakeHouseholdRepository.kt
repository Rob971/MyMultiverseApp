package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembership
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeHouseholdRepository(
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
    private val role: SpaceMemberRole = SpaceMemberRole.Owner,
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
    var lastCreatedName: String? = null

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
            HouseholdMembership(household = created, role = SpaceMemberRole.Owner),
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

    fun setMembershipStatus(status: HouseholdMembershipStatus) {
        membership.update { status }
        when (status) {
            is HouseholdMembershipStatus.Active -> state.update { status.household }
            HouseholdMembershipStatus.None -> state.update { null }
            else -> Unit
        }
    }
}
