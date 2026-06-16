package app.mymultiverse.kmp.presentation.navigation

data class HouseholdContext(
    val id: String,
    val name: String,
    val ownerId: String,
    val ownerDisplayName: String? = null,
)
