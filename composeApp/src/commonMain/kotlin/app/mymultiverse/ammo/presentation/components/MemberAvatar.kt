package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.mymultiverse.ammo.data.platform.AvatarImageFetcher
import app.mymultiverse.ammo.domain.sharing.memberAvatarInitials
import app.mymultiverse.ammo.presentation.theme.JourneySemanticColors
import app.mymultiverse.ammo.presentation.theme.SharedJourneyColors

@Composable
fun MemberAvatar(
    displayName: String,
    avatarUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val initials = remember(displayName) { memberAvatarInitials(displayName) }
    val avatarContent: @Composable () -> Unit = {
        MemberAvatarContent(
            avatarUrl = avatarUrl,
            initials = initials,
            contentDescription = contentDescription,
            size = size,
            isLoading = isLoading,
        )
    }

    if (onClick != null) {
        JourneyIconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            avatarContent()
        }
    } else {
        Box(modifier = modifier) {
            avatarContent()
        }
    }
}

@Composable
private fun MemberAvatarContent(
    avatarUrl: String?,
    initials: String,
    contentDescription: String,
    size: Dp,
    isLoading: Boolean,
) {
    var bitmap by remember(avatarUrl) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(avatarUrl) {
        bitmap = null
        val url = avatarUrl?.trim().orEmpty()
        if (url.isEmpty()) return@LaunchedEffect
        bitmap = AvatarImageFetcher.load(url)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(JourneySemanticColors.brandTeal()),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        if (isLoading) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = SharedJourneyColors.MediterraneanTeal,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}
