package app.mymultiverse.kmp.presentation.theme

import kotlin.test.Test
import kotlin.test.assertNotEquals

class AppIconsTest {

    @Test
    fun semanticIcons_doNotAliasSparklesOrWrongChevrons() {
        assertNotEquals(AppIcons.Sparkles.name, AppIcons.MoreVert.name)
        assertNotEquals(AppIcons.Sparkles.name, AppIcons.Refresh.name)
        assertNotEquals(AppIcons.Notifications.name, AppIcons.DateRange.name)
        assertNotEquals(AppIcons.ChevronRight.name, AppIcons.KeyboardArrowDown.name)
        assertNotEquals(AppIcons.ChevronLeft.name, AppIcons.KeyboardArrowUp.name)
        assertNotEquals(AppIcons.CheckCircle.name, AppIcons.Check.name)
    }
}
