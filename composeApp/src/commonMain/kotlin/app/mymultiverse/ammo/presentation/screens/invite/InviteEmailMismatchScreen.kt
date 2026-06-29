package app.mymultiverse.ammo.presentation.screens.invite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.presentation.components.JourneyDestructiveOutlinedButton
import app.mymultiverse.ammo.presentation.components.ScreenLayout
import app.mymultiverse.ammo.presentation.components.AmmoRoundLogo
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import kmpvoyagercleanarchitecture.composeapp.generated.resources.Res
import kmpvoyagercleanarchitecture.composeapp.generated.resources.auth_pending_invites_email_mismatch
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_email_mismatch_title
import kmpvoyagercleanarchitecture.composeapp.generated.resources.invite_join_sign_out_retry
import org.jetbrains.compose.resources.stringResource

object InviteEmailMismatchTestTags {
    const val SCREEN = "invite_email_mismatch_screen"
    const val SIGN_OUT_RETRY_BUTTON = "invite_email_mismatch_sign_out_retry"
}

@Composable
fun InviteEmailMismatchScreen(
    invitedEmail: String,
    sessionEmail: String,
    onSignOutRetry: () -> Unit,
) {
    Scaffold(containerColor = androidx.compose.ui.graphics.Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenLayout.horizontalPadding)
                .testTag(InviteEmailMismatchTestTags.SCREEN),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AmmoRoundLogo(modifier = Modifier.size(88.dp))
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.invite_join_email_mismatch_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = JourneySemanticColors.inkDeep(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    Res.string.auth_pending_invites_email_mismatch,
                    invitedEmail,
                    sessionEmail,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = JourneySemanticColors.inkSecondary(),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            JourneyDestructiveOutlinedButton(
                onClick = onSignOutRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(InviteEmailMismatchTestTags.SIGN_OUT_RETRY_BUTTON),
            ) {
                Text(stringResource(Res.string.invite_join_sign_out_retry))
            }
        }
    }
}
