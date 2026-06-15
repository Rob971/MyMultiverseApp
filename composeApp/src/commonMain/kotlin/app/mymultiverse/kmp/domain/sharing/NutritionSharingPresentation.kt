package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature

fun NutritionSharingFeature.sortOrder(): Int =
    when (this) {
        NutritionSharingFeature.Grocery -> 0
        NutritionSharingFeature.MealPlan -> 1
        NutritionSharingFeature.AiAdvice -> 2
    }

fun Set<NutritionSharingFeature>.sortedFeatures(): List<NutritionSharingFeature> =
    sortedBy { it.sortOrder() }
