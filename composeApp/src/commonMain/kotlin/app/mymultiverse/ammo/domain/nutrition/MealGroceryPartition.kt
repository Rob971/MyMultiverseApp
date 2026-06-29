package app.mymultiverse.ammo.domain.nutrition

/**
 * Splits meal→grocery AI labels into shopping items vs pantry staples (E4-10).
 * Pantry staples stay in a separate "check if you have these" section by default.
 */
object MealGroceryPartition {

    data class Result(
        val shopping: List<String>,
        val pantryStaples: List<String>,
    )

    private val pantryMatchers: List<(String) -> Boolean> = listOf(
        { it.contains("salt") },
        { it.contains("pepper") && !it.contains("bell pepper") },
        { it.contains("olive oil") || it == "oil" || it.endsWith(" oil") },
        { it.contains("sesame oil") },
        { it.contains("garlic") },
        { it.contains("onion") && !it.contains("green onion") && !it.contains("scallion") },
        { it.contains("butter") },
        { it.contains("herb") },
        { it.contains("dill") },
        { it.contains("basil") },
        { it.contains("soy sauce") },
        { it.contains("vinegar") },
        { it.contains("cumin") },
        { it.contains("ginger") && !it.contains("ginger ale") },
        { it.contains("stock") || it.contains("broth") },
        { it.contains("honey") },
    )

    fun partition(labels: List<String>): Result {
        val shopping = linkedSetOf<String>()
        val pantry = linkedSetOf<String>()
        labels.forEach { raw ->
            val label = raw.trim()
            if (label.isEmpty()) return@forEach
            val normalized = label.lowercase()
            if (pantryMatchers.any { it(normalized) }) {
                pantry += label
            } else {
                shopping += label
            }
        }
        return Result(
            shopping = shopping.toList(),
            pantryStaples = pantry.toList(),
        )
    }

    fun isPantryStaple(label: String): Boolean {
        val normalized = label.trim().lowercase()
        if (normalized.isEmpty()) return false
        return pantryMatchers.any { it(normalized) }
    }
}
