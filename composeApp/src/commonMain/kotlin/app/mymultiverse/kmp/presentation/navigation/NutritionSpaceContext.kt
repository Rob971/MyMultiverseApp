package app.mymultiverse.kmp.presentation.navigation

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature

data class NutritionSpaceContext(
    val id: String,
    val name: String,
    val features: Set<NutritionSharingFeature>,
) {
    fun includes(feature: NutritionSharingFeature): Boolean = feature in features
}
