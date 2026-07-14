package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.navigation.LocalMainTabBarVisible

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
    val formMaxWidth = 480.dp

    fun isWideWidth(width: Dp): Boolean = width >= expandedMinWidth
}

/**
 * Centers narrow form content on tablet-width layouts while keeping phone layouts full width.
 */
@Composable
fun AdaptiveFormWidth(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val formModifier = if (ScreenLayout.isWideWidth(maxWidth)) {
            Modifier.widthIn(max = ScreenLayout.formMaxWidth)
        } else {
            Modifier.fillMaxWidth()
        }
        Box(modifier = formModifier) {
            content()
        }
    }
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

/**
 * Like [screenContentArea] but without [imePadding] on the outer container. Use when the
 * scrollable child (e.g. a [LazyColumn]) carries [imePadding] itself so the container does not
 * resize on every keyboard frame, avoiding scroll-position jumps for top-positioned text fields.
 */
@Composable
fun Modifier.screenContentAreaScrollable(scaffoldPadding: PaddingValues): Modifier {
    val inMainTabs = LocalMainTabBarVisible.current
    return fillMaxSize()
        .padding(scaffoldPadding)
        .padding(horizontal = ScreenLayout.horizontalPadding)
        .then(if (!inMainTabs) Modifier.navigationBarsPadding() else Modifier)
}

/** Scrollable form body with keyboard and navigation-bar insets applied. */
@Composable
fun Modifier.keyboardAwareScroll(
    scrollState: ScrollState = rememberScrollState(),
): Modifier = keyboardAwareInsets().verticalScroll(scrollState)

/**
 * Scrolls a focused text field into view inside a [keyboardAwareScroll] or [LazyColumn] parent.
 *
 * The naive approach of calling [BringIntoViewRequester.bringIntoView] once on [onFocusEvent]
 * has a timing gap: focus fires before the keyboard has animated in, so the field appears
 * on-screen at that moment and no scroll occurs. Then [NutritionScaffold]'s `imePadding()` shrinks
 * the layout as the keyboard rises, covering the bottom of the content — with no follow-up scroll.
 *
 * Fix: track both focus state and the current IME bottom inset. [LaunchedEffect] re-runs whenever
 * [imeBottom] changes (i.e. every frame the keyboard animates), calling [bringIntoViewRequester]
 * each time so the field stays visible throughout the animation.
 */
@Composable
fun rememberFieldScrollIntoViewModifier(): Modifier {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)

    LaunchedEffect(isFocused, imeBottom) {
        if (isFocused) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    return Modifier
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusEvent { focusState ->
            isFocused = focusState.isFocused
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
