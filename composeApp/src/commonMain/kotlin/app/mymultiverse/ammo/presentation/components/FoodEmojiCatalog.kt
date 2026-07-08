package app.mymultiverse.ammo.presentation.components

/**
 * Presentation-layer catalog that maps known grocery labels and meal-text keywords to food emoji.
 * Returns null when no recognized item is found, so callers can decide whether to show a thumbnail.
 */
object FoodEmojiCatalog {

    // Exact / normalized grocery label → emoji (keyed by lowercase English label)
    private val groceryLabels: Map<String, String> = mapOf(
        "almond butter" to "🥜",
        "apple" to "🍎",
        "apples" to "🍎",
        "asparagus" to "🥦",
        "avocado" to "🥑",
        "balsamic vinegar" to "🍶",
        "banana" to "🍌",
        "bananas" to "🍌",
        "basil" to "🌿",
        "bell pepper" to "🫑",
        "bell peppers" to "🫑",
        "bean" to "🫘",
        "beans" to "🫘",
        "black beans" to "🫘",
        "black pepper" to "🧂",
        "berries" to "🫐",
        "broccoli" to "🥦",
        "brown rice" to "🍚",
        "butter" to "🧈",
        "canned tomatoes" to "🍅",
        "canned tuna" to "🐟",
        "carrot" to "🥕",
        "carrots" to "🥕",
        "celery" to "🌿",
        "cheese" to "🧀",
        "cheese sticks" to "🧀",
        "cherry tomatoes" to "🍅",
        "chicken" to "🍗",
        "chicken breast" to "🍗",
        "chicken stock" to "🍗",
        "chickpeas" to "🫘",
        "coconut yogurt" to "🥥",
        "cottage cheese" to "🧀",
        "cucumber" to "🥒",
        "cumin" to "🌶️",
        "dill" to "🌿",
        "dried beans" to "🫘",
        "egg" to "🥚",
        "eggs" to "🥚",
        "firm tofu" to "🌱",
        "tofu" to "🌱",
        "fortified plant milk" to "🥛",
        "fresh fruit" to "🍊",
        "frozen vegetables" to "🥦",
        "garlic" to "🧄",
        "gluten-free oats" to "🌾",
        "greek yogurt" to "🥛",
        "yogurt" to "🥛",
        "ground beef" to "🥩",
        "beef" to "🥩",
        "steak" to "🥩",
        "herbs" to "🌿",
        "honey" to "🍯",
        "hummus" to "🫘",
        "kidney beans" to "🫘",
        "lemon" to "🍋",
        "lemons" to "🍋",
        "lentils" to "🫘",
        "lettuce" to "🥬",
        "milk" to "🥛",
        "mixed greens" to "🥬",
        "mixed salad greens" to "🥬",
        "mushroom" to "🍄",
        "mushrooms" to "🍄",
        "oats" to "🌾",
        "olive oil" to "🫒",
        "onion" to "🧅",
        "parmesan" to "🧀",
        "pasta" to "🍝",
        "potato" to "🥔",
        "potatoes" to "🥔",
        "pork" to "🥓",
        "quinoa" to "🌾",
        "rice" to "🍚",
        "rice cakes" to "🌾",
        "salmon" to "🐟",
        "salmon fillets" to "🐟",
        "fish" to "🐟",
        "salsa" to "🍅",
        "salt" to "🧂",
        "sardines" to "🐟",
        "scallions" to "🌿",
        "seasonal vegetables" to "🥦",
        "sesame oil" to "🌾",
        "shrimp" to "🦐",
        "prawn" to "🦐",
        "soy sauce" to "🍶",
        "spinach" to "🥬",
        "sunflower seed butter" to "🌻",
        "tomato" to "🍅",
        "tomatoes" to "🍅",
        "tortilla" to "🫓",
        "tortillas" to "🫓",
        "turkey" to "🦃",
        "turkey slices" to "🦃",
        "vegetable broth" to "🥦",
        "whole-grain bread" to "🍞",
        "bread" to "🍞",
        "whole-wheat pasta" to "🍝",
    )

