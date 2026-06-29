package app.mymultiverse.ammo.data.repository

import app.mymultiverse.ammo.domain.home.HomeInspirationCatalog
import app.mymultiverse.ammo.domain.manager.LanguageManager
import app.mymultiverse.ammo.domain.model.Greeting
import app.mymultiverse.ammo.domain.repository.GreetingRepository
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
