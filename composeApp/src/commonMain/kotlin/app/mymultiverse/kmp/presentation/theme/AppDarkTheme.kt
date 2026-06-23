package app.mymultiverse.kmp.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAppDarkTheme = compositionLocalOf { false }

/**
 * Option 1 — deep slate charcoal base with mint primary and warm-gold secondary accent rings.
 */
fun appDarkColorScheme() = darkColorScheme(
    primary = SharedJourneyColors.AccentMintTeal,
    onPrimary = SharedJourneyColors.DarkBackground,
    primaryContainer = SharedJourneyColors.AccentMintContainer,
    onPrimaryContainer = SharedJourneyColors.AccentMintTeal,
    secondary = SharedJourneyColors.AccentWarmGold,
    onSecondary = SharedJourneyColors.DarkBackground,
    secondaryContainer = SharedJourneyColors.AccentWarmGoldContainer,
    onSecondaryContainer = SharedJourneyColors.AccentWarmGold,
    tertiary = SharedJourneyColors.TerracottaOrange,
    onTertiary = Color.White,
    background = SharedJourneyColors.DarkBackground,
    onBackground = SharedJourneyColors.DarkTextPrimary,
    surface = SharedJourneyColors.DarkSurfaceCard,
    onSurface = SharedJourneyColors.DarkTextPrimary,
    surfaceContainer = SharedJourneyColors.DarkBackground,
    surfaceContainerHigh = SharedJourneyColors.DarkSurfaceCard,
    surfaceContainerHighest = SharedJourneyColors.DarkSurfaceElevated,
    onSurfaceVariant = SharedJourneyColors.DarkTextMuted,
    outline = SharedJourneyColors.DarkOutline,
    outlineVariant = SharedJourneyColors.DarkOutline.copy(alpha = 0.6f),
    error = SharedJourneyColors.TerracottaOrange,
    onError = Color.White,
)

@Composable
fun ProvideAppDarkTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppDarkTheme provides darkTheme, content = content)
}

@Composable
fun isAppInDarkTheme(): Boolean = LocalAppDarkTheme.current
