package app.mymultiverse.kmp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

class AppNavigator internal constructor(
    private val backStack: SnapshotStateList<AppRoute>,
) {
    val current: AppRoute
        get() = backStack.last()

    val canGoBack: Boolean
        get() = backStack.size > 1

    fun navigateTo(route: AppRoute) {
        if (backStack.last() == route) return
        backStack.add(route)
    }

    /** Replaces the visible destination without growing the stack (e.g. gate → resolved screen). */
    fun replaceCurrent(route: AppRoute) {
        if (backStack.isEmpty()) {
            backStack.add(route)
            return
        }
        if (backStack.last() == route) return
        backStack[backStack.lastIndex] = route
    }

    fun navigateBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}

@Composable
fun rememberAppNavigator(
    startDestination: AppRoute = AppRoute.Home,
): AppNavigator {
    val backStack = remember { mutableStateListOf(startDestination) }
    return remember { AppNavigator(backStack) }
}
