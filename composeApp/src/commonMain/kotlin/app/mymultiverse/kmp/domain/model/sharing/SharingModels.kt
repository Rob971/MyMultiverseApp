package app.mymultiverse.kmp.domain.model.sharing

enum class AppTopic {
    Nutrition,
    Adventures,
    Budget,
}

enum class GroupLifecycle {
    Persistent,
    Event,
}

enum class NutritionSharingFeature {
    Grocery,
    MealPlan,
    AiAdvice,
}

enum class SpaceMemberRole {
    Owner,
    Editor,
    Viewer,
}

enum class SpaceMemberKind {
    Person,
    Group,
}

data class ContactGroup(
    val id: String,
    val name: String,
    val lifecycle: GroupLifecycle,
    val ownerId: String,
    val eventLabel: String? = null,
    val startsAtEpochMillis: Long? = null,
    val expiresAtEpochMillis: Long? = null,
)

data class SpaceMember(
    val id: String,
    val spaceId: String,
    val kind: SpaceMemberKind,
    val displayName: String,
    val role: SpaceMemberRole,
    val referenceId: String,
)

data class SharingSpace(
    val id: String,
    val topic: AppTopic,
    val name: String,
    val ownerId: String,
    val features: Set<NutritionSharingFeature> = emptySet(),
)
