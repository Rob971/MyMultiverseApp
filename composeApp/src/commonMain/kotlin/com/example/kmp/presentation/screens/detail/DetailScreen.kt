package com.example.kmp.presentation.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

data class DetailScreen(
    private val message: String,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Detail")
            Text(text = message)
            Button(onClick = { navigator.pop() }) {
                Text("Back")
            }
        }
    }
}
