package app.mymultiverse.kmp.data.supabase

import app.mymultiverse.kmp.data.supabase.dto.NutritionFeatureInsertRow
import app.mymultiverse.kmp.data.supabase.dto.NutritionFeatureRow
import app.mymultiverse.kmp.data.supabase.dto.ProfileInsertRow
import app.mymultiverse.kmp.data.supabase.dto.SharingSpaceInsertRow
import app.mymultiverse.kmp.data.supabase.dto.SharingSpaceRow
import app.mymultiverse.kmp.domain.model.sharing.AppTopic
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.model.sharing.SharingSpace
import app.mymultiverse.kmp.domain.repository.SharingSpaceRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SupabaseSharingSpaceRepository(
    private val client: SupabaseClient,
) : SharingSpaceRepository {

    private val nutritionSpaces = MutableStateFlow<List<SharingSpace>>(emptyList())

    override fun observeNutritionSpaces(): Flow<List<SharingSpace>> = nutritionSpaces.asStateFlow()

    override suspend fun refreshNutritionSpaces() {
        val userId = requireUserId()
        ensureProfile(userId)

        val spaces = client.postgrest["sharing_spaces"]
            .select(Columns.ALL) {
                filter {
                    eq("topic", AppTopic.Nutrition.wireName())
                }
            }
            .decodeList<SharingSpaceRow>()

        val spaceIds = spaces.map { it.id }
        val featuresBySpaceId = if (spaceIds.isEmpty()) {
            emptyMap()
        } else {
            client.postgrest["space_nutrition_features"]
                .select(Columns.ALL) {
                    filter {
                        isIn("space_id", spaceIds)
                    }
                }
                .decodeList<NutritionFeatureRow>()
                .groupBy { it.spaceId }
                .mapValues { (_, rows) -> rows.mapNotNull { it.feature.toNutritionFeature() }.toSet() }
        }

        nutritionSpaces.update {
            spaces.map { row ->
                SharingSpace(
                    id = row.id,
                    topic = AppTopic.Nutrition,
                    name = row.name,
                    ownerId = row.ownerId,
                    features = featuresBySpaceId[row.id].orEmpty(),
                )
            }.sortedBy { it.name.lowercase() }
        }
    }

    override suspend fun createNutritionSpace(
        name: String,
        features: Set<NutritionSharingFeature>,
    ): Result<SharingSpace> = runCatching {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "space_name_required" }
        require(features.isNotEmpty()) { "space_features_required" }

        val userId = requireUserId()
        ensureProfile(userId)

        val created = client.postgrest["sharing_spaces"]
            .insert(
                SharingSpaceInsertRow(
                    topic = AppTopic.Nutrition.wireName(),
                    name = trimmedName,
                    ownerId = userId,
                ),
            ) {
                select(Columns.ALL)
            }
            .decodeSingle<SharingSpaceRow>()

        val featureRows = features.map { feature ->
            NutritionFeatureInsertRow(
                spaceId = created.id,
                feature = feature.wireName(),
            )
        }
        client.postgrest["space_nutrition_features"].insert(featureRows)

        val sharingSpace = SharingSpace(
            id = created.id,
            topic = AppTopic.Nutrition,
            name = created.name,
            ownerId = created.ownerId,
            features = features,
        )
        upsertLocalSpace(sharingSpace)
        runCatching { refreshNutritionSpaces() }

        sharingSpace
    }

    private suspend fun ensureProfile(userId: String) {
        val rpcResult = runCatching { client.postgrest.rpc("ensure_current_profile") }
        if (rpcResult.isSuccess) return

        val email = client.auth.currentUserOrNull()?.email
        client.postgrest["profiles"]
            .upsert(
                ProfileInsertRow(
                    id = userId,
                    email = email,
                    displayName = email?.substringBefore("@"),
                ),
            ) {
                onConflict = "id"
            }
    }

    private fun upsertLocalSpace(space: SharingSpace) {
        nutritionSpaces.update { current ->
            (current.filterNot { it.id == space.id } + space)
                .sortedBy { it.name.lowercase() }
        }
    }

    private suspend fun requireUserId(): String {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("auth_required")
    }

    private fun AppTopic.wireName(): String =
        when (this) {
            AppTopic.Nutrition -> "nutrition"
            AppTopic.Adventures -> "adventures"
            AppTopic.Budget -> "budget"
        }

    private fun NutritionSharingFeature.wireName(): String =
        when (this) {
            NutritionSharingFeature.Grocery -> "grocery"
            NutritionSharingFeature.MealPlan -> "meal_plan"
            NutritionSharingFeature.AiAdvice -> "ai_advice"
        }

    private fun String.toNutritionFeature(): NutritionSharingFeature? =
        when (this) {
            "grocery" -> NutritionSharingFeature.Grocery
            "meal_plan" -> NutritionSharingFeature.MealPlan
            "ai_advice" -> NutritionSharingFeature.AiAdvice
            else -> null
        }
}
