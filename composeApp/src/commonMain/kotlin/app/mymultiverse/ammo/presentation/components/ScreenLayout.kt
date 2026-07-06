package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.navigation.LocalMainTabBarVisible
import kotlinx.coroutines.launch

object ScreenLayout {
    val horizontalPadding = 24.dp
    val contentTopPadding = 20.dp
    val contentBottomPadding = 32.dp
    val listItemSpacing = 12.dp
    val sectionSpacing = 16.dp
    val inputBarVerticalPadding = 12.dp
    val inputBarHorizontalPadding = 16.dp
    val expandedMinWidth = 600.dp
    val expandedSidePanelWidth = 340.dp
}

/**
 * Reserves space for the IME and (when outside main tabs) the navigation bar.
 * Required on any screen with text input when [enableEdgeToEdge] is active.
 */
@Composable
fun Modifier.keyboardAwareInsets(): Modifier {
    val inMainTabs = LocalMainTabBarVisible.current
    return this
        .then(if (!inMainTabs) Modifier.navigationBarsPadding() else Modifier)
        .imePadding()
}

@Composable
fun Modifier.screenContentArea(scaffoldPadding: PaddingValues): Modifier {
    return fillMaxSize()
        .padding(scaffoldPadding)
        .padding(horizontal = ScreenLayout.horizontalPadding)
        .keyboardAwareInsets()
}

/** Scrollable form body with keyboard and navigation-bar insets applied. */
@Composable
fun Modifier.keyboardAwareScroll(
    scrollState: ScrollState = rememberScrollState(),
): Modifier = keyboardAwareInsets().verticalScroll(scrollState)

/**
 * Scrolls a focused text field into view inside a [keyboardAwareScroll] parent.
 * Call once per field via [rememberFieldScrollIntoViewModifier].
 */
@Composable
fun rememberFieldScrollIntoViewModifier(): Modifier {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    return Modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent { focusState ->
            if (focusState.isFocused) {
                scope.launch { bringIntoViewRequester.bringIntoView() }
            }
        }
}

/** Scrollable column for [AlertDialog] text slots so inputs stay above the IME. */
@Composable
fun KeyboardAwareDialogColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

@Composable
fun screenListPadding(extraBottom: Dp = ScreenLayout.contentBottomPadding): PaddingValues =
    PaddingValues(
        top = ScreenLayout.contentTopPadding,
        bottom = extraBottom,
    )
