package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersEntryScreenModel
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersEntryTestTags
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersGate
import app.mymultiverse.ammo.presentation.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HouseholdMembersEntryInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented"))

    @Test
    fun householdMembersEntryGate_showsRetryOnSupabaseMisconfiguration() {
        val householdRepository = InstrumentedHouseholdRepository(
            ensureFailure = IllegalStateException("supabase_not_configured"),
        )
        val screenModel = HouseholdMembersEntryScreenModel(
            householdRepository = householdRepository,
            logger = logger,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )

        composeRule.setContent {
            AppTheme {
                HouseholdMembersGate(
                    onBack = {},
                    onReady = {},
                    entryScreenModel = screenModel,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(HouseholdMembersEntryTestTags.ERROR)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithTag(HouseholdMembersEntryTestTags.RETRY_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(HouseholdMembersEntryTestTags.RETRY_BUTTON).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            householdRepository.ensureCalls >= 2
        }

        assertEquals(2, householdRepository.ensureCalls)
    }
}

