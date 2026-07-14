package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.domain.model.sharing.Household
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembership
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.sharing.CollaborationErrorCodes
import app.mymultiverse.ammo.domain.sharing.HouseholdNameRules
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
    var nameAvailabilityResult: Boolean = true
    private val takenNormalizedNames = mutableSetOf<String>().apply {
        add(HouseholdNameRules.normalizeForUniqueness(household.name))
    }

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
        val normalized = HouseholdNameRules.normalizeForUniqueness(name)
        if (normalized in takenNormalizedNames) {
            return Result.failure(IllegalStateException(CollaborationErrorCodes.HOUSEHOLD_NAME_TAKEN))
        }
        val created = household.copy(name = name)
        takenNormalizedNames += normalized
        val active = HouseholdMembershipStatus.Active(
            HouseholdMembership(household = created, role = HouseholdMemberRole.Owner),
        )
        state.update { created }
        membership.update { active }
        return Result.success(created)
    }

    override suspend fun checkHouseholdNameAvailable(
        name: String,
        excludeHouseholdId: String?,
    ): Result<Boolean> {
        if (!nameAvailabilityResult) return Result.success(false)
        val normalized = HouseholdNameRules.normalizeForUniqueness(name)
        val currentId = (membership.value as? HouseholdMembershipStatus.Active)?.household?.id
        val excluded = excludeHouseholdId ?: currentId
        val taken = takenNormalizedNames.contains(normalized) &&
            (excluded == null || state.value?.let { 
                HouseholdNameRules.normalizeForUniqueness(it.name) != normalized 
            } != false)
        return Result.success(!taken && normalized.isNotBlank())
    }

    override suspend fun renameHousehold(newName: String): Result<Household> {
        val current = membership.value as? HouseholdMembershipStatus.Active
            ?: return Result.failure(IllegalStateException("household_required"))
        val oldNormalized = HouseholdNameRules.normalizeForUniqueness(current.household.name)
        val newNormalized = HouseholdNameRules.normalizeForUniqueness(newName)
        if (newNormalized in takenNormalizedNames && newNormalized != oldNormalized) {
            return Result.failure(IllegalStateException(CollaborationErrorCodes.HOUSEHOLD_NAME_TAKEN))
        }
        takenNormalizedNames.remove(oldNormalized)
        takenNormalizedNames += newNormalized
        val updated = current.household.copy(name = newName)
        membership.update {
            HouseholdMembershipStatus.Active(
                HouseholdMembership(household = updated, role = current.role),
            )
        }
        state.update { updated }
        lastCreatedName = newName
        return Result.success(updated)
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

    var updateHouseholdAvatarResult: Result<Unit> = Result.success(Unit)
    var updateHouseholdAvatarCalls: Int = 0
        private set
    var lastHouseholdAvatarUrl: String? = null
        private set

    override suspend fun updateHouseholdAvatar(
        householdId: String,
        imageBytes: ByteArray,
        contentType: String,
    ): Result<Unit> {
        updateHouseholdAvatarCalls++
        if (updateHouseholdAvatarResult.isSuccess) {
            val url = "https://example.com/households/$householdId/avatar.jpg"
            lastHouseholdAvatarUrl = url
            val current = membership.value
            if (current is HouseholdMembershipStatus.Active && current.household.id == householdId) {
                val updated = current.household.copy(avatarUrl = url)
                membership.update {
                    HouseholdMembershipStatus.Active(current.membership.copy(household = updated))
                }
                state.update { updated }
            }
        }
        return updateHouseholdAvatarResult
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
