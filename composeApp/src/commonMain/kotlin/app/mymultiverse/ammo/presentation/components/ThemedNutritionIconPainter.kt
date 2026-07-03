package app.mymultiverse.ammo.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.decodeToImageBitmap
import app.mymultiverse.ammo.presentation.theme.isAppInDarkTheme
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

internal object NutritionIconAssetPaths {
    const val NAV_TODAY_LIGHT =
        "composeResources/ammo.composeapp.generated.resources/drawable/nav_today.webp"
    const val NAV_TODAY_DARK =
        "composeResources/ammo.composeapp.generated.resources/drawable-dark/nav_today.webp"
    const val MEAL_PLAN_LIGHT =
        "composeResources/ammo.composeapp.generated.resources/drawable/nutrition_meal_plan.webp"
    const val MEAL_PLAN_DARK =
        "composeResources/ammo.composeapp.generated.resources/drawable-dark/nutrition_meal_plan.webp"
    const val GROCERY_LIGHT =
        "composeResources/ammo.composeapp.generated.resources/drawable/nutrition_grocery_list.webp"
    const val GROCERY_DARK =
        "composeResources/ammo.composeapp.generated.resources/drawable-dark/nutrition_grocery_list.webp"
}

internal fun MainTabIconKind.assetPaths(): Pair<String, String> = when (this) {
    MainTabIconKind.Today -> NutritionIconAssetPaths.NAV_TODAY_LIGHT to NutritionIconAssetPaths.NAV_TODAY_DARK
    MainTabIconKind.MealPlan -> NutritionIconAssetPaths.MEAL_PLAN_LIGHT to NutritionIconAssetPaths.MEAL_PLAN_DARK
    MainTabIconKind.Grocery -> NutritionIconAssetPaths.GROCERY_LIGHT to NutritionIconAssetPaths.GROCERY_DARK
}

internal fun NutritionFeatureKind.assetPaths(): Pair<String, String> =
    toMainTabIconKind().assetPaths()

@OptIn(InternalResourceApi::class)
@Composable
internal fun rememberThemedNutritionIconPainter(lightPath: String, darkPath: String): Painter {
    val darkTheme = isAppInDarkTheme()
    val path = if (darkTheme) darkPath else lightPath
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = readResourceBytes(path).decodeToImageBitmap()
    }
    return remember(bitmap) {
        BitmapPainter(bitmap ?: ImageBitmap(1, 1))
    }
}
