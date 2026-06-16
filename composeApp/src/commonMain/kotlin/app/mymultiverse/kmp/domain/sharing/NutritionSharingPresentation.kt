package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature

/** All nutrition hub categories enabled for a household by default. */
val DefaultNutritionSharingFeatures: Set<NutritionSharingFeature> = setOf(
    NutritionSharingFeature.Grocery,
    NutritionSharingFeature.MealPlan,
    NutritionSharingFeature.AiAdvice,
)

fun Set<NutritionSharingFeature>.orDefaultNutritionFeatures(): Set<NutritionSharingFeature> =
    if (isEmpty()) DefaultNutritionSharingFeatures else this

fun NutritionSharingFeature.sortOrder(): Int =
    when (this) {
        NutritionSharingFeature.Grocery -> 0
        NutritionSharingFeature.MealPlan -> 1
        NutritionSharingFeature.AiAdvice -> 2
    }

fun Set<NutritionSharingFeature>.sortedFeatures(): List<NutritionSharingFeature> =
    sortedBy { it.sortOrder() }
