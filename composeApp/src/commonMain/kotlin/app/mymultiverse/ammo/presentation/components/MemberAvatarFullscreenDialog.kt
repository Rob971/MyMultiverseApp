package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.mymultiverse.ammo.data.platform.AvatarImageFetcher
import app.mymultiverse.ammo.domain.sharing.memberAvatarInitials
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors

object MemberAvatarFullscreenTestTags {
    const val DIALOG = "household_members_avatar_fullscreen"
    const val CLOSE = "household_members_avatar_fullscreen_close"
}

@Composable
fun MemberAvatarFullscreenDialog(
    displayName: String,
    avatarUrl: String,
    closeLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initials = remember(displayName) { memberAvatarInitials(displayName) }
    var bitmap by remember(avatarUrl) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(avatarUrl) { mutableStateOf(true) }

    LaunchedEffect(avatarUrl) {
        isLoading = true
        bitmap = null
        bitmap = AvatarImageFetcher.load(avatarUrl.trim())
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .testTag(MemberAvatarFullscreenTestTags.DIALOG)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
                Box(
                    modifier = Modifier
                        .sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(JourneySemanticColors.brandTeal()),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        bitmap != null -> {
                            Image(
                                bitmap = bitmap!!,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        isLoading -> {
                            CircularProgressIndicator(
                                color = SharedJourneyColors.MediterraneanTeal,
                            )
                        }
                        else -> {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
            JourneyIconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .testTag(MemberAvatarFullscreenTestTags.CLOSE)
                    .semantics { contentDescription = closeLabel },
            ) {
                JourneyIcon(
                    role = AppIconRole.ChromeClose,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}
