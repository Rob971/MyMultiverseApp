package app.mymultiverse.ammo.domain.model.sharing

enum class NutritionSharingFeature {
    Grocery,
    MealPlan,
    AiAdvice,
}

enum class HouseholdMemberRole {
    Owner,
    Admin,
    Editor,
    Viewer,
}

enum class HouseholdMemberKind {
    Person,
    Group,
    Dependant,
}

data class HouseholdMember(
    val id: String,
    val householdId: String,
    val kind: HouseholdMemberKind,
    val displayName: String,
    val role: HouseholdMemberRole,
    val referenceId: String,
)
