package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.data.repository.GreetingRepositoryImpl
import app.mymultiverse.kmp.data.repository.NutritionRepositoryHolder
import app.mymultiverse.kmp.data.repository.NutritionRepositoryImpl
import app.mymultiverse.kmp.data.repository.SettingsNutritionSpaceSelectionStore
import app.mymultiverse.kmp.data.service.LocalNutritionAiAssistantService
import app.mymultiverse.kmp.data.supabase.NutritionSpaceRealtimeSync
import app.mymultiverse.kmp.data.supabase.SupabaseAuthRepository
import app.mymultiverse.kmp.data.supabase.SupabaseClientFactory
import app.mymultiverse.kmp.data.supabase.SupabaseSharingSpaceRepository
import app.mymultiverse.kmp.data.supabase.SupabaseSpaceCollaborationRepository
import app.mymultiverse.kmp.data.supabase.SyncingNutritionRepository
import app.mymultiverse.kmp.data.supabase.UnconfiguredAuthRepository
import app.mymultiverse.kmp.data.supabase.UnconfiguredSharingSpaceRepository
import app.mymultiverse.kmp.data.supabase.UnconfiguredSpaceCollaborationRepository
import app.mymultiverse.kmp.domain.repository.AuthRepository
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import app.mymultiverse.kmp.domain.repository.NutritionRepository
import app.mymultiverse.kmp.domain.repository.NutritionSpaceSelectionStore
import app.mymultiverse.kmp.domain.repository.SharingSpaceRepository
import app.mymultiverse.kmp.domain.repository.SpaceCollaborationRepository
import app.mymultiverse.kmp.domain.service.NutritionAiAssistantService
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
import app.mymultiverse.kmp.presentation.screens.auth.LoginScreenModel
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.NutritionScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.spaces.NutritionSpaceMembersScreenModel
import app.mymultiverse.kmp.presentation.screens.nutrition.spaces.NutritionSpacesScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private val domainModule = module {
    factoryOf(::GetGreetingUseCase)
}

private val dataModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }
    single<AuthRepository> {
        val client = SupabaseClientFactory.createOrNull()
        if (client != null) {
            SupabaseAuthRepository(client, get())
        } else {
            UnconfiguredAuthRepository()
        }
    }
    single<SharingSpaceRepository> {
        val client = SupabaseClientFactory.createOrNull()
        if (client != null) {
            SupabaseSharingSpaceRepository(client)
        } else {
            UnconfiguredSharingSpaceRepository()
        }
    }
    single<SpaceCollaborationRepository> {
        val client = SupabaseClientFactory.createOrNull()
        if (client != null) {
            SupabaseSpaceCollaborationRepository(client)
        } else {
            UnconfiguredSpaceCollaborationRepository()
        }
    }
    single<NutritionSpaceSelectionStore> { SettingsNutritionSpaceSelectionStore(get()) }
    single<GreetingRepository> { GreetingRepositoryImpl() }
    single<NutritionRepository> { NutritionRepositoryImpl(get()) }
    single {
        val settings = get<com.russhwolf.settings.Settings>()
        val client = SupabaseClientFactory.createOrNull()
        val realtimeSync = client?.let { NutritionSpaceRealtimeSync(it, get()) }
        NutritionRepositoryHolder(
            localFallback = NutritionRepositoryImpl(settings),
            remoteFactory = { spaceId ->
                if (client != null) {
                    SyncingNutritionRepository(spaceId = spaceId, client = client, settings = settings)
                } else {
                    NutritionRepositoryImpl(settings)
                }
            },
            realtimeSync = realtimeSync,
        )
    }
    single<NutritionAiAssistantService> { LocalNutritionAiAssistantService() }
}

private val presentationModule = module {
    singleOf(::HomeScreenModel)
    singleOf(::LoginScreenModel)
    singleOf(::NutritionSpacesScreenModel)
    singleOf(::NutritionSpaceMembersScreenModel)
    single {
        NutritionScreenModel(
            repositoryHolder = get(),
            aiAssistant = get(),
        )
    }
}

/** Core modules without platform bindings; used by unit tests with a test platform module. */
internal fun coreKoinModules() = listOf(domainModule, dataModule, presentationModule)

val appModule = module {
    includes(coreKoinModules())
    includes(platformModule())
}
