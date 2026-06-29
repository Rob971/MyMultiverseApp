package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem

/**
 * Suggests commonly paired grocery items after a related trigger is added (E4-9).
 */
object GroceryGhostPairing {

    enum class PairingId {
        TacoFixings,
        PastaToppings,
        BurgerExtras,
        CoffeeAddIns,
    }

    enum class SuggestionItem {
        Salsa,
        Cheese,
        SourCream,
        Parmesan,
        MarinaraSauce,
        Buns,
        Lettuce,
        Milk,
        Sugar,
    }

    data class Offer(
        val id: PairingId,
        val triggerLabel: String,
        val suggestions: List<SuggestionItem>,
    )

    private data class Rule(
        val id: PairingId,
        val triggers: Set<String>,
        val suggestions: List<SuggestionItem>,
    )

    private val rules = listOf(
        Rule(
            id = PairingId.TacoFixings,
            triggers = setOf(
                "tortilla", "tortillas", "taco", "tacos", "taco shells",
                "tortilla de", "tortillas de",
            ),
            suggestions = listOf(
                SuggestionItem.Salsa,
                SuggestionItem.Cheese,
                SuggestionItem.SourCream,
            ),
        ),
        Rule(
            id = PairingId.PastaToppings,
            triggers = setOf(
                "pasta", "spaghetti", "penne", "fusilli", "noodles", "nudeln",
                "pâtes", "pates", "espagueti",
            ),
            suggestions = listOf(
                SuggestionItem.Parmesan,
                SuggestionItem.MarinaraSauce,
            ),
        ),
        Rule(
            id = PairingId.BurgerExtras,
            triggers = setOf(
                "burger", "burgers", "hamburger", "ground beef", "beef patty",
                "hamburguesa", "carne molida",
            ),
            suggestions = listOf(
                SuggestionItem.Buns,
                SuggestionItem.Cheese,
                SuggestionItem.Lettuce,
            ),
        ),
        Rule(
            id = PairingId.CoffeeAddIns,
            triggers = setOf(
                "coffee", "espresso", "ground coffee", "café", "cafe", "kaffee",
            ),
            suggestions = listOf(
                SuggestionItem.Milk,
                SuggestionItem.Sugar,
            ),
        ),
    )

    fun findOffer(
        triggerLabel: String,
        existingItems: List<GroceryItem>,
        dismissedPairingIds: Set<PairingId>,
    ): Offer? {
        val normalizedTrigger = normalize(triggerLabel)
        if (normalizedTrigger.isEmpty()) return null

        val rule = rules.firstOrNull { rule ->
            rule.id !in dismissedPairingIds && matchesTrigger(normalizedTrigger, rule.triggers)
        } ?: return null

        val missingSuggestions = rule.suggestions.filter { suggestion ->
            val canonical = canonicalLabel(suggestion)
            GroceryListPresentation.findItemByNormalizedLabel(existingItems, canonical) == null
        }
        if (missingSuggestions.isEmpty()) return null

        return Offer(
            id = rule.id,
            triggerLabel = triggerLabel.trim(),
            suggestions = missingSuggestions,
        )
    }

    fun canonicalLabel(item: SuggestionItem): String = when (item) {
        SuggestionItem.Salsa -> "Salsa"
        SuggestionItem.Cheese -> "Cheese"
        SuggestionItem.SourCream -> "Sour cream"
        SuggestionItem.Parmesan -> "Parmesan"
        SuggestionItem.MarinaraSauce -> "Marinara sauce"
        SuggestionItem.Buns -> "Buns"
        SuggestionItem.Lettuce -> "Lettuce"
        SuggestionItem.Milk -> "Milk"
        SuggestionItem.Sugar -> "Sugar"
    }

    internal fun normalize(label: String): String =
        label.trim().lowercase()

    internal fun matchesTrigger(normalizedLabel: String, triggers: Set<String>): Boolean =
        triggers.any { trigger ->
            normalizedLabel == trigger || normalizedLabel.contains(trigger)
        }
}
