package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GroceryGhostPairingTest {

    @Test
    fun findOffer_tortillas_suggestsTacoFixings() {
        val items = listOf(GroceryItem(id = "1", label = "Tortillas"))
        val offer = GroceryGhostPairing.findOffer(
            triggerLabel = "Tortillas",
            existingItems = items,
            dismissedPairingIds = emptySet(),
        )
        assertNotNull(offer)
        assertEquals(GroceryGhostPairing.PairingId.TacoFixings, offer.id)
        assertEquals(
            listOf(
                GroceryGhostPairing.SuggestionItem.Salsa,
                GroceryGhostPairing.SuggestionItem.Cheese,
                GroceryGhostPairing.SuggestionItem.SourCream,
            ),
            offer.suggestions,
        )
    }

    @Test
    fun findOffer_omitsSuggestionsAlreadyOnList() {
        val items = listOf(
            GroceryItem(id = "1", label = "Tacos"),
            GroceryItem(id = "2", label = "Salsa"),
            GroceryItem(id = "3", label = "Cheese"),
        )
        val offer = GroceryGhostPairing.findOffer(
            triggerLabel = "Tacos",
            existingItems = items,
            dismissedPairingIds = emptySet(),
        )
        assertNotNull(offer)
        assertEquals(listOf(GroceryGhostPairing.SuggestionItem.SourCream), offer.suggestions)
    }

    @Test
    fun findOffer_returnsNullWhenAllSuggestionsPresent() {
        val items = listOf(
            GroceryItem(id = "1", label = "Tortillas"),
            GroceryItem(id = "2", label = "Salsa"),
            GroceryItem(id = "3", label = "Cheese"),
            GroceryItem(id = "4", label = "Sour cream"),
        )
        val offer = GroceryGhostPairing.findOffer(
            triggerLabel = "Tortillas",
            existingItems = items,
            dismissedPairingIds = emptySet(),
        )
        assertNull(offer)
    }

    @Test
    fun findOffer_respectsDismissedPairingId() {
        val items = listOf(GroceryItem(id = "1", label = "Pasta"))
        val offer = GroceryGhostPairing.findOffer(
            triggerLabel = "Pasta",
            existingItems = items,
            dismissedPairingIds = setOf(GroceryGhostPairing.PairingId.PastaToppings),
        )
        assertNull(offer)
    }

    @Test
    fun findOffer_returnsNullForUnrelatedItem() {
        val items = listOf(GroceryItem(id = "1", label = "Apples"))
        val offer = GroceryGhostPairing.findOffer(
            triggerLabel = "Apples",
            existingItems = items,
            dismissedPairingIds = emptySet(),
        )
        assertNull(offer)
    }

    @Test
    fun matchesTrigger_allowsPartialWordMatch() {
        assertTrue(
            GroceryGhostPairing.matchesTrigger(
                "flour tortillas",
                setOf("tortilla"),
            ),
        )
    }

    @Test
    fun canonicalLabel_mapsSuggestionItems() {
        assertEquals("Marinara sauce", GroceryGhostPairing.canonicalLabel(GroceryGhostPairing.SuggestionItem.MarinaraSauce))
    }
}
