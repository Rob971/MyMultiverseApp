package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.domain.model.sharing.HouseholdGateError
import app.mymultiverse.ammo.domain.model.sharing.HouseholdMembershipStatus
import app.mymultiverse.ammo.presentation.InviteRecoveryGate
import app.mymultiverse.ammo.presentation.InviteRecoveryGateTestTags
import app.mymultiverse.ammo.presentation.invite.InviteJoinAcceptState
import app.mymultiverse.ammo.presentation.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InviteRecoveryGateInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Test
    fun membershipError_showsRetryAndRefreshesOnTap() {
        val householdRepo = InstrumentedHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.Error(HouseholdGateError.Generic),
        )

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    InviteRecoveryGate(
                        membership = HouseholdMembershipStatus.Error(HouseholdGateError.Generic),
                        pendingInviteToken = null,
                        acceptState = InviteJoinAcceptState.Idle,
                        householdRepository = householdRepo,
                        onRetryAccept = {},
                        onExitInvite = {},
                        retryScope = testScope,
                    )
                }
            }
        }

        // Not a bare spinner — a retry button is visible.
        composeRule.onNodeWithTag(InviteRecoveryGateTestTags.RETRY).assertIsDisplayed()
        composeRule.onNodeWithTag(InviteRecoveryGateTestTags.RETRY).performClick()

        // Retry "works": refreshMembership was called.
        composeRule.waitUntil(2_000) { householdRepo.refreshCalls == 1 }
    }

    @Test
    fun pendingInvite_membershipError_cancelClearsInvitation() {
        val householdRepo = InstrumentedHouseholdRepository(
            initialMembershipStatus = HouseholdMembershipStatus.Error(HouseholdGateError.Generic),
        )
        var exitCalled = false

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    InviteRecoveryGate(
                        membership = HouseholdMembershipStatus.Error(HouseholdGateError.Generic),
                        pendingInviteToken = "token-abc",
                        acceptState = InviteJoinAcceptState.Idle,
                        householdRepository = householdRepo,
                        onRetryAccept = {},
                        onExitInvite = { exitCalled = true },
                        retryScope = testScope,
                    )
                }
            }
        }

        // Cancel / exit: must be visible when an invitation is held.
        composeRule.onNodeWithTag(InviteRecoveryGateTestTags.CANCEL).assertIsDisplayed()
        composeRule.onNodeWithTag(InviteRecoveryGateTestTags.CANCEL).performClick()

        assert(exitCalled) { "Expected Cancel to trigger the exit callback" }
    }
}