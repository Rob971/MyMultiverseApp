package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/** Bottom navigation tab artwork — light (`drawable/`) and dark (`drawable-dark/`) WebP variants. */
enum class MainTabIconKind {
    Today,
    MealPlan,
    Grocery,
}

fun MainTabIconKind.lightAssetPath(): String = assetPaths().first

fun NutritionFeatureKind.toMainTabIconKind(): MainTabIconKind = when (this) {
    NutritionFeatureKind.MealPlan -> MainTabIconKind.MealPlan
    NutritionFeatureKind.Grocery -> MainTabIconKind.Grocery
}

object MainTabIconDefaults {
    val tabIconSize = 32.dp
    /** Today hero circles — same WebP art as bottom nav, scaled up. */
    val heroIconSize = 48.dp
}

@Composable
fun MainTabIconArt(
    kind: MainTabIconKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val (lightPath, darkPath) = kind.assetPaths()
    Image(
        painter = rememberThemedNutritionIconPainter(lightPath, darkPath),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}