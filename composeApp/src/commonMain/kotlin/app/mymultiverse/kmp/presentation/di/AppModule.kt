package app.mymultiverse.kmp.presentation.di

import app.mymultiverse.kmp.data.repository.GreetingRepositoryImpl
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import app.mymultiverse.kmp.domain.usecase.GetGreetingUseCase
import app.mymultiverse.kmp.presentation.screens.home.HomeScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private val domainModule = module {
    factoryOf(::GetGreetingUseCase)
}

private val dataModule = module {
    single<GreetingRepository> { GreetingRepositoryImpl() }
}

private val presentationModule = module {
    singleOf(::HomeScreenModel)
}

val appModule = module {
    includes(domainModule, dataModule, presentationModule, platformModule())
}
