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

    private val _journeys =
        MutableStateFlow(
            listOf(
                JourneyDreamUi(
                    id = "1",
                    title = "Healthy Family Life",
                    subtitle = "Nutrition & mindful meals together",
                    progress = 0.72f,
                    participantInitials = listOf("M", "A", "K"),
                ),
                JourneyDreamUi(
                    id = "2",
                    title = "Daily movement",
                    subtitle = "Steps, playtime, and stretch breaks",
                    progress = 0.45f,
                    participantInitials = listOf("M", "K"),
                ),
            ),
        )
    val journeys: StateFlow<List<JourneyDreamUi>> = _journeys.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            _greeting.value = getGreetingUseCase()
        }
    }
}
