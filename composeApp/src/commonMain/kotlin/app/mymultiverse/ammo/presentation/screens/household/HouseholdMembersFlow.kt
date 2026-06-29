package app.mymultiverse.ammo.presentation.screens.household

import androidx.compose.runtime.Composable
import app.mymultiverse.ammo.presentation.navigation.HouseholdContext

@Composable
fun HouseholdMembersFlow(
    household: HouseholdContext?,
    onBack: () -> Unit,
    onHouseholdReady: (HouseholdContext) -> Unit,
) {
    if (household == null) {
        HouseholdMembersGate(
            onBack = onBack,
            onReady = onHouseholdReady,
        )
        return
    }

    HouseholdMembersScreen(
        household = household,
        onBack = onBack,
    )
}
