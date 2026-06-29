package app.mymultiverse.ammo.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.imageVector
import app.mymultiverse.ammo.presentation.theme.resolveTint

@Composable
fun JourneyIcon(
    role: AppIconRole,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    tint: Color? = null,
    useContentColor: Boolean = false,
) {
    JourneyIcon(
        imageVector = role.imageVector(),
        role = role,
        contentDescription = contentDescription,
        modifier = modifier,
        accentColor = accentColor,
        tint = tint,
        useContentColor = useContentColor,
    )
}

@Composable
fun JourneyIcon(
    imageVector: ImageVector,
    role: AppIconRole,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    tint: Color? = null,
    useContentColor: Boolean = false,
) {
    val resolvedTint = when {
        useContentColor -> LocalContentColor.current
        tint != null -> tint
        else -> role.resolveTint(accentColor = accentColor)
    }
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = resolvedTint,
        modifier = modifier,
    )
}
