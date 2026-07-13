package app.mymultiverse.ammo.presentation

import androidx.compose.runtime.Composable

/**
 * Platform hook that requests coarse-location permission on first launch when the
 * device locale is Italy, then triggers language bootstrapping.
 *
 * For all other locales — or on subsequent launches — the bootstrap runs
 * immediately without showing any permission dialog.
 */
@Composable
expect fun PlatformLocationPermissionSetup()
