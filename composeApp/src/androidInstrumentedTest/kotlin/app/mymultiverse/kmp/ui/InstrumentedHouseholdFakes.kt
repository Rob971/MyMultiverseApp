package app.mymultiverse.kmp.ui

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
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
) : HouseholdRepository {
    private val state = MutableStateFlow<Household?>(household)
    var ensureCalls = 0

    override fun observeHousehold(): Flow<Household?> = state.asStateFlow()

    override suspend fun ensureHousehold(): Result<Household> {
        ensureCalls++
        ensureFailure?.let { return Result.failure(it) }
        state.update { household }
        return Result.success(household)
    }
}

class InstrumentedNutritionSpaceSelectionStore : NutritionSpaceSelectionStore {
    val activeSpaceId = MutableStateFlow<String?>(null)

    override fun observeActiveSpaceId(): Flow<String?> = activeSpaceId.asStateFlow()

    override suspend fun setActiveSpaceId(spaceId: String) {
        activeSpaceId.value = spaceId
    }

    override suspend fun clearActiveSpaceId() {
        activeSpaceId.value = null
    }
}
