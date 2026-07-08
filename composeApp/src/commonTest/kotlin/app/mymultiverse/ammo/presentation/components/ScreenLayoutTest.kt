package app.mymultiverse.ammo.presentation.components

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenLayoutTest {

    @Test
    fun isWideWidth_matchesTabletBreakpoint() {
        assertFalse(ScreenLayout.isWideWidth(ScreenLayout.expandedMinWidth - 1.dp))
        assertTrue(ScreenLayout.isWideWidth(ScreenLayout.expandedMinWidth))
        assertTrue(ScreenLayout.isWideWidth(ScreenLayout.expandedMinWidth + 100.dp))
    }
}
