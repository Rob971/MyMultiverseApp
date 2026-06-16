package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnconfiguredHouseholdRepository : HouseholdRepository {
    private val household = MutableStateFlow<Household?>(null)

    override fun observeHousehold(): Flow<Household?> = household.asStateFlow()

    override suspend fun ensureHousehold(): Result<Household> =
        Result.failure(IllegalStateException("supabase_not_configured"))
}
