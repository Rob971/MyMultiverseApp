package app.mymultiverse.kmp.presentation

import androidx.compose.runtime.Composable

/**
 * Platform hook for push notification permission (Android) and related setup.
 */
@Composable
expect fun PlatformPushSetup()
