package app.mymultiverse.ammo.presentation.navigation

import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature

/** Navigation snapshot for the active household (members, nutrition, and future modules). */
data class HouseholdContext(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerDisplayName: String? = null,
    val nutritionFeatures: Set<NutritionSharingFeature> = emptySet(),
    val avatarUrl: String? = null,
) {
    fun includesNutritionFeature(feature: NutritionSharingFeature): Boolean =
        feature in nutritionFeatures
}
