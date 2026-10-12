package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem

/**
 * Pure 3-way merge for grocery lists by item id.
 *
 * Resolves conflicts between local and remote changes against a common base.
 * All decisions are deterministic and symmetric: swapping local and remote
 * produces the same item ids and the same field values.
 */
object GroceryMerge {

    /**
     * Merges three grocery lists into one resolved list.
     *
     * @param base   last list known identical on the server (anchor point)
     * @param local  this device's current list
     * @param remote the server's current list
     * @return resolved list — local-only additions (in local order) followed
     *         by resolved remote items (in remote order)
     */
    fun merge(
        base: List<GroceryItem>,
        local: List<GroceryItem>,
        remote: List<GroceryItem>,
    ): List<GroceryItem> {
        // First-occurrence dedup within each list
        val baseById = base.firstById()
        val localById = local.firstById()
        val remoteById = remote.firstById()

        // --- Phase 1: local-only survivors (in local order) ---
        val localOnly = mutableListOf<GroceryItem>()
        for (item in localById.values) {
            val id = item.id
            if (id in remoteById) continue          // in both → handled in phase 2
            if (id in baseById) continue             // remote deleted it → DROP
            localOnly.add(item)                      // local add → KEEP
        }

        // --- Phase 2: remote-part survivors (in remote order) ---
        val remotePart = mutableListOf<GroceryItem>()
        for (item in remoteById.values) {
            val id = item.id
            val l = localById[id]
            if (l == null) {
                // Remote-only: KEEP unless local deleted it
                if (id !in baseById) remotePart.add(item)
            } else {
                // In both → merge fields
                val b = baseById[id]
                remotePart.add(
                    GroceryItem(
                        id = id,
                        label = resolveField(b?.label, l.label, item.label),
                        isChecked = resolveChecked(b?.isChecked, l.isChecked, item.isChecked),
                        isPantryCheck = resolveChecked(b?.isPantryCheck, l.isPantryCheck, item.isPantryCheck),
                    )
                )
            }
        }

        return localOnly + remotePart
    }

    // --- Field-level resolution ---

    private fun resolveField(base: String?, local: String, remote: String): String = when {
        local == remote -> local
        base == null -> if (local >= remote) local else remote // tie → greater wins
        local == base -> remote // only remote changed
        remote == base -> local // only local changed
        else -> if (local >= remote) local else remote // tie → greater wins
    }

    private fun resolveChecked(base: Boolean?, local: Boolean, remote: Boolean): Boolean = when {
        local == remote -> local
        base == null -> true // tie → true wins
        local == base -> remote // only remote changed
        remote == base -> local // only local changed
        else -> true // tie → true wins
    }

    private fun List<GroceryItem>.firstById(): Map<String, GroceryItem> {
        val map = LinkedHashMap<String, GroceryItem>()
        for (item in this) {
            if (item.id !in map) map[item.id] = item
        }
        return map
    }
}