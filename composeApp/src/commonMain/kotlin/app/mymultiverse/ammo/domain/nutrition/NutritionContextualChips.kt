package app.mymultiverse.ammo.domain.nutrition

/**
 * Derives contextual AI chip ingredients from meal-plan text and grocery list history (E4-11).
 */
object NutritionContextualChips {

    data class IngredientMatch(
        val id: String,
        val displayName: String,
    )

    private data class LexiconEntry(
        val id: String,
        val displayName: String,
        val matchers: List<(String) -> Boolean>,
    )

    private val lexicon = listOf(
        entry("chicken", "chicken", "chicken"),
        entry("beef", "beef", "ground beef", "steak"),
        entry("pork", "pork", "pork chop", "bacon"),
        entry("salmon", "salmon", "fish"),
        entry("shrimp", "shrimp", "prawn"),
        entry("tofu", "tofu"),
        entry("rice", "rice"),
        entry("pasta", "pasta", "spaghetti", "penne"),
        entry("beans", "beans", "black beans", "kidney beans"),
        entry("tomatoes", "tomato", "tomatoes"),
        entry("spinach", "spinach"),
        entry("broccoli", "broccoli"),
        entry("cheese", "cheese", "mozzarella", "cheddar"),
        entry("eggs", "egg", "eggs"),
        entry("potatoes", "potato", "potatoes"),
        entry("mushrooms", "mushroom", "mushrooms"),
        entry("tortillas", "tortilla", "tortillas"),
    )

    /**
     * @param mealTexts lunch/dinner strings from the current week
     * @param groceryLabels unchecked grocery item labels (checked items are excluded by caller)
     * @param maxChips maximum suggestions (default 3)
     */
    fun ingredientsFromHistory(
        mealTexts: List<String>,
        groceryLabels: List<String>,
        maxChips: Int = 3,
        languageCode: String = "en",
    ): List<IngredientMatch> {
        if (maxChips <= 0) return emptyList()

        val ordered = linkedSetOf<String>()
        groceryLabels.forEach { label ->
            match(label)?.let { ordered += it.id }
        }

        val mealCounts = mutableMapOf<String, Int>()
        mealTexts.forEach { text ->
            match(text)?.let { mealCounts[it.id] = (mealCounts[it.id] ?: 0) + 1 }
        }
        mealCounts.entries
            .sortedByDescending { it.value }
            .forEach { (id, _) -> ordered += id }

        return ordered
            .take(maxChips)
            .mapNotNull { id -> lexicon.find { it.id == id }?.toMatch(languageCode) }
    }

    private fun match(text: String): IngredientMatch? {
        val normalized = text.trim().lowercase()
        if (normalized.isEmpty()) return null
        return lexicon.firstOrNull { entry ->
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, entry.id) ||
                entry.matchers.any { it(normalized) }
        }?.toMatch()
    }

    private fun LexiconEntry.toMatch(languageCode: String = "en") = IngredientMatch(
        id = id,
        displayName = NutritionFoodSuggestionLocalization.ingredientNameFor(id, languageCode),
    )

    private fun entry(id: String, displayName: String, vararg keywords: String): LexiconEntry {
        val terms = (listOf(displayName) + keywords.toList()).distinct()
        return LexiconEntry(
            id = id,
            displayName = displayName,
            matchers = terms.map { term ->
                { normalized: String ->
                    normalized.contains(term.lowercase())
                }
            },
        )
    }
}
