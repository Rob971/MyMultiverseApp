package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivity
import app.mymultiverse.ammo.domain.nutrition.NutritionCollaborationActivityKind
import app.mymultiverse.ammo.domain.nutrition.WeekCalendar
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import app.mymultiverse.ammo.presentation.components.NutritionSyncStatusTestTags
import app.mymultiverse.ammo.presentation.screens.nutrition.GroceryShoppingScreen
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.ammo.presentation.theme.AppTheme
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * S6 UI tests: the removal snackbar and the sync banner must be visible INSIDE the
 * screen container a user actually sees (model tests cannot observe rendering).
 */
@RunWith(AndroidJUnit4::class)
class SyncUxInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun screenModel(
        coordinator: InstrumentedNutritionSessionCoordinator,
        repository: InstrumentedNutritionRepository,
    ): NutritionScreenModel {
        return NutritionScreenModel(
            session = coordinator,
            householdRepository = InstrumentedHouseholdRepository(),
            collaborationRepository = InstrumentedHouseholdCollaborationRepository(),
            aiAssistant = InstrumentedNutritionAdviceService(),
            favoriteDishesRepository = InstrumentedFavoriteDishesRepository(),
            ghostPairingDismissStore = GroceryGhostPairingDismissStore(MapSettings()),
            logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented")),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )
    }

    @Test
    fun partnerRemoval_snackbarIsDisplayedInGroceryScreen() {
        val repository = InstrumentedNutritionRepository(WeekCalendar.currentWeekKey())
        repository.grocery.value = listOf(GroceryItem(id = "milk-1", label = "Milk"))
        val coordinator = InstrumentedNutritionSessionCoordinator(repository)
        val screenModel = screenModel(coordinator, repository)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        // A partner removal arrives (realtime or pull) while the user is on groceries
        coordinator.emitCollaborationActivity(
            NutritionCollaborationActivity(
                actorUserId = null,
                kind = NutritionCollaborationActivityKind.GroceryRemoved,
                itemLabel = "Milk",
                removedItem = GroceryItem(id = "milk-1", label = "Milk"),
            ),
        )

        // Batching debounces for 2 s, then the snackbar shows for a few seconds
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithText("removed Milk", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("removed Milk", substring = true, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun syncBanner_isDisplayedOnGroceryScreenWhenPendingPush() {
        val repository = InstrumentedNutritionRepository(WeekCalendar.currentWeekKey())
        val coordinator = InstrumentedNutritionSessionCoordinator(repository)
        val screenModel = screenModel(coordinator, repository)

        composeRule.setContent {
            AppTheme {
                InstrumentedKoinHost {
                    GroceryShoppingScreen(onBack = {}, screenModel = screenModel)
                }
            }
        }

        assertTrue(
            "no banner when Idle",
            composeRule.onAllNodesWithTag(NutritionSyncStatusTestTags.BANNER)
                .fetchSemanticsNodes().isEmpty(),
        )

        coordinator.emitSyncStatus(NutritionSyncStatus.PendingPush(2))

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(NutritionSyncStatusTestTags.BANNER)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(NutritionSyncStatusTestTags.BANNER).assertIsDisplayed()
    }
}