package app.mymultiverse.ammo.domain.model.nutrition

/** Kind of a seasonal food; weekly suggestions are checked against these (e.g. distinct vegetables). */
enum class SeasonalFoodCategory { VEGETABLE, FRUIT, LEGUME, GRAIN, FISH, MEAT, DAIRY, OTHER }

/** One in-season food. [id] is a stable lowercase slug that weekly suggestions refer to. */
data class SeasonalFood(val id: String, val name: String, val category: SeasonalFoodCategory)

/** In-season foods for a country and month, named in the user's language. */
data class SeasonalCatalog(
    val countryCode: String,
    val month: Int,
    val languageCode: String,
    val foods: List<SeasonalFood>,
) {
    fun isFor(countryCode: String, month: Int, languageCode: String): Boolean =
        this.countryCode.equals(countryCode, ignoreCase = true) &&
            this.month == month &&
            this.languageCode.equals(languageCode, ignoreCase = true)
}
