package app.mymultiverse.ammo.data.service

/** Fetches a dish-specific ingredient list from an AI backend. */
internal interface DishIngredientClient {
    /**
     * Returns a list of ingredients needed for [dish], localized to [languageCode].
     * Returns a [Result] failure on any network, authentication, or parse error.
     */
    suspend fun generateIngredients(dish: String, languageCode: String): Result<List<String>>
}
