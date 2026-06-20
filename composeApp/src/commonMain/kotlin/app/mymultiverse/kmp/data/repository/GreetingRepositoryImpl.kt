package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.home.HomeInspirationCatalog
import app.mymultiverse.kmp.domain.manager.LanguageManager
import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.repository.GreetingRepository
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.flow.first

class GreetingRepositoryImpl(
    private val languageManager: LanguageManager,
) : GreetingRepository {
    override suspend fun loadGreeting(): Greeting {
        val localeCode = languageManager.currentLanguage.first()
        return Greeting(text = HomeInspirationCatalog.pick(localeCode))
    }
}
