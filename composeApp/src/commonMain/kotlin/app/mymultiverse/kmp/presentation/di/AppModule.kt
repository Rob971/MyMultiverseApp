package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.data.observability.AppLogger
import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteApi
import app.mymultiverse.kmp.data.repository.GreetingRepositoryImpl
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.data.invite.InviteSessionStore
import app.mymultiverse.kmp.data.repository.SettingsNutritionHouseholdSelectionStore
import app.mymultiverse.kmp.data.service.LocalNutritionAiAssistantService
import app.mymultiverse.kmp.data.supabase.SupabaseAuthRepository
import app.mymultiverse.kmp.data.supabase.SupabaseClientHolder
import app.mymultiverse.kmp.data.supabase.SupabaseHouseholdRepository
import app.mymultiverse.kmp.data.supabase.SupabaseHouseholdCollaborationRepository
import app.mymultiverse.kmp.data.supabase.UnconfiguredAuthRepository
import app.mymultiverse.kmp.data.supabase.UnconfiguredHouseholdRepository
import app.mymultiverse.kmp.data.supabase.UnconfiguredHouseholdCollaborationRepository
import app.mymultiverse.kmp.data.sync.NutritionSessionCoordinatorImpl
import app.mymultiverse.kmp.data.sync.NutritionHouseholdRealtimeSync
import app.mymultiverse.kmp.domain.observability.DiagnosticsContext
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import app.mymultiverse.kmp.domain.repository.HouseholdRepository
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.repository.NutritionSessionCoordinator
import app.mymultiverse.kmp.domain.repository.NutritionHouseholdSelectionStore
import app.mymultiverse.kmp.domain.repository.HouseholdCollaborationRepository
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
import app.mymultiverse.kmp.presentation.screens.auth.LoginScreenModel
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionEntryScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.kmp.presentation.screens.household.HouseholdMembersEntryScreenModel
import app.mymultiverse.kmp.presentation.screens.household.HouseholdMembersScreenModel
import app.mymultiverse.kmp.presentation.screens.invite.JoinHouseholdScreenModel
import app.mymultiverse.kmp.presentation.invite.InviteJoinFlowCoordinator
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
            SupabaseHouseholdRepository(client)
        } else {
            UnconfiguredHouseholdRepository()
        }
    }
    single<HouseholdCollaborationRepository> {
        val client = get<SupabaseClientHolder>().client
        if (client != null) {
            SupabaseHouseholdCollaborationRepository(client)
        } else {
            UnconfiguredHouseholdCollaborationRepository()
        }
    }
    single<NutritionHouseholdSelectionStore> { SettingsNutritionHouseholdSelectionStore(get()) }
    single { InviteSessionStore(get()) }
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
    single<NutritionAiAssistantService> { LocalNutritionAiAssistantService() }
}

private val presentationModule = module {
    singleOf(::HomeScreenModel)
    singleOf(::LoginScreenModel)
    singleOf(::JoinHouseholdScreenModel)
    singleOf(::InviteJoinFlowCoordinator)
    singleOf(::NutritionEntryScreenModel)
    singleOf(::HouseholdMembersEntryScreenModel)
    singleOf(::HouseholdMembersScreenModel)
    single {
        NutritionScreenModel(
            session = get(),
            householdRepository = get(),
            aiAssistant = get(),
        )
    }
}

/** Core modules without platform bindings; used by unit tests with a test platform module. */
internal fun coreKoinModules() = listOf(observabilityModule, domainModule, dataModule, presentationModule)

val appModule = module {
    includes(coreKoinModules())
    includes(platformModule())
}
