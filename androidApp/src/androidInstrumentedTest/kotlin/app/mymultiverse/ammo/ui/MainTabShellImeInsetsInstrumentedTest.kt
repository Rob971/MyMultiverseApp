package app.mymultiverse.ammo.ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.mymultiverse.ammo.presentation.components.GroceryInputBar
import app.mymultiverse.ammo.presentation.components.GroceryInputBarTestTags
import app.mymultiverse.ammo.presentation.components.NutritionScaffold
import app.mymultiverse.ammo.presentation.navigation.AppMainTab
import app.mymultiverse.ammo.presentation.navigation.MainTabShell
import app.mymultiverse.ammo.presentation.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Keyboard and navigation-bar insets for a [NutritionScaffold] nested in [MainTabShell].
 *
 * The shell reserves its tab bar as content padding; if it does not also consume those insets,
 * the nested `imePadding()` counts the tab bar twice and leaves an empty band above the keyboard.
 * Insets are injected straight into the Compose view (as androidx's own WindowInsetsPaddingTest
 * does), so no real keyboard or emulator setting is needed.
 */
@RunWith(AndroidJUnit4::class)
class MainTabShellImeInsetsInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var composeView: View

    @Test
    fun keyboardOpen_nutritionBottomBarSitsOnKeyboardTop() {
        setTabContent {
            Box(Modifier.fillMaxWidth().height(56.dp).testTag(BOTTOM_BAR))
        }
        val imePx = with(composeRule.density) { 320.dp.roundToPx() }
        sendInsets(imeBottom = imePx, navigationBottom = navigationBarPx())

        val barBottom = composeRule.onNodeWithTag(BOTTOM_BAR).fetchSemanticsNode().boundsInRoot.bottom
        assertEquals(
            "bottom bar must end at the keyboard top, not a tab-bar height above it",
            (composeView.height - imePx).toFloat(),
            barBottom,
            1f,
        )
    }

    @Test
    fun keyboardClosed_groceryInputBarAddsNoNavigationBarBand() {
        setTabContent {
            GroceryInputBar(
                value = "",
                onValueChange = {},
                placeholder = "Add an item",
                addContentDescription = "Add",
                onSubmit = {},
                embeddedInMainTabs = true,
            )
        }
        sendInsets(imeBottom = 0, navigationBottom = 0)
        val heightWithoutNavigationBar = stickyBarHeight()

        sendInsets(imeBottom = 0, navigationBottom = navigationBarPx())

        assertEquals(
            "the tab bar already covers the navigation bar; the input bar must not pad for it again",
            heightWithoutNavigationBar,
            stickyBarHeight(),
            1f,
        )
    }

    private fun setTabContent(bottomBar: @Composable () -> Unit) {
        composeRule.setKoinContent {
            composeView = LocalView.current
            AppTheme {
                MainTabShell(
                    selectedTab = AppMainTab.Grocery,
                    onTabSelected = {},
                    showBottomBar = true,
                    modifier = Modifier.fillMaxSize(),
                ) { contentModifier ->
                    NutritionScaffold(
                        title = "Grocery",
                        onBack = {},
                        modifier = contentModifier,
                        showBackButton = false,
                        showGlobalLanguageAction = false,
                        bottomBar = bottomBar,
                    ) {}
                }
            }
        }
    }

    private fun sendInsets(imeBottom: Int, navigationBottom: Int) {
        composeRule.runOnIdle {
            val insets = WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, imeBottom))
                .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navigationBottom))
                .setVisible(WindowInsetsCompat.Type.ime(), imeBottom > 0)
                .build()
            composeView.dispatchApplyWindowInsets(insets.toWindowInsets()!!)
        }
        composeRule.waitForIdle()
    }

    private fun navigationBarPx(): Int = with(composeRule.density) { 48.dp.roundToPx() }

    private fun stickyBarHeight(): Float =
        composeRule.onNodeWithTag(GroceryInputBarTestTags.STICKY_BAR).fetchSemanticsNode().boundsInRoot.height

    private companion object {
        const val BOTTOM_BAR = "main_tab_ime_test_bottom_bar"
    }
}
