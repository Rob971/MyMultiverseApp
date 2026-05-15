package com.example.kmp.presentation.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.kmp.domain.model.Greeting
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.usecase.GetGreetingUseCase
import com.example.kmp.domain.usecase.GetJourneysUseCase
import com.example.kmp.domain.usecase.ToggleTaskUseCase
import com.example.kmp.domain.usecase.CheerTaskUseCase
import com.example.kmp.domain.usecase.RefreshJourneysUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getGreetingUseCase: GetGreetingUseCase,
    private val getJourneysUseCase: GetJourneysUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val cheerTaskUseCase: CheerTaskUseCase,
    private val refreshJourneysUseCase: RefreshJourneysUseCase,
) : ScreenModel {
    private val _greeting = MutableStateFlow<Greeting?>(null)
    val greeting: StateFlow<Greeting?> = _greeting.asStateFlow()

    val journeys: StateFlow<List<Journey>> = getJourneysUseCase()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            _greeting.value = getGreetingUseCase()
            refreshJourneysUseCase()
        }
    }

    fun toggleTask(journeyId: String, taskId: String) {
        screenModelScope.launch {
            toggleTaskUseCase(journeyId, taskId)
        }
    }

    fun cheerTask(journeyId: String, taskId: String) {
        screenModelScope.launch {
            cheerTaskUseCase(journeyId, taskId)
        }
    }
}
