package app.mymultiverse.kmp.domain.repository

import app.mymultiverse.kmp.domain.model.Greeting

interface GreetingRepository {
    suspend fun loadGreeting(): Greeting
}
