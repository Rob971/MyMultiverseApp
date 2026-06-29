package app.mymultiverse.ammo.data.nutrition

import app.mymultiverse.ammo.domain.nutrition.GroceryGhostPairing
import com.russhwolf.settings.Settings

class GroceryGhostPairingDismissStore(
    private val settings: Settings,
) {
    fun dismissedIds(householdId: String): Set<GroceryGhostPairing.PairingId> {
        val raw = settings.getStringOrNull(dismissKey(householdId)) ?: return emptySet()
        return raw.split(',')
            .mapNotNull { token ->
                runCatching { GroceryGhostPairing.PairingId.valueOf(token.trim()) }.getOrNull()
            }
            .toSet()
    }

    fun dismiss(householdId: String, pairingId: GroceryGhostPairing.PairingId) {
        val updated = dismissedIds(householdId) + pairingId
        settings.putString(
            dismissKey(householdId),
            updated.joinToString(",") { it.name },
        )
    }

    fun clearDismissed(householdId: String) {
        settings.remove(dismissKey(householdId))
    }

    private fun dismissKey(householdId: String): String =
        "grocery_ghost_pairing_dismissed_$householdId"
}
