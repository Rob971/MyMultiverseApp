package app.mymultiverse.ammo.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import kotlinx.coroutines.flow.StateFlow

internal object InstrumentedComposeTest {
    const val DEFAULT_TIMEOUT_MS = 5_000L

    fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.waitFor(
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
        condition: () -> Boolean,
    ) {
        waitUntil(timeoutMillis) { condition() }
    }

    fun <A : ComponentActivity, T> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.waitForState(
        flow: StateFlow<T>,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
        predicate: (T) -> Boolean,
    ) {
        waitUntil(timeoutMillis) { predicate(flow.value) }
    }
}
