package app.mymultiverse.ammo.presentation.screens.home

data class HomeRenameUiState(
    val isVisible: Boolean = false,
    val nameInput: String = "",
    val isSaving: Boolean = false,
    val nameAvailability: HouseholdNameAvailability = HouseholdNameAvailability.Unknown,
)
