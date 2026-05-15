package com.example.kmp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.domain.model.Greeting
import com.example.kmp.presentation.components.JourneyBanner
import com.example.kmp.presentation.components.JourneyDreamCard
import com.example.kmp.presentation.screens.calendar.CalendarScreen
import com.example.kmp.presentation.screens.calendar.CalendarScope
import com.example.kmp.presentation.screens.detail.DetailScreen
import com.example.kmp.presentation.theme.SharedJourneyColors
import org.jetbrains.compose.ui.tooling.preview.Preview

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<HomeScreenModel>()
        val greeting by screenModel.greeting.collectAsState()
        val journeys by screenModel.journeys.collectAsState()

        HomeContent(
            greeting = greeting,
            journeys = journeys,
            onJourneyClick = { dream ->
                navigator.push(DetailScreen(journeyId = dream.id))
            },
            onRefreshClick = {
                screenModel.refresh()
            },
            onGlobalCalendarClick = {
                navigator.push(CalendarScreen(CalendarScope.Global))
            }
        )
    }
}

@Composable
fun HomeContent(
    greeting: Greeting?,
    journeys: List<JourneyDreamUi>,
    onJourneyClick: (JourneyDreamUi) -> Unit,
    onRefreshClick: () -> Unit,
    onGlobalCalendarClick: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .safeDrawingPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                JourneyBanner(
                    headline = "SHARED JOURNEY",
                    supportingLine =
                        when (greeting) {
                            null -> "Loading your family space…"
                            else -> greeting.text
                        },
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
                            color = SharedJourneyColors.Terracotta,
                        )
                    }
                }
            }

            itemsIndexed(journeys) { index, dream ->
                JourneyDreamCard(
                    dream = dream,
                    progressColor =
                        if (index == 0) {
                            SharedJourneyColors.Terracotta
                        } else {
                            SharedJourneyColors.SageMuted
                        },
                    onClick = { onJourneyClick(dream) },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    TextButton(onClick = onRefreshClick) {
                        Text(
                            text = "Refresh family greeting",
                            style = MaterialTheme.typography.labelLarge,
                            color = SharedJourneyColors.Terracotta,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeContentPreview() {
    val mockJourneys = listOf(
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
    )
    MaterialTheme {
        HomeContent(
            greeting = Greeting("Hello from Preview!"),
            journeys = mockJourneys,
            onJourneyClick = {},
            onRefreshClick = {},
            onGlobalCalendarClick = {}
        )
    }
}
