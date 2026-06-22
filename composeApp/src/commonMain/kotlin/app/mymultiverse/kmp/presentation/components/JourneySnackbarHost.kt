package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object JourneySnackbarDefaults {
    /** Visual height of [GroceryInputBar] content (field + vertical padding). */
    val stickyInputBarHeight = 80.dp
}

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
