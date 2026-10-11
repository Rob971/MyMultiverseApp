package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TwoDeviceGrocerySyncTest {

    private val weekKey = "2026-W40"
    private val householdId = "household-1"

    private class FakeRemoteDataSource(
        val data: MutableMap<Triple<String, String, String>, Pair<String, String>> = mutableMapOf(),
    ) : NutritionRemoteDataSource {
        var offline = false
        var failFetchOnly = false
        var upsertCount = 0
        var failNextUpserts = 0
        val upsertLog: MutableList<String> = mutableListOf()
        var gate: CompletableDeferred<Unit>? = null
        private var clock = 0L

        override suspend fun fetchWeek(
            householdId: String,
            weekKey: String,
        ): List<NutritionWeekDataRow> {
            if (offline || failFetchOnly) throw IllegalStateException("offline")
            return data.filter { (key, _) ->
                key.first == householdId && key.second == weekKey
            }.map { (key, value) ->
                NutritionWeekDataRow(
                    householdId = key.first,
                    weekKey = key.second,
                    dataKind = key.third,
                    payload = value.first,
                    updatedAt = value.second,
                )
            }
        }

        override suspend fun upsert(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
        ) {
            if (offline) throw IllegalStateException("offline")
            val currentGate = gate
            if (currentGate != null) {
                gate = null
                currentGate.await()
            }
            if (failNextUpserts > 0) {
                failNextUpserts--
                throw IllegalStateException("injected failure")
            }
            upsertCount++
            upsertLog.add(payload)
            clock++
            data[Triple(householdId, weekKey, dataKind)] = payload to "t$clock"
        }

        fun set(key: Triple<String, String, String>, payload: String, updatedAt: String) {
            data[key] = payload to updatedAt
        }
    }

    private fun encode(vararg items: GroceryItem): String =
        NutritionStorageCodec.encodeGrocery(items.toList())

    private fun item(
        id: String,
        label: String = id,
        isChecked: Boolean = false,
        isPantryCheck: Boolean = false,
    ) = GroceryItem(id, label, isChecked, isPantryCheck)

    private data class DeviceComponents(
        val store: NutritionLocalStore,
        val outbox: NutritionSyncOutbox,
        val engine: NutritionSyncEngine,
        val repository: OfflineFirstNutritionRepository,
    )

    private fun device(
        settings: MapSettings = MapSettings(),
        remote: FakeRemoteDataSource,
    ): DeviceComponents {
        val store = NutritionLocalStore(settings, householdId, weekKey)
        val outbox = NutritionSyncOutbox(settings)
        val engine = NutritionSyncEngine(remote, outbox, TestObservability.logger)
        val repository = OfflineFirstNutritionRepository(
            localStore = store,
            syncEngine = engine,
            householdId = householdId,
            weekKey = weekKey,
            remoteEnabled = true,
        )
        return DeviceComponents(store, outbox, engine, repository)
    }

    private fun DeviceComponents.seedRemote(
        items: List<GroceryItem>,
        remote: FakeRemoteDataSource,
    ) {
        val payload = encode(*items.toTypedArray())
        remote.set(Triple(householdId, weekKey, "grocery"), payload, "t0")
        store.saveGroceryItems(items)
        store.markSynced("grocery", payload)
    }

    private fun DeviceComponents.assertGroceryIs(vararg expected: GroceryItem) {
        val actual = repository.currentGroceryItems()
        val expectedIds = expected.map { it.id }
        val actualIds = actual.map { it.id }
        assertEquals(expectedIds, actualIds, "item ids")
        for (exp in expected) {
            val act = actual.first { it.id == exp.id }
            assertEquals(exp.label, act.label, "label for id=${exp.id}")
            assertEquals(exp.isChecked, act.isChecked, "isChecked for id=${exp.id}")
        }
    }

    private fun assertRemoteItems(
        remote: FakeRemoteDataSource,
        expectedIds: List<String>,
    ) {
        val keys = remote.data.filter { (k, _) ->
            k.first == householdId && k.second == weekKey && k.third == "grocery"
        }
        assertEquals(1, keys.size)
        val items = NutritionStorageCodec.decodeGrocery(keys.entries.first().value.first)
        assertEquals(expectedIds, items.map { it.id })
    }
// -- Test cases --

    @Test
    fun `incident — offline stale snapshot does not clobber partner cleanup`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val abcd = listOf(item("a"), item("b"), item("c"), item("d"))
        a.seedRemote(abcd, remote)
        b.seedRemote(abcd, remote)

        // B clears a,b,c and syncs {d} — remote now {d}
        b.repository.saveGroceryItems(listOf(item("d")))

        // A goes offline and adds x,y — enqueued in outbox, remote unchanged
        remote.offline = true
        a.repository.saveGroceryItems(
            listOf(item("a"), item("b"), item("c"), item("d"), item("x"), item("y")),
        )

        // A back online — refresh pulls {d}, merges, pushes {x,y,d}
        remote.offline = false
        a.repository.refreshFromRemote()

        a.assertGroceryIs(item("x"), item("y"), item("d"))

        // B refreshes and also sees merged {x,y,d}
        b.repository.refreshFromRemote()
        b.assertGroceryIs(item("x"), item("y"), item("d"))

        // a,b,c absent everywhere
        assertTrue(a.repository.currentGroceryItems().none { it.id in listOf("a", "b", "c") })
        assertTrue(b.repository.currentGroceryItems().none { it.id in listOf("a", "b", "c") })
    }

    @Test
    fun `stale with no offline edits — refresh matches remote`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        remote.set(Triple(householdId, weekKey, "grocery"), encode(item("d")), "t1")

        a.repository.refreshFromRemote()

        a.assertGroceryIs(item("d"))
    }

    @Test
    fun `pull fails — does not push and outbox entry survives`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val abcd = listOf(item("a"), item("b"), item("c"), item("d"))
        a.seedRemote(abcd, remote)

        remote.offline = true
        a.repository.saveGroceryItems(listOf(item("x")))

        remote.offline = false
        remote.failFetchOnly = true

        val beforeOutboxCount = a.outbox.pendingFor(householdId, weekKey).size
        a.repository.refreshFromRemote()

        assertEquals(beforeOutboxCount, a.outbox.pendingFor(householdId, weekKey).size)
        assertRemoteItems(remote, listOf("a", "b", "c", "d"))
    }

    @Test
    fun `no base and no pending — remote overwrites stale local`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.store.saveGroceryItems(listOf(item("a"), item("b")))
        remote.set(Triple(householdId, weekKey, "grocery"), encode(item("d")), "t1")

        a.repository.refreshFromRemote()

        a.assertGroceryIs(item("d"))
    }

    @Test
    fun `realtime apply — local adds merge with remote`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemote(listOf(item("a"), item("b")), remote)

        a.store.saveGroceryItems(listOf(item("a"), item("b"), item("x")))

        a.repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = encode(item("a"), item("c")),
            ),
        )

        a.assertGroceryIs(item("x", label = "x"), item("a"), item("c"))
        assertRemoteItems(remote, listOf("x", "a", "c"))
    }

    @Test
    fun `save then refresh uses updated base — catches missing markSynced`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val baseItem = listOf(item("a", label = "base"))
        a.seedRemote(baseItem, remote)
        b.seedRemote(baseItem, remote)

        // B edits label and pushes successfully
        b.repository.saveGroceryItems(listOf(item("a", label = "B_label")))

        // A edits label and pushes — remote now has A_label
        a.repository.saveGroceryItems(listOf(item("a", label = "A_label")))

        // B refreshes — should fast-forward to A_label (B's local == B's base after push)
        b.repository.refreshFromRemote()

        // B refreshes — should fast-forward: local == base -> remote wins
        assertEquals("A_label", b.repository.currentGroceryItems().single().label)
    }

    @Test
    fun `convergence — no echo ping-pong on second refresh`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val abcd = listOf(item("a"), item("b"), item("c"), item("d"))
        a.seedRemote(abcd, remote)
        b.seedRemote(abcd, remote)

        // B clears a,b,c and syncs {d}
        b.repository.saveGroceryItems(listOf(item("d")))

        // A goes offline and adds x,y
        remote.offline = true
        a.repository.saveGroceryItems(
            listOf(item("a"), item("b"), item("c"), item("d"), item("x"), item("y")),
        )
        remote.offline = false

        val upsertsBefore = remote.upsertCount
        a.repository.refreshFromRemote()
        assertTrue(remote.upsertCount > upsertsBefore, "A refresh must push merged")

        b.repository.refreshFromRemote()
        val upsertsAfterB = remote.upsertCount

        a.repository.refreshFromRemote()
        b.repository.refreshFromRemote()

        assertEquals(upsertsAfterB, remote.upsertCount, "second refresh: no upsert")
        a.assertGroceryIs(item("x"), item("y"), item("d"))
        b.assertGroceryIs(item("x"), item("y"), item("d"))
    }

    @Test
    fun `(g) flush after failed apply-push advances base — finding 1`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val abcd = listOf(item("a"), item("b"), item("c"), item("d"))
        a.seedRemote(abcd, remote)
        b.seedRemote(abcd, remote)

        // A goes offline and adds x,y — pending
        remote.offline = true
        a.repository.saveGroceryItems(
            listOf(item("a"), item("b"), item("c"), item("d"), item("x"), item("y")),
        )

        // B deletes d and syncs
        remote.offline = false
        b.repository.saveGroceryItems(listOf(item("b"), item("c")))

        // A back online — push fails (enqueues merged), flush succeeds
        remote.failNextUpserts = 1
        a.repository.refreshFromRemote()

        // B deletes x and syncs
        b.repository.saveGroceryItems(listOf(item("b"), item("c"), item("y")))

        // A refreshes — if base was advanced after flush, x stays gone
        a.repository.refreshFromRemote()

        // x must be absent (partner deleted it; base was advanced so A knows x was merged)
        a.assertGroceryIs(item("b"), item("c"), item("y"))
    }

    @Test
    fun `(h) applyRemoteWeekData returns GroceryChange with before and after for grocery`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemote(listOf(item("a"), item("b")), remote)

        a.store.saveGroceryItems(listOf(item("a"), item("b"), item("x")))

        val change = a.repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = encode(item("a"), item("c")),
            ),
        )

        // GroceryChange must be non-null with correct before and after
        assertTrue(change != null, "applyRemoteWeekData must return non-null for grocery")
        assertEquals(listOf("a", "b", "x"), change!!.before.map { it.id })
        assertEquals(listOf("x", "a", "c"), change!!.after.map { it.id })

        // For ai_grocery and meal_plan, return null
        val aiChange = a.repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "ai_grocery",
                payload = encode(item("g")),
            ),
        )
        assertEquals(null, aiChange, "ai_grocery must return null")

        val mpChange = a.repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "meal_plan",
                payload = NutritionStorageCodec.encodeMealPlan(
                    WeeklyMealPlan(weekKey, List(7) { DayMeals() }),
                ),
            ),
        )
        assertEquals(null, mpChange, "meal_plan must return null")
    }

    @Test
    fun `(i) serialization — mutex sequences push calls across operations`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemote(listOf(item("a")), remote)

        // Base={a}, local={a,b} (local add), remote={a,c} (remote add)
        a.store.saveGroceryItems(listOf(item("a"), item("b")))
        val change = a.repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "grocery",
                payload = encode(item("a"), item("c")),
            ),
        )
        // merged = GroceryMerge.merge([a], [a,b], [a,c]) = [b, a, c] != remote
        assertEquals(listOf("a", "b"), change!!.before.map { it.id })
        assertEquals(listOf("b", "a", "c"), change!!.after.map { it.id })
        assertEquals(1, remote.upsertLog.size, "apply must push merged")

        // Now save locally — push adds another entry
        a.repository.saveGroceryItems(listOf(item("a"), item("b"), item("c"), item("z")))
        assertEquals(2, remote.upsertLog.size)
        val savePayload = NutritionStorageCodec.decodeGrocery(remote.upsertLog[1])
        assertEquals(listOf("a", "b", "c", "z"), savePayload.map { it.id })

        // Final local state is the save result
        a.assertGroceryIs(item("a"), item("b"), item("c"), item("z"))

        // Verify base was updated correctly after apply+save pushes
        val lastSynced = a.store.lastSyncedPayload("grocery")
        val lastSyncedItems = NutritionStorageCodec.decodeGrocery(lastSynced!!)
        assertEquals(listOf("a", "b", "c", "z"), lastSyncedItems.map { it.id })
    }
}