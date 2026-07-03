package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_grocery_list
import ammo.composeapp.generated.resources.nutrition_meal_plan
import app.mymultiverse.ammo.presentation.theme.AppIconRole
import app.mymultiverse.ammo.presentation.theme.AppIcons
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Shared grocery list and weekly meal plan feature visuals. */
enum class NutritionFeatureKind {
    MealPlan,
    Grocery,
}

fun NutritionFeatureKind.drawable(): DrawableResource = when (this) {
    NutritionFeatureKind.MealPlan -> Res.drawable.nutrition_meal_plan
    NutritionFeatureKind.Grocery -> Res.drawable.nutrition_grocery_list
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

/** Raster thumbnail — bottom nav, Today hero circles, and compact labels. */
@Composable
fun NutritionFeatureArt(
    feature: NutritionFeatureKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    clipShape: Shape? = null,
) {
    Image(
        painter = painterResource(feature.drawable()),
        contentDescription = contentDescription,
        modifier = modifier.then(
            if (clipShape != null) Modifier.clip(clipShape) else Modifier,
        ),
        contentScale = contentScale,
    )
}
