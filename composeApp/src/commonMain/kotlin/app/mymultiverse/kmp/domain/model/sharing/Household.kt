package app.mymultiverse.kmp.domain.model.sharing

/**
 * The single household shared by everyone who lives together.
 * Nutrition, Adventures, and Budget all use this household for membership and persistence scope.
 *
 * Today [id] is the nutrition household id in Supabase; future topic households will reference
 * the same household membership without separate groups.
 */
data class Household(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerDisplayName: String?,
    val nutritionFeatures: Set<NutritionSharingFeature>,
)
