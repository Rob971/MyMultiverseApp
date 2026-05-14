package com.example.kmp.presentation.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.kmp.domain.model.Greeting
import com.example.kmp.domain.usecase.GetGreetingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getGreetingUseCase: GetGreetingUseCase,
) : ScreenModel {
    private val _greeting = MutableStateFlow<Greeting?>(null)
    val greeting: StateFlow<Greeting?> = _greeting.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            _greeting.value = getGreetingUseCase()
        }
    }
}
