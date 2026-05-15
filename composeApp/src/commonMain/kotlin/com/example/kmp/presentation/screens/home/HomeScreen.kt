package com.example.kmp.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.Greeting
import com.example.kmp.domain.model.Journey
import com.example.kmp.presentation.components.JourneyBanner
import com.example.kmp.presentation.components.JourneyDreamCard
import com.example.kmp.presentation.components.NapolitanBackground
import com.example.kmp.presentation.screens.calendar.CalendarScreen
import com.example.kmp.presentation.screens.calendar.CalendarScope
import com.example.kmp.presentation.screens.detail.DetailScreen
import com.example.kmp.presentation.theme.SharedJourneyColors

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<HomeScreenModel>()

        val greeting by screenModel.greeting.collectAsState()
        val journeys by screenModel.journeys.collectAsState()

        NapolitanBackground {
            HomeContent(
                greeting = greeting,
                journeys = journeys,
                onJourneyClick = { dream ->
                    navigator.push(DetailScreen(journeyId = dream.id))
                },
                onAddJourneyClick = {
                    navigator.push(JourneyEditScreen())
                },
                onEditJourneyClick = { journeyId ->
                    navigator.push(JourneyEditScreen(journeyId = journeyId))
                },
                onDeleteJourneyClick = { journeyId ->
                    screenModel.deleteJourney(journeyId)
                },
                onRefreshClick = {
                    screenModel.refresh()
                },
                onGlobalCalendarClick = {
                    navigator.push(CalendarScreen(CalendarScope.Global))
                },
                onTaskToggle = { jId, tId -> screenModel.toggleTask(jId, tId) },
                onTaskAdd = { jId, task -> screenModel.addTask(jId, task) },
                onTaskUpdate = { task -> screenModel.updateTask(task) },
                onTaskDelete = { jId, tId -> screenModel.deleteTask(jId, tId) }
            )
        }
    }
}

@Composable
fun HomeContent(
    greeting: Greeting?,
    journeys: List<Journey>,
    onJourneyClick: (Journey) -> Unit,
    onAddJourneyClick: () -> Unit,
    onEditJourneyClick: (String) -> Unit,
    onDeleteJourneyClick: (String) -> Unit,
    onRefreshClick: () -> Unit,
    onGlobalCalendarClick: () -> Unit,
    onTaskToggle: (String, String) -> Unit,
    onTaskAdd: (String, com.example.kmp.domain.model.JourneyTask) -> Unit,
    onTaskUpdate: (com.example.kmp.domain.model.JourneyTask) -> Unit,
    onTaskDelete: (String, String) -> Unit
) {
    Scaffold(
        topBar = { },
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddJourneyClick,
                containerColor = SharedJourneyColors.TerracottaOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Dream")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .safeDrawingPadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                JourneyBanner(
                    headline = "BENVENUTI A CASA",
                    supportingLine =
                        when (greeting) {
                            null -> "Caricando il tuo spazio famiglia..."
                            else -> greeting.text
                        },
                    description = "Uno spazio condiviso per coltivare i sogni della nostra famiglia, tracciare obiettivi S.M.A.R.T. e rafforzare i nostri legami quotidiani.",
                    onCalendarClick = onGlobalCalendarClick
                )
            }

            item {
                if (greeting == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = SharedJourneyColors.MediterraneanTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "I Nostri Sogni",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SharedJourneyColors.InkDeep,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            itemsIndexed(journeys) { _, dream ->
                JourneyDreamCard(
                    dream = dream,
                    progressColor = when (dream.id) {
                        "vesuvian-vitality" -> SharedJourneyColors.TerracottaOrange
                        "financial-masterplan" -> SharedJourneyColors.LemonZestYellow
                        "motore-unita" -> SharedJourneyColors.MediterraneanTeal
                        else -> SharedJourneyColors.TerracottaOrange
                    },
                    onClick = { onJourneyClick(dream) },
                    onEditClick = { onEditJourneyClick(dream.id) },
                    onDeleteClick = { onDeleteJourneyClick(dream.id) },
                    onTaskToggle = onTaskToggle,
                    onTaskAdd = onTaskAdd,
                    onTaskUpdate = onTaskUpdate,
                    onTaskDelete = onTaskDelete
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onRefreshClick) {
                        Text(
                            "Aggiorna Ispirazioni",
                            style = MaterialTheme.typography.labelMedium,
                            color = SharedJourneyColors.InkMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
