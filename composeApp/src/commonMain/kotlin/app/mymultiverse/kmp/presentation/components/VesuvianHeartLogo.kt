package app.mymultiverse.kmp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.ammo_round_logo
import org.jetbrains.compose.resources.painterResource

/**
 * Round Ammò disc logo (terracotta seal with gold wordmark).
 */
@Composable
fun VesuvianHeartLogo(modifier: Modifier = Modifier.size(64.dp)) {
    Image(
        painter = painterResource(Res.drawable.ammo_round_logo),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
