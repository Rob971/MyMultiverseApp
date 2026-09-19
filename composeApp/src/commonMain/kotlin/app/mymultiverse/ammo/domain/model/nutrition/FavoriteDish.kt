package app.mymultiverse.ammo.domain.model.nutrition

/**
 * A dish the user has saved as a favorite.
 *
 * @param label           Display text as the user typed it (trimmed, original casing).
 * @param normalisedLabel Identity key: [label] lower-cased and trimmed, unique per user.
 */
data class FavoriteDish(
    val label: String,
    val normalisedLabel: String,
)

/** Normalises a free-text meal label into the favorite identity key. */
fun favoriteKeyFor(label: String): String = label.trim().lowercase()