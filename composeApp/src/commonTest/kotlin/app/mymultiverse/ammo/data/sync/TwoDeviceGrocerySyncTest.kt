package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.local.nutrition.NutritionWeekMaintenanceStore
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.data.sync.NutritionWeekMaintenanceRunner
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TwoDeviceGrocerySyncTest {

    private val weekKey = "2026-W40"
    private val householdId = "household-1"

    private class FakeRemoteDataSource(
        val data: MutableMap<Triple<String, String, String>, Triple<String, String, Long?>> = mutableMapOf(),
        /**
         * Simulates server-held claim registry: household_id -> week_key.
         * claimWeekCarry returns true when the key is new, false when already claimed.
         */
        private val claims: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    ) : NutritionRemoteDataSource {
        var offline = false
        var failFetchOnly = false
        var upsertCount = 0
        var failNextUpserts = 0
        val upsertLog: MutableList<String> = mutableListOf()
        var gate: CompletableDeferred<Unit>? = null
        private var clock = 0L
        var claimCount = 0
        var casEnabled = true
        /** Plain (blind) upserts only; conditional writes do not count here. */
        var blindUpsertCount = 0
        /** Every conditional write attempt that reached the server (ok or conflict). */
        var casAttempts = 0
        /** Every conditional write answers with a conflict (a partner that always wins). */
        var alwaysConflict = false

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
                    version = value.third,
                )
            }
        }

        override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean {
            if (offline) throw IllegalStateException("offline")
            claimCount++
            val weekSet = claims.getOrPut(householdId) { mutableSetOf() }
            return weekSet.add(weekKey)
        }

        override suspend fun upsert(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
        ) {
            beforeWrite()
            blindUpsertCount++
            upsertCount++
            upsertLog.add(payload)
            clock++
            // Blind upsert bumps the version (like the server trigger)
            val key = Triple(householdId, weekKey, dataKind)
            val existing = data[key]
            val newVersion = (existing?.third ?: 0L) + 1
            data[key] = Triple(payload, "t$clock", newVersion)
        }

        /** Offline, gate and injected failures apply to blind and conditional writes alike. */
        private suspend fun beforeWrite() {
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
        }

        override suspend fun pushIfVersion(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
            expectedVersion: Long?,
        ): PushResult {
            if (!casEnabled) {
                upsert(householdId, weekKey, dataKind, payload)
                return PushResult.ok(0)
            }
            beforeWrite()
            casAttempts++
            if (alwaysConflict) return PushResult.conflict()
            val key = Triple(householdId, weekKey, dataKind)
            val existing = data[key]
            if (expectedVersion == null) {
                // Insert path
                if (existing != null) return PushResult.conflict() // 23505
                upsertCount++
                upsertLog.add(payload)
                data[key] = Triple(payload, "t${++clock}", 1L)
                return PushResult.ok(1)
            }
            // Update with CAS
            val currentVersion = existing?.third
            if (currentVersion == null) return PushResult.conflict()
            if (currentVersion != expectedVersion) return PushResult.conflict()
            val newVersion = currentVersion + 1
            upsertCount++
            upsertLog.add(payload)
            data[key] = Triple(payload, "t${++clock}", newVersion)
            return PushResult.ok(newVersion)
        }

        fun set(key: Triple<String, String, String>, payload: String, updatedAt: String, version: Long? = null) {
            data[key] = Triple(payload, updatedAt, version)
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
        weekKey: String = this.weekKey,
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
        version: Long? = 0,
    ) {
        val payload = encode(*items.toTypedArray())
        remote.set(Triple(householdId, weekKey, "grocery"), payload, "t0", version)
        store.saveGroceryItems(items)
        store.markSynced("grocery", payload, version)
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
        weekKey: String = this.weekKey,
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

        // B edits the label and pushes: B's base is now B_label at the server's version
        b.repository.saveGroceryItems(listOf(item("a", label = "B_label")))

        // A refreshes (sees B_label), then edits and pushes A_label
        a.repository.refreshFromRemote()
        a.repository.saveGroceryItems(listOf(item("a", label = "A_label")))

        // B refreshes: B's local equals its base, so the remote edit fast-forwards.
        // Without markSynced after B's push, base would still be "base", both sides would
        // look changed, and the tie rule would keep B_label.
        b.repository.refreshFromRemote()

        assertEquals("A_label", b.repository.currentGroceryItems().single().label)
        assertEquals(0, remote.blindUpsertCount, "every write must go through the version precondition")
    }

    @Test
    fun `S3 concurrent label edits — both devices resolve to the same label by the tie rule`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val baseItem = listOf(item("a", label = "base"))
        a.seedRemote(baseItem, remote)
        b.seedRemote(baseItem, remote)

        // Neither device has seen the other's edit
        a.repository.saveGroceryItems(listOf(item("a", label = "A_label")))
        b.repository.saveGroceryItems(listOf(item("a", label = "B_label"))) // conflicts, merges, retries

        a.repository.refreshFromRemote()

        // Both edits changed the label: the greater label wins, on both devices
        assertEquals("B_label", a.repository.currentGroceryItems().single().label)
        assertEquals("B_label", b.repository.currentGroceryItems().single().label)
        assertEquals(2L, remote.data[Triple(householdId, weekKey, "grocery")]?.third)
        assertEquals(0, remote.blindUpsertCount)
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

        // B refreshes, so it has really seen x and y, then deletes x and syncs
        b.repository.refreshFromRemote()
        b.repository.saveGroceryItems(listOf(item("b"), item("c"), item("y")))

        // A refreshes: the flush advanced A's base to the list that contained x, so B's
        // deletion of a known item is honoured instead of being read as a stale snapshot.
        a.repository.refreshFromRemote()

        // x gone (deleted by the partner after seeing it); y kept (A's own offline add)
        a.assertGroceryIs(item("b"), item("c"), item("y"))
        b.assertGroceryIs(item("b"), item("c"), item("y"))
        assertEquals(0, remote.blindUpsertCount, "no write may bypass the version precondition")
    }

    @Test
    fun `S3 a stale writer that never saw an item does not delete it`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val ab = listOf(item("b"), item("c"))
        a.seedRemote(ab, remote)
        b.seedRemote(ab, remote)

        // A adds x and y and pushes
        a.repository.saveGroceryItems(listOf(item("b"), item("c"), item("x"), item("y")))

        // B never refreshed: it has not seen x, so omitting x is not a deletion
        b.repository.saveGroceryItems(listOf(item("b"), item("c"), item("y"))) // conflicts, merges

        a.repository.refreshFromRemote()
        val expected = setOf("b", "c", "x", "y")
        assertEquals(expected, a.repository.currentGroceryItems().map { it.id }.toSet())
        assertEquals(expected, b.repository.currentGroceryItems().map { it.id }.toSet())
        assertEquals(0, remote.blindUpsertCount)
    }

    @Test
    fun `S3 bounded retry — a remote that always conflicts gets exactly three attempts then the entry stays pending`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemote(listOf(item("a")), remote)
        remote.alwaysConflict = true

        a.repository.saveGroceryItems(listOf(item("a"), item("b")))

        assertEquals(3, remote.casAttempts, "bounded: three conditional attempts, no more")
        assertEquals(0, remote.blindUpsertCount)
        assertTrue(a.engine.hasPending(householdId, weekKey, "grocery"), "the write must stay queued")
        assertEquals(setOf("a", "b"), a.repository.currentGroceryItems().map { it.id }.toSet())
    }

    @Test
    fun `S3 a transport failure is not a conflict — one attempt, no merge, entry queued then flushed by CAS`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemote(listOf(item("a")), remote)

        remote.failNextUpserts = 5
        a.repository.saveGroceryItems(listOf(item("a"), item("b")))

        assertEquals(4, remote.failNextUpserts, "a failure is one attempt: no retry loop, no merge round-trip")
        assertTrue(a.engine.hasPending(householdId, weekKey, "grocery"))

        remote.failNextUpserts = 0
        a.repository.refreshFromRemote()

        assertTrue(!a.engine.hasPending(householdId, weekKey, "grocery"), "the next refresh must push it")
        assertEquals(
            setOf("a", "b"),
            NutritionStorageCodec.decodeGrocery(remote.data[Triple(householdId, weekKey, "grocery")]?.first ?: "")
                .map { it.id }.toSet(),
        )
        assertEquals(0, remote.blindUpsertCount)
    }

    @Test
    fun `S3 a grocery entry whose conditional push failed is never flushed blind in the same refresh`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemote(listOf(item("a")), remote)

        remote.offline = true
        a.repository.saveGroceryItems(listOf(item("a"), item("b"))) // queued
        remote.offline = false

        // The apply-merge push and the flush push both fail on this refresh
        remote.failNextUpserts = 2
        a.repository.refreshFromRemote()

        assertEquals(0, remote.blindUpsertCount, "a failed CAS push must not fall back to a blind upsert")
        assertTrue(a.engine.hasPending(householdId, weekKey, "grocery"))

        a.repository.refreshFromRemote() // recovered: goes out through the precondition
        assertTrue(!a.engine.hasPending(householdId, weekKey, "grocery"))
        assertEquals(0, remote.blindUpsertCount)
        assertEquals(
            setOf("a", "b"),
            NutritionStorageCodec.decodeGrocery(remote.data[Triple(householdId, weekKey, "grocery")]?.first ?: "")
                .map { it.id }.toSet(),
        )
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
        val row = NutritionWeekDataRow(
            householdId = householdId,
            weekKey = weekKey,
            dataKind = "grocery",
            payload = encode(item("a"), item("c")),
        )

        // Gate the first push so we can verify ordering through the mutex
        val gate = CompletableDeferred<Unit>()
        remote.gate = gate

        launch { a.repository.applyRemoteWeekData(row) }
        runCurrent() // apply starts, acquires mutex, enters upsert, blocks on gate

        launch { a.repository.saveGroceryItems(listOf(item("a"), item("b"), item("c"), item("z"))) }
        runCurrent() // save starts, tries to acquire mutex, blocked by apply holding it

        // Release gate → apply finishes its push, markSynced, releases mutex
        gate.complete(Unit)
        advanceUntilIdle() // save acquires mutex, pushes, finishes

        // Order: apply's merged payload first, save's newList second
        assertEquals(2, remote.upsertLog.size, "both pushes must execute")
        val mergedItems = NutritionStorageCodec.decodeGrocery(remote.upsertLog[0])
        val saveItems = NutritionStorageCodec.decodeGrocery(remote.upsertLog[1])
        assertEquals(listOf("b", "a", "c"), mergedItems.map { it.id })
        assertEquals(listOf("a", "b", "c", "z"), saveItems.map { it.id })

        // Final remote matches save's list
        assertRemoteItems(remote, listOf("a", "b", "c", "z"))
        a.assertGroceryIs(item("a"), item("b"), item("c"), item("z"))
    }

    /** Sequential id generator so carried items stay unique. */
    private fun sequentialId(prefix: String): () -> String {
        var seq = 0
        return { "$prefix-${seq++}" }
    }

    @Test
    fun `S4 incident - partner carries and clears, returning device with stale marker does not re-carry`() = runTest {
        val remote = FakeRemoteDataSource()
        val today = LocalDate(2026, 6, 17) // Wednesday
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"

        // Previous week's groceries on the remote: Milk and Eggs (both unchecked).
        remote.set(
            Triple(householdId, previousWeek, "grocery"),
            encode(item("milk", "Milk"), item("eggs", "Eggs")),
            "t1",
        )

        // Partner device A binds to the current week and carries (claim -> true).
        val settingsA = MapSettings()
        val a = device(settings = settingsA, remote = remote, weekKey = currentWeek)
        val runnerA = NutritionWeekMaintenanceRunner(
            settings = settingsA,
            maintenanceStore = NutritionWeekMaintenanceStore(settingsA),
            remoteApi = remote,
            newItemId = sequentialId("a"),
        )
        runnerA.runForCurrentWeek(a.repository, householdId, currentWeek, today = today)
        // merged order: eggs (id a-1) prepended after milk (id a-0); original labels kept.
        a.assertGroceryIs(item("a-1", "Eggs"), item("a-0", "Milk"))
        assertRemoteItems(remote, listOf("a-1", "a-0"), weekKey = currentWeek)

        // Partner clears the list — the empty list is now on the remote.
        a.repository.saveGroceryItems(emptyList())
        assertRemoteItems(remote, emptyList(), weekKey = currentWeek)

        // Returning device B still has last week's local marker, so it would retry the carry.
        val settingsB = MapSettings()
        NutritionWeekMaintenanceStore(settingsB).setLastMaintainedWeekKey(householdId, previousWeek)
        val b = device(settings = settingsB, remote = remote, weekKey = currentWeek)
        val runnerB = NutritionWeekMaintenanceRunner(
            settings = settingsB,
            maintenanceStore = NutritionWeekMaintenanceStore(settingsB),
            remoteApi = remote,
            newItemId = sequentialId("b"),
        )
        runnerB.runForCurrentWeek(b.repository, householdId, currentWeek, today = today)

        // The returning device's list stays empty and the remote list stays empty.
        b.assertGroceryIs()
        assertRemoteItems(remote, emptyList(), weekKey = currentWeek)
        // B's marker is set (claim=false) so it will not retry.
        assertEquals(
            currentWeek,
            NutritionWeekMaintenanceStore(settingsB).lastMaintainedWeekKey(householdId),
            "M4: marker must be set when another device already claimed",
        )
    }

    // ================================================================
    // S3: Version-based optimistic concurrency tests
    // ================================================================

    @Test
    fun `S3 DoD — concurrent adds keep both items across devices`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)

        a.repository.saveGroceryItems(listOf(item("a")))
        b.repository.saveGroceryItems(listOf(item("b")))

        // Device B pushed second — CAS conflicted, merged, both items on server
        // Device A must refresh to see B's add
        a.repository.refreshFromRemote()

        // Both devices have both items (order may vary by merge)
        assertEquals(
            setOf("a", "b"),
            a.repository.currentGroceryItems().map { it.id }.toSet(),
        )
        assertEquals(
            setOf("a", "b"),
            b.repository.currentGroceryItems().map { it.id }.toSet(),
        )
        // Remote has both items; order depends on who pushed the merged result
        val remoteIds = remote.data
            .filter { (k, _) -> k.first == householdId && k.second == weekKey && k.third == "grocery" }
            .flatMap { (_, v) -> NutritionStorageCodec.decodeGrocery(v.first).map { it.id } }
        assertEquals(setOf("a", "b"), remoteIds.toSet(), "both items must be on server")
    }

    @Test
    fun `S3 negative control — concurrent adds fail without CAS`() = runTest {
        val remote = FakeRemoteDataSource()
        remote.casEnabled = false
        val a = device(remote = remote)
        val b = device(remote = remote)

        a.repository.saveGroceryItems(listOf(item("a")))
        b.repository.saveGroceryItems(listOf(item("b")))

        val remoteIds = remote.data.filter { (k, _) ->
            k.first == householdId && k.second == weekKey && k.third == "grocery"
        }.map { (_, v) -> NutritionStorageCodec.decodeGrocery(v.first).map { it.id } }
        assertEquals(1, remoteIds.size, "server should have exactly one grocery row")
    }

    @Test
    fun `S3 stale version — conflict merges and retry succeeds with higher version`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)

        a.repository.saveGroceryItems(listOf(item("a")))
        assertRemoteItems(remote, listOf("a"))
        assertEquals(1, a.store.lastSyncedVersion("grocery"))

        val staleSettings = MapSettings()
        val staleStore = NutritionLocalStore(staleSettings, householdId, weekKey)
        val staleOutbox = NutritionSyncOutbox(staleSettings)
        val staleEngine = NutritionSyncEngine(remote, staleOutbox, TestObservability.logger)
        val staleRepo = OfflineFirstNutritionRepository(
            localStore = staleStore,
            syncEngine = staleEngine,
            householdId = householdId,
            weekKey = weekKey,
            remoteEnabled = true,
        )

        staleRepo.saveGroceryItems(listOf(item("x")))

        // Remote has both items; merge order puts local before remote
        assertRemoteItems(remote, listOf("x", "a"))
        val remoteRow = remote.data[Triple(householdId, weekKey, "grocery")]
        assertTrue((remoteRow?.third ?: 0) > 1, "version must increase after conflict retry")
    }

    @Test
    fun `S3 old-client interop — blind upsert bumps version, new client merges`() = runTest {
        val remote = FakeRemoteDataSource()

        val settingsA = MapSettings()
        val outboxA = NutritionSyncOutbox(settingsA)
        val engineA = NutritionSyncEngine(remote, outboxA, TestObservability.logger)
        engineA.pushNowOrEnqueue(householdId, weekKey, "grocery", encode(item("a")))
        assertEquals(1L, remote.data[Triple(householdId, weekKey, "grocery")]?.third,
            "blind upsert must bump version to 1")

        val b = device(remote = remote)
        b.repository.saveGroceryItems(listOf(item("b")))

        // Merge order: local {b} first, then remote {a}
        assertRemoteItems(remote, listOf("b", "a"))
        b.assertGroceryIs(item("b"), item("a"))
    }

    @Test
    fun `S3 version null rows fall back to blind upsert`() = runTest {
        val remote = FakeRemoteDataSource()
        remote.data[Triple(householdId, weekKey, "grocery")] = Triple(
            encode(item("a")), "t0", null,
        )
        val a = device(remote = remote)
        a.repository.refreshFromRemote()

        a.repository.saveGroceryItems(listOf(item("a"), item("b")))

        // The fallback uploads the merged union (local-only items first, then the server's
        // list), not the raw local list; the content is the same, the order is the merge's.
        assertRemoteItems(remote, listOf("b", "a"))
        a.assertGroceryIs(item("b"), item("a"))
    }

    @Test
    fun `S3 version null fallback merges before the blind upsert — a partner's item is not overwritten`() = runTest {
        val remote = FakeRemoteDataSource()
        // Server without the version column: the row the partner wrote has no version
        remote.data[Triple(householdId, weekKey, "grocery")] = Triple(
            encode(item("a"), item("c")), "t0", null,
        )
        val a = device(remote = remote) // never pulled: no base, no stored version

        a.repository.saveGroceryItems(listOf(item("a"), item("b")))

        val expected = setOf("a", "b", "c")
        assertEquals(
            expected,
            NutritionStorageCodec.decodeGrocery(remote.data[Triple(householdId, weekKey, "grocery")]?.first ?: "")
                .map { it.id }.toSet(),
            "the blind upsert must carry the partner's c",
        )
        assertEquals(expected, a.repository.currentGroceryItems().map { it.id }.toSet())
        assertEquals(1, remote.blindUpsertCount)
    }

    @Test
    fun `S3 push with insert path — first write creates row with version 1`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)

        a.repository.saveGroceryItems(listOf(item("a")))

        val remoteRow = remote.data[Triple(householdId, weekKey, "grocery")]

        assertEquals(1L, remoteRow?.third ?: 0, "first write must set version to 1")
        assertEquals(1L, a.store.lastSyncedVersion("grocery"), "stored version must be 1")
    }
}
