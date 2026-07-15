package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.data.supabase.SupabaseRuntimeFlags
import app.mymultiverse.ammo.data.observability.NoOpCrashReporter
import app.mymultiverse.ammo.domain.observability.CrashReporter
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.manager.ThemeManager
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import app.mymultiverse.ammo.domain.model.auth.AuthState
import app.mymultiverse.ammo.domain.model.auth.AuthUser
import app.mymultiverse.ammo.domain.platform.PersonalDataExporter
import app.mymultiverse.ammo.domain.platform.PushNotificationRegistrar
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import app.mymultiverse.ammo.presentation.screens.home.HomeScreenModel
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionEntryScreenModel
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionScreenModel
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies the production Koin graph (with test platform bindings) resolves every type
 * needed when navigating Home → Nutrition. Catches mis-wired constructors such as
 * `singleOf(::NutritionScreenModel)` resolving optional CoroutineScope parameters.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleKoinTest : KoinTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testPlatformModule = module {
        single<Settings> { MapSettings() }
        single<LanguageManager> { FakeLanguageManager() }
        single<ThemeManager> { FakeThemeManager() }
        single<AiAssistantSettings> { FakeAiAssistantSettings() }
        single<CrashReporter> { NoOpCrashReporter() }
        single<AuthRepository> {
            FakeAuthRepository(
                initialState = AuthState.Authenticated(
                    AuthUser(
                        id = "test-user",
                        email = "test@example.com",
                        displayName = "Test User",
                    ),
                ),
            )
        }
        single<HouseholdRepository> { FakeHouseholdRepository() }
        single<HouseholdCollaborationRepository> { FakeHouseholdCollaborationRepository() }
        single<PersonalDataExporter> { FakePersonalDataExporter() }
        single<PushNotificationRegistrar> { FakePushNotificationRegistrar() }
    }

    private val testAppModule = module {
        includes(coreKoinModules())
        includes(testPlatformModule)
    }

    @BeforeTest
    fun start() {
        SupabaseRuntimeFlags.disableClientCreation = true
        Dispatchers.setMain(testDispatcher)
        startKoin {
            allowOverride(true)
            modules(testAppModule)
        }
    }

    @AfterTest
    fun stop() {
        stopKoin()
        SupabaseRuntimeFlags.disableClientCreation = false
        Dispatchers.resetMain()
    }

    @Test
    fun homeScreenModel_resolvesAndLoadsGreeting() = runTest(testDispatcher) {
        val model = get<HomeScreenModel>()

        advanceUntilIdle()

        assertNotNull(model.greeting.value)
    }

    @Test
    fun nutritionScreenModel_resolvesWithRepositoryAndAdviceService() {
        val model = get<NutritionScreenModel>()
        val repository = get<NutritionRepository>()
        val advice = get<NutritionAiAssistantService>()

        assertNotNull(model)
        assertNotNull(repository)
        assertNotNull(advice)
        assertEquals(repository.weekKey, model.weekKey)
    }

    @Test
    fun nutritionScreenModel_canMutateGroceryAfterKoinResolution() = runTest(testDispatcher) {
        val model = get<NutritionScreenModel>()

        model.addGroceryItem("Tomatoes")
        advanceUntilIdle()

        val items = model.groceryItems.first()
        assertEquals(1, items.size)
        assertEquals("Tomatoes", items.single().label)
    }

    @Test
    fun nutritionSessionCoordinator_resolvesWithScreenModel() {
        val coordinator = get<NutritionSessionCoordinator>()
        val model = get<NutritionScreenModel>()

        assertNotNull(coordinator)
        assertNotNull(model)
        assertEquals(coordinator.nutrition.value.weekKey, model.weekKey)
    }

    @Test
    fun nutritionScreenModel_mutationsFlowThroughSessionCoordinator() = runTest(testDispatcher) {
        val coordinator = get<NutritionSessionCoordinator>()
        val model = get<NutritionScreenModel>()

        model.addGroceryItem("Session path")
        advanceUntilIdle()

        assertEquals(1, coordinator.nutrition.value.observeGroceryItems().first().size)
    }

    @Test
    fun activateHousehold_switchesActiveNutritionToSharedScope() = runTest(testDispatcher) {
        val coordinator = get<NutritionSessionCoordinator>()

        coordinator.activateHousehold("koin-household")
        advanceUntilIdle()

        assertEquals("koin-household", coordinator.nutrition.value.householdId)
        assertEquals(NutritionSyncStatus.RemoteUnavailable, coordinator.observeSyncStatus().first())
    }

    @Test
    fun nutritionScreenModel_sharesWeekKeyWithPersonalRepository() {
        val repositoryFromModel = get<NutritionScreenModel>().weekKey
        val repositoryDirect = get<NutritionRepository>().weekKey

        assertEquals(repositoryDirect, repositoryFromModel)
    }

    @Test
    fun nutritionEntryScreenModel_resolvesFromKoinGraph() {
        val model = get<NutritionEntryScreenModel>()
        assertNotNull(model)
    }

    @Test
    fun mapSettings_isSharedAcrossNutritionRepository() {
        val settings = get<Settings>()
        val repository = get<NutritionRepository>()

        runTest(testDispatcher) {
            repository.saveGroceryItems(
                listOf(
                    app.mymultiverse.ammo.domain.model.nutrition.GroceryItem(
                        id = "1",
                        label = "Milk",
                        isChecked = false,
                    ),
                ),
            )
        }

        assertTrue(settings.getStringOrNull("nutrition_grocery_${repository.weekKey}") != null)
    }
}
