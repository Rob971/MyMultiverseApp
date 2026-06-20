package app.mymultiverse.kmp.presentation.screens.home

import app.mymultiverse.kmp.domain.model.Greeting

internal sealed interface HomeInspirationLine {
    data object Loading : HomeInspirationLine

    data class Ready(val text: String) : HomeInspirationLine

    companion object {
        fun select(greeting: Greeting?): HomeInspirationLine =
            when (greeting) {
                null -> Loading
                else -> Ready(greeting.text)
            }
    }
}
