package app.mymultiverse.ammo.presentation.screens.nutrition

import androidx.compose.runtime.Composable
import app.mymultiverse.ammo.domain.nutrition.GroceryGhostPairing
import ammo.composeapp.generated.resources.Res
import ammo.composeapp.generated.resources.nutrition_grocery_ghost_pairing_list_many
import ammo.composeapp.generated.resources.nutrition_grocery_ghost_pairing_list_two
import ammo.composeapp.generated.resources.nutrition_pairing_item_buns
import ammo.composeapp.generated.resources.nutrition_pairing_item_cheese
import ammo.composeapp.generated.resources.nutrition_pairing_item_lettuce
import ammo.composeapp.generated.resources.nutrition_pairing_item_marinara
import ammo.composeapp.generated.resources.nutrition_pairing_item_milk
import ammo.composeapp.generated.resources.nutrition_pairing_item_parmesan
import ammo.composeapp.generated.resources.nutrition_pairing_item_salsa
import ammo.composeapp.generated.resources.nutrition_pairing_item_sour_cream
import ammo.composeapp.generated.resources.nutrition_pairing_item_sugar
import org.jetbrains.compose.resources.stringResource

@Composable
fun groceryGhostPairingItemLabel(item: GroceryGhostPairing.SuggestionItem): String = when (item) {
    GroceryGhostPairing.SuggestionItem.Salsa -> stringResource(Res.string.nutrition_pairing_item_salsa)
    GroceryGhostPairing.SuggestionItem.Cheese -> stringResource(Res.string.nutrition_pairing_item_cheese)
    GroceryGhostPairing.SuggestionItem.SourCream -> stringResource(Res.string.nutrition_pairing_item_sour_cream)
    GroceryGhostPairing.SuggestionItem.Parmesan -> stringResource(Res.string.nutrition_pairing_item_parmesan)
    GroceryGhostPairing.SuggestionItem.MarinaraSauce -> stringResource(Res.string.nutrition_pairing_item_marinara)
    GroceryGhostPairing.SuggestionItem.Buns -> stringResource(Res.string.nutrition_pairing_item_buns)
    GroceryGhostPairing.SuggestionItem.Lettuce -> stringResource(Res.string.nutrition_pairing_item_lettuce)
    GroceryGhostPairing.SuggestionItem.Milk -> stringResource(Res.string.nutrition_pairing_item_milk)
    GroceryGhostPairing.SuggestionItem.Sugar -> stringResource(Res.string.nutrition_pairing_item_sugar)
}

@Composable
fun formatGhostPairingItemList(labels: List<String>): String = when (labels.size) {
    0 -> ""
    1 -> labels[0]
    2 -> stringResource(
        Res.string.nutrition_grocery_ghost_pairing_list_two,
        labels[0],
        labels[1],
    )
    else -> stringResource(
        Res.string.nutrition_grocery_ghost_pairing_list_many,
        labels.dropLast(1).joinToString(", "),
        labels.last(),
    )
}
