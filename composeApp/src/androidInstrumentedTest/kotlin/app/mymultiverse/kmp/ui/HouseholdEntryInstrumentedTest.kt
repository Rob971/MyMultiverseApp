package app.mymultiverse.kmp.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.data.observability.NoOpCrashReporter
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionEntryGate
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionEntryScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionEntryTestTags
import app.mymultiverse.kmp.presentation.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class HouseholdEntryInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val logger = AppLogger(NoOpCrashReporter(), DiagnosticsContext(sessionId = "instrumented"))

    @Test
    fun nutritionEntryGate_loadsHouseholdAndNavigatesToHub() {
        val householdRepository = InstrumentedHouseholdRepository()
        val selectionStore = InstrumentedNutritionSpaceSelectionStore()
        val sessionCoordinator = InstrumentedNutritionSessionCoordinator(
            repository = InstrumentedNutritionRepository(weekKey = "2026-06-16"),
        )
        val screenModel = NutritionEntryScreenModel(
            householdRepository = householdRepository,
            selectionStore = selectionStore,
            sessionCoordinator = sessionCoordinator,
            logger = logger,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )
        var resolvedHouseholdId: String? = null

        composeRule.setContent {
            AppTheme {
                KoinApplication(application = { modules(testModule(householdRepository, selectionStore, sessionCoordinator)) }) {
                    NutritionEntryGate(
                        onBack = {},
                        onReady = { resolvedHouseholdId = it.id },
                        screenModel = screenModel,
                    )
                }
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            resolvedHouseholdId != null
        }

        assertEquals("household-space-1", resolvedHouseholdId)
        assertEquals("household-space-1", selectionStore.activeSpaceId.value)
        assertEquals("household-space-1", sessionCoordinator.activatedSpaceId)
        assertEquals(1, householdRepository.ensureCalls)
    }

    @Test
    fun nutritionEntryGate_showsRetryOnSupabaseMisconfiguration() {
        val householdRepository = InstrumentedHouseholdRepository(
            ensureFailure = IllegalStateException("supabase_not_configured"),
        )
        val screenModel = NutritionEntryScreenModel(
            householdRepository = householdRepository,
            selectionStore = InstrumentedNutritionSpaceSelectionStore(),
            sessionCoordinator = InstrumentedNutritionSessionCoordinator(
                repository = InstrumentedNutritionRepository(weekKey = "2026-06-16"),
            ),
            logger = logger,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
        )

        composeRule.setContent {
            AppTheme {
                NutritionEntryGate(
                    onBack = {},
                    onReady = {},
                    screenModel = screenModel,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(NutritionEntryTestTags.ERROR).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(NutritionEntryTestTags.RETRY_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(NutritionEntryTestTags.RETRY_BUTTON).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            householdRepository.ensureCalls >= 2
        }
        assertEquals(2, householdRepository.ensureCalls)
    }

    private fun testModule(
        householdRepository: HouseholdRepository,
        selectionStore: NutritionSpaceSelectionStore,
        sessionCoordinator: NutritionSessionCoordinator,
    ) = module {
        single<HouseholdRepository> { householdRepository }
        single<NutritionSpaceSelectionStore> { selectionStore }
        single<NutritionSessionCoordinator> { sessionCoordinator }
    }
}
