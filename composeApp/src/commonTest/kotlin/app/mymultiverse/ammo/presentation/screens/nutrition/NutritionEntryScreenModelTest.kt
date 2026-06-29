package app.mymultiverse.ammo.presentation.screens.nutrition

import app.mymultiverse.ammo.domain.model.sharing.Household
import app.mymultiverse.ammo.domain.sharing.DefaultNutritionSharingFeatures
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.presentation.di.FakeHouseholdRepository
import app.mymultiverse.ammo.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.ammo.presentation.screens.nutrition.FakeNutritionHouseholdSelectionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionEntryScreenModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun ensureHousehold_resolvesHouseholdAndActivatesSession() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository()
        val selectionStore = FakeNutritionHouseholdSelectionStore()
        val sessionCoordinator = FakeNutritionSessionCoordinator(
            initialRepository = app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl(
                com.russhwolf.settings.MapSettings(),
            ),
        )
        val model = NutritionEntryScreenModel(
            householdRepository = householdRepository,
            selectionStore = selectionStore,
            sessionCoordinator = sessionCoordinator,
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val ready = model.state.value
        assertIs<NutritionEntryState.Ready>(ready)
        assertEquals("household-1", ready.household.id)
        assertEquals("Our household", ready.household.name)
        assertEquals("test-user", ready.household.ownerId)
        assertEquals("Test User", ready.household.ownerDisplayName)
        assertEquals(
            setOf(
                app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature.Grocery,
                app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature.MealPlan,
                app.mymultiverse.ammo.domain.model.sharing.NutritionSharingFeature.AiAdvice,
            ),
            ready.household.nutritionFeatures,
        )
        assertEquals("household-1", selectionStore.activeHouseholdId.value)
        assertEquals("household-1", sessionCoordinator.activatedHouseholdId)
        assertEquals(1, householdRepository.ensureCalls)
    }

    @Test
    fun ensureHousehold_appliesDefaultFeaturesWhenHouseholdReturnsEmpty() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository(
            household = Household(
                id = "household-1",
                name = "Legacy household",
                ownerId = "test-user",
                ownerDisplayName = "Test User",
                nutritionFeatures = emptySet(),
            ),
        )
        val model = NutritionEntryScreenModel(
            householdRepository = householdRepository,
            selectionStore = FakeNutritionHouseholdSelectionStore(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl(
                    com.russhwolf.settings.MapSettings(),
                ),
            ),
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val ready = model.state.value
        assertIs<NutritionEntryState.Ready>(ready)
        assertEquals(DefaultNutritionSharingFeatures, ready.household.nutritionFeatures)
    }

    @Test
    fun ensureHousehold_onFailure_setsErrorState() = runTest(testDispatcher) {
        val model = NutritionEntryScreenModel(
            householdRepository = FakeHouseholdRepository(
                ensureFailure = IllegalStateException("supabase_not_configured"),
            ),
            selectionStore = FakeNutritionHouseholdSelectionStore(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl(
                    com.russhwolf.settings.MapSettings(),
                ),
            ),
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val error = model.state.value
        assertIs<NutritionEntryState.Error>(error)
        assertEquals(NutritionEntryError.NotConfigured, error.error)
    }
}
