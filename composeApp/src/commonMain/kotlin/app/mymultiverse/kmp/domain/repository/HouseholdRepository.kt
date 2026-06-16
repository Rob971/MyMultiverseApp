package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.sharing.Household
import kotlinx.coroutines.flow.Flow

interface HouseholdRepository {
    fun observeHousehold(): Flow<Household?>

    suspend fun ensureHousehold(): Result<Household>
}
