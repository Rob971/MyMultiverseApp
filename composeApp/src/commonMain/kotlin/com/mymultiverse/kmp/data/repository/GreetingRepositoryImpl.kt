package com.mymultiverse.kmp.data.repository

import com.mymultiverse.kmp.domain.model.Greeting
import com.mymultiverse.kmp.domain.repository.GreetingRepository

class GreetingRepositoryImpl : GreetingRepository {
    override suspend fun loadGreeting(): Greeting =
             Greeting(text = "Il cuore pulsante della nostra famiglia.")
}
