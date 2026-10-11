package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroceryMergeTest {

    // -- helpers --

    private fun item(
        id: String,
        label: String = id,
        isChecked: Boolean = false,
        isPantryCheck: Boolean = false,
    ) = GroceryItem(id, label, isChecked, isPantryCheck)

    private fun idsOf(items: List<GroceryItem>) = items.map { it.id }

    private fun assertOrder(expected: List<String>, actual: List<GroceryItem>) {
        assertEquals(expected, idsOf(actual), "item order")
    }

    private fun assertSameFields(a: GroceryItem, b: GroceryItem) {
        assertEquals(a.label, b.label, "label differs for id=${a.id}")
        assertEquals(a.isChecked, b.isChecked, "isChecked differs for id=${a.id}")
        assertEquals(a.isPantryCheck, b.isPantryCheck, "isPantryCheck differs for id=${a.id}")
    }

    // ---------------------------------------------------------------
    // 1. The incident: offline device pushes stale full snapshot
    // ---------------------------------------------------------------

    @Test
    fun `incident regression — stale local after partner cleanup`() {
        val base = listOf(item("a"), item("b"), item("c"), item("d"))
        val remote = listOf(item("d"))
        val local = listOf(item("a"), item("b"), item("c"), item("d"), item("x"), item("y"))

        val result = GroceryMerge.merge(base, local, remote)

        // a, b, c were on both local and base but missing from remote → partner deleted → DROP
        // x, y are only on local → KEEP, in local order
        // d is on all three → KEEP as merged (same field values)
        assertOrder(listOf("x", "y", "d"), result)
    }

    // ---------------------------------------------------------------
    // 2. Local delete propagates
    // ---------------------------------------------------------------

    @Test
    fun `local delete propagates to merge result`() {
        val base = listOf(item("a"), item("b"))
        val local = listOf(item("b"))
        val remote = listOf(item("a"), item("b"), item("e"))

        val result = GroceryMerge.merge(base, local, remote)

        // a is in base and remote but local deleted it → DROP
        // b is in all three → KEEP
        // e is only in remote → KEEP
        assertOrder(listOf("b", "e"), result)
    }

    // ---------------------------------------------------------------
    // 3. Both deleted the same id
    // ---------------------------------------------------------------

    @Test
    fun `both sides deleted the same item — absent from result`() {
        val base = listOf(item("a"))
        val local = emptyList<GroceryItem>()
        val remote = emptyList<GroceryItem>()

        val result = GroceryMerge.merge(base, local, remote)

        assertTrue(result.isEmpty(), "expected empty result when both deleted the same id")
    }

    // ---------------------------------------------------------------
    // 4. Field-level merge
    // ---------------------------------------------------------------

    @Test
    fun `only local toggled isChecked — local value wins`() {
        val base = listOf(item("a", isChecked = false))
        val local = listOf(item("a", isChecked = true))
        val remote = listOf(item("a", isChecked = false))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals(true, result.single().isChecked)
    }

    @Test
    fun `only remote toggled isChecked — remote value wins`() {
        val base = listOf(item("a", isChecked = false))
        val local = listOf(item("a", isChecked = false))
        val remote = listOf(item("a", isChecked = true))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals(true, result.single().isChecked)
    }

    @Test
    fun `both toggled isChecked same way — that value wins`() {
        val base = listOf(item("a", isChecked = false))
        val local = listOf(item("a", isChecked = true))
        val remote = listOf(item("a", isChecked = true))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals(true, result.single().isChecked)
    }
@Test
    fun `tie on isChecked — true wins`() {
        val base = listOf(item("a")) // base = false
        val local = listOf(item("a", isChecked = false))
        val remote = listOf(item("a", isChecked = true))

        val result = GroceryMerge.merge(base, local, remote)

        // local=false, remote=true, base=false → local == base → remote wins (=true)
        assertEquals(true, result.single().isChecked)
    }

    @Test
    fun `tie on isPantryCheck — true wins`() {
        val base = listOf(item("a")) // base = false
        val local = listOf(item("a", isPantryCheck = true))
        val remote = listOf(item("a", isPantryCheck = false))

        val result = GroceryMerge.merge(base, local, remote)

        // local=true, remote=false, base=false → remote == base → local wins (=true)
        assertEquals(true, result.single().isPantryCheck)
    }

    @Test
    fun `both sides changed label the same — that label wins`() {
        val base = listOf(item("a", label = "original"))
        val local = listOf(item("a", label = "renamed"))
        val remote = listOf(item("a", label = "renamed"))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals("renamed", result.single().label)
    }

    @Test
    fun `label tie — greater string wins`() {
        val base = listOf(item("a", label = "alpha"))
        val local = listOf(item("a", label = "beta"))
        val remote = listOf(item("a", label = "gamma"))

        val result = GroceryMerge.merge(base, local, remote)
        // Both differ from base (tie): greater wins
        assertEquals("gamma", result.single().label)
    }

    // ---------------------------------------------------------------
    // 5. Invariant: local == base → result == remote
    //    (for lists built by prepend/remove only)
    // ---------------------------------------------------------------

    @Test
    fun `invariant — remote equals base yields local`() {
        val base = listOf(item("a"), item("b"), item("c"))
        val local = listOf(item("x"), item("a"), item("c")) // x prepended, b removed
        val remote = listOf(item("a"), item("b"), item("c"))

        val result = GroceryMerge.merge(base, local, remote)

        assertOrder(listOf("x", "a", "c"), result)
    }

    // ---------------------------------------------------------------
    // 7. Fixed point
    // ---------------------------------------------------------------

    @Test
    fun `invariant — fixed point`() {
        val base = listOf(item("a", label = "x"), item("b"))
        val local = listOf(item("a", label = "y"), item("c"))
        val remote = listOf(item("a", label = "z"), item("d"))

        val r = GroceryMerge.merge(base, local, remote)
        val reMerge = GroceryMerge.merge(base = remote, local = r, remote = remote)

        // Fixed point: merge(original_remote, merge_result, original_remote) == merge_result
        assertOrder(idsOf(r), reMerge)
        for (i in r.indices) assertSameFields(r[i], reMerge[i])
    }

    // ---------------------------------------------------------------
    // 8. Symmetry
    // ---------------------------------------------------------------

    @Test
    fun `invariant — symmetry of merge`() {
        val base = listOf(item("a"), item("b"))
        val local = listOf(item("a", label = "L"), item("c"))
        val remote = listOf(item("a", label = "R"), item("d"))

        val resultLR = GroceryMerge.merge(base, local, remote)
        val resultRL = GroceryMerge.merge(base, remote, local)

        // Same ids
        assertEquals(idsOf(resultLR).toSet(), idsOf(resultRL).toSet(), "ids must match under swap")

        // Same field values for same ids
        val mapLR = resultLR.associateBy { it.id }
        val mapRL = resultRL.associateBy { it.id }
        for ((id, itemLR) in mapLR) {
            val itemRL = mapRL[id]
            assertSameFields(itemLR, itemRL!!)
        }
    }

    // ---------------------------------------------------------------
    // 9. Empty base (never synced)
    // ---------------------------------------------------------------

    @Test
    fun `empty base — union of both sides`() {
        val local = listOf(item("a"), item("b"))
        val remote = listOf(item("b"), item("c"))

        val result = GroceryMerge.merge(base = emptyList(), local, remote)

        // local-only first (a), then remote items (b, c)
        // b is in both → merged (both have same values with empty base tie → greater label, false wins)
        assertOrder(listOf("a", "b", "c"), result)
    }

    @Test
    fun `empty base — greater label wins on conflict`() {
        val local = listOf(item("a", label = "alpha"))
        val remote = listOf(item("a", label = "beta"))

        val result = GroceryMerge.merge(base = emptyList(), local, remote)

        // Tie with empty base → greater string wins
        assertEquals("beta", result.single().label)
    }

    @Test
    fun `empty base — true wins on boolean conflict`() {
        val local = listOf(item("a", isChecked = false))
        val remote = listOf(item("a", isChecked = true))

        val result = GroceryMerge.merge(base = emptyList(), local, remote)

        // Tie with empty base → true wins
        assertEquals(true, result.single().isChecked)
    }

    // ---------------------------------------------------------------
    // 10. Edge cases
    // ---------------------------------------------------------------

    @Test
    fun `duplicate id in local — first occurrence wins`() {
        val base = listOf(item("a"))
        val local = listOf(item("a", label = "first"), item("a", label = "second"))
        val remote = listOf(item("a", label = "remote"))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals(1, result.size)
        assertEquals("remote", result.single().label)
    }

    @Test
    fun `duplicate id in remote — first occurrence wins`() {
        val base = listOf(item("a"))
        val local = listOf(item("a", label = "localValue"))
        val remote = listOf(item("a", label = "first"), item("a", label = "second"))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals(1, result.size)
        val expected = if ("localValue" >= "first") "localValue" else "first"
        assertEquals(expected, result.single().label)
    }

    @Test
    fun `all three lists empty`() {
        val result = GroceryMerge.merge(emptyList(), emptyList(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `local only has items — remote and base empty`() {
        val local = listOf(item("a"), item("b"))
        val result = GroceryMerge.merge(emptyList(), local, emptyList())
        assertOrder(listOf("a", "b"), result)
    }

    @Test
    fun `remote only has items — local and base empty`() {
        val remote = listOf(item("a"), item("b"))
        val result = GroceryMerge.merge(emptyList(), emptyList(), remote)
        assertOrder(listOf("a", "b"), result)
    }
// ---------------------------------------------------------------
    // 11. Fast-forward (local == base)
    // ---------------------------------------------------------------

    @Test
    fun `fast-forward — local equals base yields remote`() {
        val base = listOf(item("a"), item("b"))
        val local = listOf(item("a"), item("b"))
        val remote = listOf(item("b"), item("c"))

        val result = GroceryMerge.merge(base, local, remote)

        // local == base → fast-forward → result equals remote (same ids, order, fields)
        assertOrder(listOf("b", "c"), result)
        assertSameFields(remote[0], result[0])
        assertSameFields(remote[1], result[1])
    }

    // ---------------------------------------------------------------
    // 12. Label edited only on one side
    // ---------------------------------------------------------------

    @Test
    fun `label edited only locally — local label wins`() {
        val base = listOf(item("a", label = "base"))
        val local = listOf(item("a", label = "local"))
        val remote = listOf(item("a", label = "base"))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals("local", result.single().label)
    }

    @Test
    fun `label edited only remotely — remote label wins`() {
        val base = listOf(item("a", label = "base"))
        val local = listOf(item("a", label = "base"))
        val remote = listOf(item("a", label = "remote"))

        val result = GroceryMerge.merge(base, local, remote)

        assertEquals("remote", result.single().label)
    }
}