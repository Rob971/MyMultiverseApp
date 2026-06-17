package app.mymultiverse.kmp.presentation.screens.household

import app.mymultiverse.kmp.data.observability.TestObservability
import app.mymultiverse.kmp.domain.model.sharing.Household
import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import app.mymultiverse.kmp.domain.sharing.DefaultNutritionSharingFeatures
import app.mymultiverse.kmp.presentation.di.FakeHouseholdRepository
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
class HouseholdMembersEntryScreenModelTest {

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
    fun ensureHousehold_resolvesSharedNavigationContext() = runTest(testDispatcher) {
        val householdRepository = FakeHouseholdRepository()
        val model = HouseholdMembersEntryScreenModel(
            householdRepository = householdRepository,
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val ready = model.state.value
        assertIs<HouseholdMembersEntryState.Ready>(ready)
        assertEquals("household-1", ready.household.id)
        assertEquals("Our household", ready.household.name)
        assertEquals("test-user", ready.household.ownerId)
        assertEquals("Test User", ready.household.ownerDisplayName)
        assertEquals(
            setOf(
                NutritionSharingFeature.Grocery,
                NutritionSharingFeature.MealPlan,
                NutritionSharingFeature.AiAdvice,
            ),
            ready.household.nutritionFeatures,
        )
        assertEquals(1, householdRepository.ensureCalls)
    }

    @Test
    fun ensureHousehold_appliesDefaultNutritionFeaturesWhenHouseholdReturnsEmpty() = runTest(testDispatcher) {
        val model = HouseholdMembersEntryScreenModel(
            householdRepository = FakeHouseholdRepository(
                household = Household(
                    id = "household-1",
                    name = "Legacy household",
                    ownerId = "test-user",
                    ownerDisplayName = "Test User",
                    nutritionFeatures = emptySet(),
                ),
            ),
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val ready = model.state.value
        assertIs<HouseholdMembersEntryState.Ready>(ready)
        assertEquals(DefaultNutritionSharingFeatures, ready.household.nutritionFeatures)
    }

    @Test
    fun ensureHousehold_onFailure_setsErrorState() = runTest(testDispatcher) {
        val model = HouseholdMembersEntryScreenModel(
            householdRepository = FakeHouseholdRepository(
                ensureFailure = IllegalStateException("supabase_not_configured"),
            ),
            logger = TestObservability.logger,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + kotlinx.coroutines.SupervisorJob()),
        )

        model.ensureHousehold()
        advanceUntilIdle()

        val error = model.state.value
        assertIs<HouseholdMembersEntryState.Error>(error)
        assertEquals(HouseholdMembersEntryError.NotConfigured, error.error)
    }
}
