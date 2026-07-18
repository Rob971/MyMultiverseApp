package app.mymultiverse.ammo.presentation.di

import app.mymultiverse.ammo.data.location.LocationLanguageSuggestionBootstrapper
import app.mymultiverse.ammo.data.observability.AppLogger
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteApi
import app.mymultiverse.ammo.data.repository.GreetingRepositoryImpl
import app.mymultiverse.ammo.data.repository.NutritionRepositoryImpl
import app.mymultiverse.ammo.data.manager.SettingsThemeManager
import app.mymultiverse.ammo.data.home.HomeFirstWinChecklistStore
import app.mymultiverse.ammo.data.home.HomeWeekPlanNudgeStore
import app.mymultiverse.ammo.data.nutrition.GroceryGhostPairingDismissStore
import app.mymultiverse.ammo.data.invite.InviteSessionStore
import app.mymultiverse.ammo.data.repository.SettingsNutritionHouseholdSelectionStore
import app.mymultiverse.ammo.data.ai.AiSecrets
import app.mymultiverse.ammo.data.manager.SettingsAiAssistantSettings
import app.mymultiverse.ammo.data.manager.SyncedAiAssistantSettings
import app.mymultiverse.ammo.data.supabase.SupabaseAiSettingsRepository
import app.mymultiverse.ammo.data.supabase.UnconfiguredAiSettingsRepository
import app.mymultiverse.ammo.domain.repository.AiSettingsRemoteRepository
import app.mymultiverse.ammo.data.service.GeminiApiClient
import app.mymultiverse.ammo.data.service.GeminiDishIngredientClient
import app.mymultiverse.ammo.data.service.GeminiTextClient
import app.mymultiverse.ammo.data.service.LocalNutritionAiAssistantService
import app.mymultiverse.ammo.data.service.RemoteNutritionAiAssistantService
import app.mymultiverse.ammo.domain.settings.AiAssistantSettings
import app.mymultiverse.ammo.data.supabase.SupabaseAuthRepository
import app.mymultiverse.ammo.data.supabase.SupabaseClientHolder
import app.mymultiverse.ammo.data.supabase.SupabaseHouseholdRepository
import app.mymultiverse.ammo.data.supabase.SupabaseHouseholdCollaborationRepository
import app.mymultiverse.ammo.data.supabase.UnconfiguredAuthRepository
import app.mymultiverse.ammo.data.supabase.UnconfiguredHouseholdRepository
import app.mymultiverse.ammo.data.supabase.UnconfiguredHouseholdCollaborationRepository
import app.mymultiverse.ammo.data.sync.NutritionSessionCoordinatorImpl
import app.mymultiverse.ammo.data.sync.NutritionHouseholdRealtimeSync
import app.mymultiverse.ammo.domain.observability.DiagnosticsContext
import app.mymultiverse.ammo.domain.repository.AuthRepository
import app.mymultiverse.ammo.domain.repository.GreetingRepository
import app.mymultiverse.ammo.domain.repository.HouseholdRepository
import app.mymultiverse.ammo.domain.repository.NutritionRepository
import app.mymultiverse.ammo.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.ammo.domain.repository.NutritionHouseholdSelectionStore
import app.mymultiverse.ammo.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.ammo.domain.service.NutritionAiAssistantService
import app.mymultiverse.ammo.domain.location.DeviceRegionService
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.manager.ThemeManager
import app.mymultiverse.ammo.domain.usecase.GetGreetingUseCase
import app.mymultiverse.ammo.presentation.registration.RegistrationData
import app.mymultiverse.ammo.presentation.screens.auth.LoginScreenModel
import app.mymultiverse.ammo.presentation.screens.onboarding.OnboardingScreenModel
import app.mymultiverse.ammo.presentation.screens.householdsetup.HouseholdSetupScreenModel
import app.mymultiverse.ammo.presentation.screens.home.HomeScreenModel
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionEntryScreenModel
import app.mymultiverse.ammo.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersEntryScreenModel
import app.mymultiverse.ammo.presentation.screens.household.HouseholdMembersScreenModel
import app.mymultiverse.ammo.presentation.screens.invite.JoinHouseholdScreenModel
import app.mymultiverse.ammo.presentation.invite.InviteJoinFlowCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private val observabilityModule = module {
    single { DiagnosticsContext() }
    single { AppLogger(get(), get()) }
}

