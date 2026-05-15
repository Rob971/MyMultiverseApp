package com.example.kmp.data.repository

import com.example.kmp.domain.model.Greeting
import com.example.kmp.domain.repository.GreetingRepository

class GreetingRepositoryImpl : GreetingRepository {
    override suspend fun loadGreeting(): Greeting =
             Greeting(text = "Il cuore pulsante della nostra famiglia.")
}
