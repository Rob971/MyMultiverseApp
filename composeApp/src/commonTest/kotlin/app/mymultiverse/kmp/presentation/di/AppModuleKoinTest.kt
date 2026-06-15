package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.domain.model.auth.AuthState
import app.mymultiverse.kmp.domain.model.auth.AuthUser
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.SharingSpaceRepository
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModel
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
        single<SharingSpaceRepository> { FakeSharingSpaceRepository() }
        single<SpaceCollaborationRepository> { FakeSpaceCollaborationRepository() }
    }

    private val testAppModule = module {
        includes(coreKoinModules())
        includes(testPlatformModule)
    }

    @BeforeTest
    fun start() {
        Dispatchers.setMain(testDispatcher)
        startKoin { modules(testAppModule) }
    }

    @AfterTest
    fun stop() {
        stopKoin()
        Dispatchers.resetMain()
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
    fun homeScreenModel_resolvesAndLoadsGreeting() = runTest(testDispatcher) {
        val model = get<HomeScreenModel>()

        advanceUntilIdle()

        assertNotNull(model.greeting.value)
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
    fun nutritionRepository_isSingletonWithScreenModel() {
        val repositoryFromModel = get<NutritionScreenModel>().weekKey
        val repositoryDirect = get<NutritionRepository>().weekKey

        assertEquals(repositoryDirect, repositoryFromModel)
    }

    @Test
    fun mapSettings_isSharedAcrossNutritionRepository() {
        val settings = get<Settings>()
        val repository = get<NutritionRepository>()

        runTest(testDispatcher) {
            repository.saveGroceryItems(
                listOf(
                    app.mymultiverse.kmp.domain.model.nutrition.GroceryItem(
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
