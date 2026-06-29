package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.domain.model.sharing.HouseholdInvite
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.auth_pending_invites_accept
import ammo.composeapp.generated.resources.auth_pending_invites_decline
import ammo.composeapp.generated.resources.auth_pending_invites_subtitle
import ammo.composeapp.generated.resources.auth_pending_invites_title
import org.jetbrains.compose.resources.stringResource

object PendingInvitesTestTags {
    const val CARD = "pending_invites_card"
    const val ACCEPT_BUTTON = "pending_invites_accept"
    const val DECLINE_BUTTON = "pending_invites_decline"
}

@Composable
fun PendingInvitesCard(
    invites: List<HouseholdInvite>,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (invites.isEmpty()) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(PendingInvitesTestTags.CARD),
        color = SharedJourneyColors.TerracottaOrange.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.auth_pending_invites_title),
                style = MaterialTheme.typography.titleSmall,
                color = JourneySemanticColors.inkDeep(),
            )
            invites.forEach { invite ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(
                            Res.string.auth_pending_invites_subtitle,
                            invite.householdName,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneySemanticColors.inkMuted(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { onAccept(invite.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("${PendingInvitesTestTags.ACCEPT_BUTTON}_${invite.id}"),
                        ) {
                            Text(stringResource(Res.string.auth_pending_invites_accept))
                        }
                        OutlinedButton(
                            onClick = { onDecline(invite.id) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("${PendingInvitesTestTags.DECLINE_BUTTON}_${invite.id}"),
                        ) {
                            Text(stringResource(Res.string.auth_pending_invites_decline))
                        }
                    }
                }
            }
        }
    }
}
