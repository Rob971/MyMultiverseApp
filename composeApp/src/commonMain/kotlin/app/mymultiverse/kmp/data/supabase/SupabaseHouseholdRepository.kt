package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.supabase.dto.HouseholdMembershipRpcRow
import app.mymultiverse.kmp.data.supabase.dto.HouseholdRpcDecoder
import app.mymultiverse.kmp.data.supabase.dto.HouseholdRpcRow
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.HouseholdGateError
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembership
import app.mymultiverse.kmp.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SpaceMemberRole
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SupabaseHouseholdRepository(
    private val client: SupabaseClient,
) : HouseholdRepository {

    private val household = MutableStateFlow<Household?>(null)
    private val membershipStatus = MutableStateFlow<HouseholdMembershipStatus>(HouseholdMembershipStatus.Loading)

    override fun observeHousehold(): Flow<Household?> = household.asStateFlow()

    override fun observeMembershipStatus(): Flow<HouseholdMembershipStatus> = membershipStatus.asStateFlow()

    override suspend fun refreshMembership(): Result<HouseholdMembershipStatus> = runCatching {
        client.auth.awaitInitialization()
        requireUserId()

        val row = HouseholdRpcDecoder.decodeMembership(
            client.postgrest.rpc("household_membership_status"),
        )

        row.toMembershipStatus().also { resolved ->
            membershipStatus.update { resolved }
            when (resolved) {
                is HouseholdMembershipStatus.Active -> household.update { resolved.household }
                HouseholdMembershipStatus.None -> household.update { null }
                else -> Unit
            }
        }
    }

    override suspend fun createHousehold(name: String): Result<Household> = runCatching {
        client.auth.awaitInitialization()
        requireUserId()

        val row = HouseholdRpcDecoder.decodeMembership(
            client.postgrest.rpc(
                "create_household",
                buildJsonObject { put("p_name", name.trim()) },
            ),
        )

        val status = row.toMembershipStatus()
        when (status) {
            is HouseholdMembershipStatus.Active -> {
                membershipStatus.update { status }
                household.update { status.household }
                status.household
            }
            else -> throw IllegalStateException("create_household_unexpected_status")
        }
    }

    override suspend fun ensureHousehold(): Result<Household> = runCatching {
        client.auth.awaitInitialization()
        requireUserId()

        val row = HouseholdRpcDecoder.decode(
            client.postgrest.rpc("ensure_household"),
        )

        row.toDomain().also { resolved ->
            household.update { resolved }
        }
    }

    override suspend fun leaveHousehold(): Result<Unit> = runCatching {
        client.auth.awaitInitialization()
        requireUserId()
        client.postgrest.rpc("leave_household")
        membershipStatus.update { HouseholdMembershipStatus.None }
        household.update { null }
    }

    override suspend fun dissolveHousehold(): Result<Unit> = runCatching {
        client.auth.awaitInitialization()
        requireUserId()
        client.postgrest.rpc("dissolve_household")
        membershipStatus.update { HouseholdMembershipStatus.None }
        household.update { null }
    }

    override suspend fun transferOwnership(newOwnerUserId: String): Result<Unit> = runCatching {
        client.auth.awaitInitialization()
        requireUserId()
        client.postgrest.rpc(
            "transfer_household_ownership",
            buildJsonObject { put("p_new_owner_user_id", newOwnerUserId) },
        )
        refreshMembership()
    }

    private suspend fun requireUserId(): String =
        client.auth.currentUserOrNull()?.id
            ?: client.auth.currentSessionOrNull()?.user?.id
            ?: throw IllegalStateException("auth_required")

    private fun HouseholdMembershipRpcRow.toMembershipStatus(): HouseholdMembershipStatus =
        when (status) {
            "active" -> {
                val resolvedHousehold = toHousehold()
                    ?: return HouseholdMembershipStatus.Error(HouseholdGateError.Generic)
                HouseholdMembershipStatus.Active(
                    membership = HouseholdMembership(
                        household = resolvedHousehold,
                        role = role.toSpaceMemberRole(),
                    ),
                )
            }
            "none" -> HouseholdMembershipStatus.None
            else -> HouseholdMembershipStatus.Error(HouseholdGateError.Generic)
        }

    private fun HouseholdMembershipRpcRow.toHousehold(): Household? {
        val resolvedId = spaceId ?: return null
        val resolvedName = spaceName ?: return null
        val resolvedOwnerId = ownerId ?: return null
        return Household(
            id = resolvedId,
            name = resolvedName,
            ownerId = resolvedOwnerId,
            ownerDisplayName = ownerDisplayName?.takeIf { it.isNotBlank() },
            nutritionFeatures = (features ?: emptyList()).mapNotNull { it.toNutritionFeature() }.toSet(),
        )
    }

    private fun HouseholdRpcRow.toDomain(): Household =
        Household(
            id = spaceId,
            name = spaceName,
            ownerId = ownerId,
            ownerDisplayName = ownerDisplayName?.takeIf { it.isNotBlank() },
            nutritionFeatures = (features ?: emptyList()).mapNotNull { it.toNutritionFeature() }.toSet(),
        )

    private fun String.toNutritionFeature(): NutritionSharingFeature? =
        when (this) {
            "grocery" -> NutritionSharingFeature.Grocery
            "meal_plan" -> NutritionSharingFeature.MealPlan
            "ai_advice" -> NutritionSharingFeature.AiAdvice
            else -> null
        }

    private fun String?.toSpaceMemberRole(): SpaceMemberRole =
        when (this) {
            "owner" -> SpaceMemberRole.Owner
            "viewer" -> SpaceMemberRole.Viewer
            else -> SpaceMemberRole.Editor
        }
}
