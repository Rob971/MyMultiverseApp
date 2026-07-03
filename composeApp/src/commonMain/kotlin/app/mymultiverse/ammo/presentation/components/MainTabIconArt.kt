package app.mymultiverse.ammo.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nav_today
import ammo.composeapp.generated.resources.nutrition_grocery_list
import ammo.composeapp.generated.resources.nutrition_meal_plan
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** Bottom navigation tab artwork — transparent WebP glyphs only (no white backing plate). */
enum class MainTabIconKind {
    Today,
    MealPlan,
    Grocery,
}

fun MainTabIconKind.drawable(): DrawableResource = when (this) {
    MainTabIconKind.Today -> Res.drawable.nav_today
    MainTabIconKind.MealPlan -> Res.drawable.nutrition_meal_plan
    MainTabIconKind.Grocery -> Res.drawable.nutrition_grocery_list
}

@Composable
fun MainTabIconArt(
    kind: MainTabIconKind,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(kind.drawable()),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