private val domainModule = module {
    factoryOf(::GetGreetingUseCase)
}

private val dataModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single { SupabaseClientHolder.create(get()) }
    single<AuthRepository> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            SupabaseAuthRepository(client, get())
        } else {
            UnconfiguredAuthRepository()
        }
    }
    single<HouseholdRepository> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            SupabaseHouseholdRepository(client, get())
        } else {
            UnconfiguredHouseholdRepository()
        }
    }
    single<HouseholdCollaborationRepository> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            SupabaseHouseholdCollaborationRepository(client, get())
        } else {
            UnconfiguredHouseholdCollaborationRepository()
        }
    }
    single<NutritionHouseholdSelectionStore> { SettingsNutritionHouseholdSelectionStore(get()) }
    single { InviteSessionStore(get()) }
    single { HomeFirstWinChecklistStore(get()) }
    single { HomeWeekPlanNudgeStore(get()) }
    single { GroceryGhostPairingDismissStore(get()) }
    single<ThemeManager> { SettingsThemeManager(get()) }
    single<GreetingRepository> { GreetingRepositoryImpl(get()) }
    single<NutritionRepository> { NutritionRepositoryImpl(get()) }
    single { NutritionSyncOutbox(get()) }
    single<NutritionSessionCoordinator> {
        val settings = get<com.russhwolf.settings.Settings>()
        val client = get<SupabaseClientHolder>().client
        NutritionSessionCoordinatorImpl.create(
            settings = settings,
            remoteApi = client?.let { NutritionRemoteApi(it) },
            outbox = get(),
            realtimeSync = client?.let { NutritionHouseholdRealtimeSync(it, get()) },
            logger = get(),
            diagnostics = get(),
        )
    }
    single<AiSettingsRemoteRepository> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) SupabaseAiSettingsRepository(client)
        else UnconfiguredAiSettingsRepository()
    }
    single<AiAssistantSettings>(createdAtStart = true) {
        SyncedAiAssistantSettings(
            local = SettingsAiAssistantSettings(
                settings = get(),
                compiledKey = AiSecrets.GEMINI_API_KEY,
            ),
            remote = get(),
            authRepository = get(),
            scope = get(),
            appLogger = get(),
        )
    }
    single<NutritionAiAssistantService> {
        val languageManager = get<LanguageManager>()
        val aiSettings = get<AiAssistantSettings>()
        val local = LocalNutritionAiAssistantService(
            currentLanguageCode = { languageManager.currentLanguage.value },
        )
        val keyProvider: () -> String = { aiSettings.geminiApiKey.value }
        val geminiApi: GeminiTextClient = GeminiApiClient(apiKeyProvider = keyProvider)
        RemoteNutritionAiAssistantService(
            local = local,
            geminiClient = GeminiDishIngredientClient(apiKeyProvider = keyProvider),
            geminiApi = geminiApi,
            currentLanguageCode = { languageManager.currentLanguage.value },
            aiSettings = aiSettings,
            appLogger = get(),
        )
    }
}

private val presentationModule = module {
    single { RegistrationData() }
    singleOf(::HomeScreenModel)
    singleOf(::LoginScreenModel)
    singleOf(::OnboardingScreenModel)
    singleOf(::HouseholdSetupScreenModel)
    singleOf(::JoinHouseholdScreenModel)
    singleOf(::InviteJoinFlowCoordinator)
    singleOf(::NutritionEntryScreenModel)
    singleOf(::HouseholdMembersEntryScreenModel)
    singleOf(::HouseholdMembersScreenModel)
    single {
        NutritionScreenModel(
            session = get(),
            householdRepository = get(),
            collaborationRepository = get(),
            aiAssistant = get(),
            ghostPairingDismissStore = get(),
            logger = get(),
        )
    }
}

/** Core modules without platform bindings; used by unit tests with a test platform module. */
internal fun coreKoinModules() = listOf(observabilityModule, domainModule, dataModule, presentationModule)

val appModule = module {
    includes(coreKoinModules())
    includes(platformModule())
    single {
        LocationLanguageSuggestionBootstrapper(
            settings = get(),
            languageManager = get(),
            deviceRegionService = get<DeviceRegionService>(),
            scope = get(),
        )
    }
}
