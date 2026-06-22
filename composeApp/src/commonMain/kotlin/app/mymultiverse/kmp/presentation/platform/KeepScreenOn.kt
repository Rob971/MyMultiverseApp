package app.mymultiverse.kmp.presentation.platform

import androidx.compose.runtime.Composable

/**
 * Keeps the device screen awake while [enabled] (e.g. grocery shopping list visible).
 */
@Composable
expect fun KeepScreenOn(enabled: Boolean)
