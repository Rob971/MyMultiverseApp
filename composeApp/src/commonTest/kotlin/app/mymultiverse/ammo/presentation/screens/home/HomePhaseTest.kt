package app.mymultiverse.ammo.presentation.screens.home

import app.mymultiverse.ammo.domain.model.sharing.HouseholdGateError
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembership
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMemberRole
import app.mymultiverse.ammo.domain.model.sharing.Household
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HomePhaseTest {

    @Test
    fun toHomePhase_mapsMembershipStatuses() {
        assertEquals(HomePhase.Loading, HouseholdMembershipStatus.Loading.toHomePhase())
        assertEquals(HomePhase.Onboarding, HouseholdMembershipStatus.None.toHomePhase())
        assertEquals(
            HomePhase.Welcome,
            HouseholdMembershipStatus.Active(
                HouseholdMembership(
                    household = Household(
                        id = "h1",
                        name = "Test",
                        ownerId = "o1",
                        ownerDisplayName = "Owner",
                        nutritionFeatures = setOf(NutritionSharingFeature.Grocery),
                    ),
                    role = HouseholdMemberRole.Owner,
                ),
            ).toHomePhase(),
        )
        assertEquals(
            HomePhase.Error(HouseholdGateError.Generic),
            HouseholdMembershipStatus.Error(HouseholdGateError.Generic).toHomePhase(),
        )
        assertIs<HomePhase.Error>(
            HouseholdMembershipStatus.Error(HouseholdGateError.NotConfigured).toHomePhase(),
        )
    }
}
