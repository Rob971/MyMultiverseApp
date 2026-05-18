package app.mymultiverse.kmp.data.repository

import app.mymultiverse.kmp.domain.model.Greeting
import app.mymultiverse.kmp.domain.repository.GreetingRepository

class GreetingRepositoryImpl : GreetingRepository {
    override suspend fun loadGreeting(): Greeting =
             Greeting(text = "Il cuore pulsante della nostra famiglia.")
}
