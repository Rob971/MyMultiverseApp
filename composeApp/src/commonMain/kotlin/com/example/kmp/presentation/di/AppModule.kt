package com.example.kmp.presentation.di

import com.example.kmp.data.repository.GreetingRepositoryImpl
import com.example.kmp.domain.repository.GreetingRepository
import com.example.kmp.domain.usecase.GetGreetingUseCase
import com.example.kmp.presentation.screens.home.HomeScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

private val domainModule = module {
    factoryOf(::GetGreetingUseCase)
}

private val dataModule = module {
    single<GreetingRepository> { GreetingRepositoryImpl() }
}

private val presentationModule = module {
    factoryOf(::HomeScreenModel)
}

val appModule = module {
    includes(domainModule, dataModule, presentationModule)
}
