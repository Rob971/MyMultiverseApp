package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object JourneySnackbarDefaults {
    /** Visual height of [GroceryInputBar] content (field + vertical padding). */
    val stickyInputBarHeight = 80.dp

    /** Auto-dismiss action snackbars (e.g. undo delete) unless the user taps the action. */
    val actionSnackbarDuration = SnackbarDuration.Short
}

suspend fun SnackbarHostState.showJourneyActionSnackbar(
    message: String,
    actionLabel: String,
    duration: SnackbarDuration = JourneySnackbarDefaults.actionSnackbarDuration,
): SnackbarResult = showSnackbar(
    message = message,
    actionLabel = actionLabel,
    duration = duration,
)

@Composable
fun JourneySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    aboveBottomBar: Boolean = false,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(
            bottom = if (aboveBottomBar) JourneySnackbarDefaults.stickyInputBarHeight else 0.dp,
        ),
    )
}
