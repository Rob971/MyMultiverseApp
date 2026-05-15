package com.example.kmp.presentation.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.kmp.domain.model.Greeting
import com.example.kmp.domain.model.Journey
import com.example.kmp.domain.model.JourneyTask
import com.example.kmp.domain.usecase.GetGreetingUseCase
import com.example.kmp.domain.usecase.GetJourneysUseCase
import com.example.kmp.domain.usecase.UpsertJourneyUseCase
import com.example.kmp.domain.usecase.DeleteJourneyUseCase
import com.example.kmp.domain.usecase.ToggleTaskUseCase
import com.example.kmp.domain.usecase.CheerTaskUseCase
import com.example.kmp.domain.usecase.AddTaskUseCase
import com.example.kmp.domain.usecase.UpdateTaskUseCase
import com.example.kmp.domain.usecase.DeleteTaskUseCase
import com.example.kmp.domain.usecase.RefreshJourneysUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val getGreetingUseCase: GetGreetingUseCase,
    private val getJourneysUseCase: GetJourneysUseCase,
    private val upsertJourneyUseCase: UpsertJourneyUseCase,
    private val deleteJourneyUseCase: DeleteJourneyUseCase,
    private val toggleTaskUseCase: ToggleTaskUseCase,
    private val cheerTaskUseCase: CheerTaskUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
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

    fun addJourney(journey: Journey) {
        screenModelScope.launch {
            upsertJourneyUseCase(journey)
        }
    }

    fun deleteJourney(id: String) {
        screenModelScope.launch {
            deleteJourneyUseCase(id)
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

    fun addTask(journeyId: String, task: JourneyTask) {
        screenModelScope.launch {
            addTaskUseCase(journeyId, task)
        }
    }

    fun updateTask(task: JourneyTask) {
        screenModelScope.launch {
            updateTaskUseCase(task)
        }
    }

    fun deleteTask(journeyId: String, taskId: String) {
        screenModelScope.launch {
            deleteTaskUseCase(journeyId, taskId)
        }
    }
}