    // Ordered list of (keywords, emoji) for meal text fuzzy matching
    private val mealMatchers: List<Pair<List<String>, String>> = listOf(
        listOf("pizza") to "🍕",
        listOf("pasta", "spaghetti", "penne", "linguine", "fettuccine", "carbonara", "lasagna", "lasagne", "tagliatelle") to "🍝",
        listOf("noodle", "noodles", "ramen", "pad thai", "pho", "udon", "lo mein", "chow mein") to "🍜",
        listOf("burger", "hamburger", "cheeseburger") to "🍔",
        listOf("taco", "tacos") to "🌮",
        listOf("burrito", "fajita") to "🌯",
        listOf("wrap") to "🥙",
        listOf("sushi", "maki", "nigiri", "sashimi", "temaki") to "🍣",
        listOf("dumpling", "dumplings", "gyoza", "wonton", "dim sum") to "🥟",
        listOf("curry") to "🍛",
        listOf("soup", "stew", "chili", "chilli", "bisque", "minestrone", "ramen soup") to "🍲",
        listOf("salad") to "🥗",
        listOf("sandwich", "sub", "hoagie", "panini") to "🥪",
        listOf("pancake", "pancakes", "waffle", "waffles", "french toast", "crepe", "crepes") to "🥞",
        listOf("oatmeal", "porridge", "granola", "cereal", "muesli") to "🥣",
        listOf("egg", "eggs", "omelette", "omelet", "frittata", "scrambled", "benedict") to "🍳",
        listOf("rice bowl", "fried rice", "rice") to "🍚",
        listOf("risotto") to "🍚",
        listOf("steak", "beef steak") to "🥩",
        listOf("pork", "ribs", "pork chop", "pulled pork") to "🥓",
        listOf("chicken", "poultry") to "🍗",
        listOf("fish", "salmon", "tuna", "cod", "tilapia", "seafood", "shrimp", "prawn") to "🐟",
        listOf("bbq", "barbecue", "grill", "grilled") to "🍖",
        listOf("stir fry", "stir-fry") to "🥘",
        listOf("paella") to "🥘",
        listOf("enchilada", "quesadilla") to "🫔",
        listOf("pita", "kebab", "gyro", "shawarma", "falafel") to "🥙",
        listOf("quiche", "pot pie") to "🥧",
        listOf("bento", "bowl") to "🍱",
        listOf("fondue") to "🫕",
        listOf("beef", "ground beef") to "🥩",
        listOf("tofu", "tempeh") to "🌱",
        listOf("veggie", "vegetable", "vegetables") to "🥦",
    )

    /**
     * Returns a food emoji for the given grocery item label, or null if the item is not recognized.
     * Matching is case-insensitive. First tries exact match, then substring containment.
     */
    fun emojiForGroceryLabel(label: String): String? {
        if (label.isBlank()) return null
        val normalized = label.trim().lowercase()
        // Exact match
        groceryLabels[normalized]?.let { return it }
        // Catalog key found inside label (e.g. "organic chicken breast" matches "chicken breast")
        val byKeyInLabel = groceryLabels.entries
            .filter { (key, _) -> normalized.contains(key) }
            .maxByOrNull { (key, _) -> key.length }
        if (byKeyInLabel != null) return byKeyInLabel.value
        // Label found inside a catalog key (e.g. "carrot" matches "carrots")
        val byLabelInKey = groceryLabels.entries
            .filter { (key, _) -> key.contains(normalized) }
            .minByOrNull { (key, _) -> key.length }
        return byLabelInKey?.value
    }

    /**
     * Returns a food emoji representing the dish described in [mealText], or null if unrecognized.
     * Matching checks each keyword list in priority order and returns the first match.
     */
    fun emojiForMealText(mealText: String): String? {
        if (mealText.isBlank()) return null
        val normalized = mealText.trim().lowercase()
        return mealMatchers.firstOrNull { (keywords, _) ->
            keywords.any { keyword -> normalized.contains(keyword) }
        }?.second
    }
}
