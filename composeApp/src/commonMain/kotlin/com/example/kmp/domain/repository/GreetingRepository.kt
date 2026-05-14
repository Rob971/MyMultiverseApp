package com.example.kmp.domain.repository

import com.example.kmp.domain.model.Greeting

interface GreetingRepository {
    suspend fun loadGreeting(): Greeting
}
