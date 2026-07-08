package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem

object GroceryListPresentation {

    data class Sections(
        val active: List<GroceryItem>,
        val completed: List<GroceryItem>,
    )

    fun partition(items: List<GroceryItem>): Sections {
        val seen = mutableSetOf<String>()
        val active = mutableListOf<GroceryItem>()
        val completed = mutableListOf<GroceryItem>()
        items.forEach { item ->
            if (!seen.add(item.id)) return@forEach
            if (item.isChecked) completed += item else active += item
        }
        return Sections(active = active, completed = completed)
    }

    /** Shopping mode: unchecked first; optionally omit completed from display. */
    fun forShoppingDisplay(sections: Sections, hideCompleted: Boolean): Sections =
        if (hideCompleted) {
            sections.copy(completed = emptyList())
        } else {
            sections
        }

    fun isDuplicateLabel(items: List<GroceryItem>, label: String, excludingId: String? = null): Boolean =
        findItemByNormalizedLabel(items, label, excludingId) != null

    fun findItemByNormalizedLabel(
        items: List<GroceryItem>,
        label: String,
        excludingId: String? = null,
    ): GroceryItem? {
        val normalized = label.trim().lowercase()
        if (normalized.isEmpty()) return null
        return items.firstOrNull { item ->
            item.id != excludingId && item.label.trim().lowercase() == normalized
        }
    }

    /** Moves an unchecked item up (-1) or down (+1) among active items; completed order is preserved. */
    fun moveActiveItem(items: List<GroceryItem>, itemId: String, direction: Int): List<GroceryItem> {
        if (direction == 0) return items
        val sections = partition(items)
        val active = sections.active.toMutableList()
        val fromIndex = active.indexOfFirst { it.id == itemId }
        if (fromIndex < 0) return items
        val toIndex = (fromIndex + direction).coerceIn(0, active.lastIndex)
        if (fromIndex == toIndex) return items
        val item = active.removeAt(fromIndex)
        active.add(toIndex, item)
        return active + sections.completed
    }
}
