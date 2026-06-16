package app.mymultiverse.kmp.presentation.screens.nutrition

import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
import app.mymultiverse.kmp.presentation.di.FakeNutritionSessionCoordinator
import app.mymultiverse.kmp.presentation.screens.nutrition.spaces.FakeNutritionSpaceSelectionStore
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
    fun ensureHousehold_resolvesSpaceAndActivatesSession() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository()
        val selectionStore = FakeNutritionSpaceSelectionStore()
        val sessionCoordinator = FakeNutritionSessionCoordinator(
            initialRepository = app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl(
                com.russhwolf.settings.MapSettings(),
            ),
        )
        val model = NutritionEntryScreenModel(
            householdRepository = householdRepository,
            selectionStore = selectionStore,
            sessionCoordinator = sessionCoordinator,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val ready = model.state.value
        assertIs<NutritionEntryState.Ready>(ready)
        assertEquals("household-space-1", ready.space.id)
        assertEquals("household-space-1", selectionStore.activeSpaceId.value)
        assertEquals("household-space-1", sessionCoordinator.activatedSpaceId)
        assertEquals(1, householdRepository.ensureCalls)
    }

    @Test
    fun ensureHousehold_onFailure_setsErrorState() = runTest(testDispatcher) {
        val model = NutritionEntryScreenModel(
            householdRepository = FakeHouseholdRepository(
                ensureFailure = IllegalStateException("supabase_not_configured"),
            ),
            selectionStore = FakeNutritionSpaceSelectionStore(),
            sessionCoordinator = FakeNutritionSessionCoordinator(
                initialRepository = app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl(
                    com.russhwolf.settings.MapSettings(),
                ),
            ),
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val error = model.state.value
        assertIs<NutritionEntryState.Error>(error)
        assertEquals(NutritionEntryError.NotConfigured, error.error)
    }
}
