package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.ammo_round_logo
import ammo.composeapp.generated.resources.app_brand_name
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Round Ammò disc logo (gold-framed terracotta seal with wordmark).
 */
@Composable
fun AmmoRoundLogo(modifier: Modifier = Modifier.size(64.dp)) {
    Image(
        painter = painterResource(Res.drawable.ammo_round_logo),
        contentDescription = stringResource(Res.string.app_brand_name),
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

/** @deprecated Use [AmmoRoundLogo] — kept for incremental migration. */
@Composable
fun VesuvianHeartLogo(modifier: Modifier = Modifier) = AmmoRoundLogo(modifier = modifier)
