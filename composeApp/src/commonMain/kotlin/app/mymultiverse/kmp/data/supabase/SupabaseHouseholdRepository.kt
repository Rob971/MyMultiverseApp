package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.supabase.dto.HouseholdRpcRow
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SupabaseHouseholdRepository(
    private val client: SupabaseClient,
) : HouseholdRepository {

    private val household = MutableStateFlow<Household?>(null)

    override fun observeHousehold(): Flow<Household?> = household.asStateFlow()

    override suspend fun ensureHousehold(): Result<Household> = runCatching {
        client.auth.awaitInitialization()
        requireUserId()

        val row = client.postgrest
            .rpc("ensure_household")
            .decodeSingle<HouseholdRpcRow>()

        row.toDomain().also { resolved ->
            household.update { resolved }
        }
    }

    private suspend fun requireUserId(): String =
        client.auth.currentUserOrNull()?.id ?: throw IllegalStateException("auth_required")

    private fun HouseholdRpcRow.toDomain(): Household =
        Household(
            id = spaceId,
            name = spaceName,
            ownerId = ownerId,
            ownerDisplayName = ownerDisplayName?.takeIf { it.isNotBlank() },
            nutritionFeatures = features.mapNotNull { it.toNutritionFeature() }.toSet(),
        )

    private fun String.toNutritionFeature(): NutritionSharingFeature? =
        when (this) {
            "grocery" -> NutritionSharingFeature.Grocery
            "meal_plan" -> NutritionSharingFeature.MealPlan
            "ai_advice" -> NutritionSharingFeature.AiAdvice
            else -> null
        }
}
