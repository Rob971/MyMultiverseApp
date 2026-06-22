package app.mymultiverse.kmp.domain.nutrition

import app.mymultiverse.kmp.domain.model.nutrition.GroceryItem

object GroceryListPresentation {

    data class Sections(
        val active: List<GroceryItem>,
        val completed: List<GroceryItem>,
    )

    fun partition(items: List<GroceryItem>): Sections {
        val active = mutableListOf<GroceryItem>()
        val completed = mutableListOf<GroceryItem>()
        items.forEach { item ->
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
}
