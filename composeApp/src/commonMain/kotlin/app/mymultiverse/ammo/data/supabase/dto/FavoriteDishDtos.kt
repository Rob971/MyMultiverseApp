package app.mymultiverse.ammo.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A row of `user_favorite_dishes`. */
@Serializable
internal data class FavoriteDishRow(
    @SerialName("label") val label: String = "",
    @SerialName("normalised_label") val normalisedLabel: String = "",
)

/** Parameters for `add_favorite(p_label)`. */
@Serializable
internal data class AddFavoriteParams(
    @SerialName("p_label") val label: String,
)

/** Parameters for `remove_favorite(p_normalised_label)`. */
@Serializable
internal data class RemoveFavoriteParams(
    @SerialName("p_normalised_label") val normalisedLabel: String,
)

/** Parameters for `replace_favorite(p_remove_normalised, p_new_label)`. */
@Serializable
internal data class ReplaceFavoriteParams(
    @SerialName("p_remove_normalised") val removeNormalised: String,
    @SerialName("p_new_label") val newLabel: String,
)