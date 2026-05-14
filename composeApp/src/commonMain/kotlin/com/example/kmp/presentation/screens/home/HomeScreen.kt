package com.example.kmp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import com.example.kmp.presentation.screens.detail.DetailScreen

object HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<HomeScreenModel>()
        val greeting by screenModel.greeting.collectAsState()

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Home")
            when (val value = greeting) {
                null -> CircularProgressIndicator()
                else -> {
                    Text(text = value.text)
                    Button(
                        onClick = {
                            navigator.push(DetailScreen(message = value.text))
                        },
                    ) {
                        Text("Open detail")
                    }
                    Button(onClick = { screenModel.refresh() }) {
                        Text("Refresh greeting")
                    }
                }
            }
        }
    }
}
