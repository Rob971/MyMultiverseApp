package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

@Composable
fun Modifier.screenContentArea(scaffoldPadding: PaddingValues): Modifier =
    fillMaxSize()
        .padding(scaffoldPadding)
        .padding(horizontal = ScreenLayout.horizontalPadding)
        .navigationBarsPadding()
        .imePadding()

fun screenListPadding(extraBottom: Dp = ScreenLayout.contentBottomPadding): PaddingValues =
    PaddingValues(
        top = ScreenLayout.contentTopPadding,
        bottom = extraBottom,
    )
