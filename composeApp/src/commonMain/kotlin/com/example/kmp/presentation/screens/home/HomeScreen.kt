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
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.kmp.presentation.components.JourneyBanner
import com.example.kmp.presentation.components.JourneyDreamCard
import com.example.kmp.presentation.screens.detail.DetailScreen
import com.example.kmp.presentation.theme.SharedJourneyColors

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<HomeScreenModel>()
        val greeting by screenModel.greeting.collectAsState()
        val journeys by screenModel.journeys.collectAsState()

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
                            when (val g = greeting) {
                                null -> "Loading your family space…"
                                else -> g.text
                            },
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
                        onClick = {
                            navigator.push(
                                DetailScreen(
                                    message = "${dream.title} — ${dream.subtitle}",
                                ),
                            )
                        },
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        TextButton(onClick = { screenModel.refresh() }) {
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
}
