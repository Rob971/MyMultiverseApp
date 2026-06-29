package app.mymultiverse.ammo.domain.repository

import app.mymultiverse.ammo.domain.model.Greeting

interface GreetingRepository {
    suspend fun loadGreeting(): Greeting
}
