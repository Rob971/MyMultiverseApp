package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature

/** Navigation snapshot for the active household (members, nutrition, and future modules). */
data class HouseholdContext(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerDisplayName: String? = null,
    val nutritionFeatures: Set<NutritionSharingFeature> = emptySet(),
) {
    fun includesNutritionFeature(feature: NutritionSharingFeature): Boolean =
        feature in nutritionFeatures
}
