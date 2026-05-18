package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.database.AppDatabase
import app.mymultiverse.kmp.database.DatabaseDriverFactory
import app.mymultiverse.kmp.data.repository.GreetingRepositoryImpl
import app.mymultiverse.kmp.data.repository.JourneyRepositoryImpl
import app.mymultiverse.kmp.data.service.AiServiceImpl
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import app.mymultiverse.kmp.domain.repository.JourneyRepository
import app.mymultiverse.kmp.domain.service.AiService
import app.mymultiverse.kmp.domain.usecase.*
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

private val domainModule = module {
    factoryOf(::GetGreetingUseCase)
    factoryOf(::GetJourneysUseCase)
    factoryOf(::UpsertJourneyUseCase)
    factoryOf(::DeleteJourneyUseCase)
    factoryOf(::ToggleTaskUseCase)
    factoryOf(::CheerTaskUseCase)
    factoryOf(::AddTaskUseCase)
    factoryOf(::UpdateTaskUseCase)
    factoryOf(::DeleteTaskUseCase)
    factoryOf(::AddFinanceBillEntryUseCase)
    factoryOf(::RefreshJourneysUseCase)
}

private val dataModule = module {
    single<GreetingRepository> { GreetingRepositoryImpl() }
    single<JourneyRepository> { JourneyRepositoryImpl(get()) }
    single<AiService> { AiServiceImpl() }
    
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        AppDatabase(driver)
    }
}

private val presentationModule = module {
    factoryOf(::HomeScreenModel)
}

val appModule = module {
    includes(domainModule, dataModule, presentationModule, platformModule())
}
