package com.mymultiverse.kmp.domain.repository

import com.mymultiverse.kmp.domain.model.Greeting

interface GreetingRepository {
    suspend fun loadGreeting(): Greeting
}
