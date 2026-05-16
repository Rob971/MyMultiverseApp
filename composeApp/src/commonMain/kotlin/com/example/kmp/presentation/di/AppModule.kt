package com.example.kmp.presentation.di

import com.example.kmp.database.AppDatabase
import com.example.kmp.database.DatabaseDriverFactory
import com.example.kmp.data.repository.GreetingRepositoryImpl
import com.example.kmp.data.repository.JourneyRepositoryImpl
import com.example.kmp.data.service.AiServiceImpl
import com.example.kmp.domain.repository.GreetingRepository
import com.example.kmp.domain.repository.JourneyRepository
import com.example.kmp.domain.service.AiService
import com.example.kmp.domain.usecase.*
import com.example.kmp.presentation.screens.home.HomeScreenModel
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
