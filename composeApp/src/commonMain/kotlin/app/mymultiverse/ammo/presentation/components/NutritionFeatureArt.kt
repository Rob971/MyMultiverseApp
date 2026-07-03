package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons

/** Shared grocery list and weekly meal plan feature visuals. */
enum class NutritionFeatureKind {
    MealPlan,
    Grocery,
}

fun NutritionFeatureKind.icon(): ImageVector = when (this) {
    NutritionFeatureKind.MealPlan -> AppIcons.MealPlan
    NutritionFeatureKind.Grocery -> AppIcons.GroceryList
}

/** Vector icon — badges and compact accents (hero circles use [MainTabIconArt]). */
@Composable
fun NutritionFeatureIcon(
    feature: NutritionFeatureKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    accentColor: Color? = null,
) {
    JourneyIcon(
        imageVector = feature.icon(),
        role = AppIconRole.FeatureAccent,
        contentDescription = contentDescription,
        accentColor = accentColor,
        tint = tint,
        modifier = modifier,
    )
}

/** Raster thumbnail — bottom nav, Today hero circles, hub cards; light/dark WebP via `drawable-dark/`. */
@Composable
fun NutritionFeatureArt(
    feature: NutritionFeatureKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    clipShape: Shape? = null,
) {
    val (lightPath, darkPath) = feature.assetPaths()
    Image(
        painter = rememberThemedNutritionIconPainter(lightPath, darkPath),
        contentDescription = contentDescription,
        modifier = modifier.then(
            if (clipShape != null) Modifier.clip(clipShape) else Modifier,
        ),
        contentScale = contentScale,
    )
}
