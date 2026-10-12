package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * S5: meal_plan gets the same compare-and-swap + cell-merge treatment S3 gave grocery.
 *
 * The unit of merge is the cell (day index x lunch|dinner). Concurrent edits to
 * different cells both survive; a same-cell conflict resolves in favour of the
 * device that pushed last; a cleared (empty) cell is a change like any other and
 * is never resurrected by a device whose base still held it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TwoDeviceMealPlanSyncTest {

    private val weekKey = "2026-W40"
    private val householdId = "household-1"

    private class FakeRemoteDataSource(
        val data: MutableMap<Triple<String, String, String>, Triple<String, String, Long?>> = mutableMapOf(),
    ) : NutritionRemoteDataSource {
        var offline = false
        var failFetchOnly = false
        var upsertCount = 0
        var failNextUpserts = 0
        val upsertLog: MutableList<String> = mutableListOf()
        var gate: CompletableDeferred<Unit>? = null
        private var clock = 0L
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
            return true
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

    // -- meal plan helpers --

    private fun day(lunch: String = "", dinner: String = "") = DayMeals(lunch, dinner)

    private fun plan(vararg days: DayMeals = arrayOf()): WeeklyMealPlan {
        val list = List(WeeklyMealPlan.DAYS_IN_WEEK) { index -> days.getOrElse(index) { DayMeals() } }
        return WeeklyMealPlan(weekKey, list)
    }

    private fun edit(plan: WeeklyMealPlan, dayIndex: Int, lunch: String? = null, dinner: String? = null): WeeklyMealPlan {
        val days = plan.days.mapIndexed { index, d ->
            if (index == dayIndex) DayMeals(lunch ?: d.lunch, dinner ?: d.dinner) else d
        }
        return plan.copy(days = days)
    }

    private fun encode(plan: WeeklyMealPlan): String = NutritionStorageCodec.encodeMealPlan(plan)

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

    private fun DeviceComponents.seedRemotePlan(
        remote: FakeRemoteDataSource,
        plan: WeeklyMealPlan,
        version: Long? = 0,
    ) {
        val payload = encode(plan)
        remote.set(Triple(householdId, weekKey, "meal_plan"), payload, "t0", version)
        store.saveMealPlan(plan)
        store.markSynced("meal_plan", payload, version)
    }

    private fun serverPlan(remote: FakeRemoteDataSource): WeeklyMealPlan {
        val row = remote.data[Triple(householdId, weekKey, "meal_plan")]
            ?: error("no meal_plan row on the fake server")
        return NutritionStorageCodec.decodeMealPlan(weekKey, row.first)
    }

// -- Test cases --

    @Test
    fun `S5 DoD — concurrent edits to different cells both survive`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        a.seedRemotePlan(remote, plan())
        b.seedRemotePlan(remote, plan())

        // A edits Monday lunch, B edits Friday dinner — neither has seen the other
        a.repository.saveMealPlan(edit(plan(), dayIndex = 0, lunch = "Lentil soup"))
        b.repository.saveMealPlan(edit(plan(), dayIndex = 4, dinner = "Pizza"))

        a.repository.refreshFromRemote()

        // Both devices keep both edits
        assertEquals("Lentil soup", a.repository.currentMealPlan().days[0].lunch, "A's Monday lunch")
        assertEquals("Pizza", a.repository.currentMealPlan().days[4].dinner, "B's Friday dinner on A")
        assertEquals("Lentil soup", b.repository.currentMealPlan().days[0].lunch, "A's Monday lunch on B")
        assertEquals("Pizza", b.repository.currentMealPlan().days[4].dinner, "B's Friday dinner")

        // The server holds both edits
        val server = serverPlan(remote)
        assertEquals("Lentil soup", server.days[0].lunch)
        assertEquals("Pizza", server.days[4].dinner)
        assertEquals(0, remote.blindUpsertCount, "no write may bypass the version precondition")
    }

    @Test
    fun `S5 negative control — different-cell edits fail without CAS (last writer wins)`() = runTest {
        val remote = FakeRemoteDataSource()
        remote.casEnabled = false
        val a = device(remote = remote)
        val b = device(remote = remote)
        a.seedRemotePlan(remote, plan())
        b.seedRemotePlan(remote, plan())

        a.repository.saveMealPlan(edit(plan(), dayIndex = 0, lunch = "Lentil soup"))
        b.repository.saveMealPlan(edit(plan(), dayIndex = 4, dinner = "Pizza"))

        a.repository.refreshFromRemote()

        // Blind upsert: B's whole-plan write erased A's Monday edit
        assertEquals("", a.repository.currentMealPlan().days[0].lunch, "A's edit must be lost without CAS")
        assertEquals("Pizza", a.repository.currentMealPlan().days[4].dinner)
    }

    @Test
    fun `S5 DoD — same cell edited by both — the last pusher wins everywhere`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val base = plan(day(lunch = "Soup"))
        a.seedRemotePlan(remote, base)
        b.seedRemotePlan(remote, base)

        a.repository.saveMealPlan(edit(base, dayIndex = 0, lunch = "Pasta"))
        b.repository.saveMealPlan(edit(base, dayIndex = 0, lunch = "Risotto")) // conflicts, merges, retries

        a.repository.refreshFromRemote()

        // B pushed last, so B's value resolves the same-cell conflict on every side
        assertEquals("Risotto", a.repository.currentMealPlan().days[0].lunch)
        assertEquals("Risotto", b.repository.currentMealPlan().days[0].lunch)
        assertEquals("Risotto", serverPlan(remote).days[0].lunch)
        assertEquals(0, remote.blindUpsertCount)
    }
@Test
    fun `S5 DoD — offline device returning after a partner edit keeps both sides' cells`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val base = plan(day(lunch = "Soup"))
        a.seedRemotePlan(remote, base)
        b.seedRemotePlan(remote, base)

        // B edits Tuesday lunch while A is offline — B pushes to the server
        b.repository.saveMealPlan(edit(base, dayIndex = 2, lunch = "Bowl"))

        remote.offline = true
        a.repository.saveMealPlan(edit(base, dayIndex = 5, dinner = "Stew")) // enqueued
        remote.offline = false

        a.repository.refreshFromRemote()

        // A's Monday "Soup" (unchanged), B's Tuesday "Bowl" and A's Friday "Stew" all present
        val aPlan = a.repository.currentMealPlan()
        assertEquals("Soup", aPlan.days[0].lunch)
        assertEquals("Bowl", aPlan.days[2].lunch, "partner's edit must not be erased")
        assertEquals("Stew", aPlan.days[5].dinner, "own offline edit must survive")

        // The merge was pushed: A's Friday Stew is now on the server too
        val server = serverPlan(remote)
        assertEquals("Bowl", server.days[2].lunch)
        assertEquals("Stew", server.days[5].dinner)

        b.repository.refreshFromRemote()
        val bPlan = b.repository.currentMealPlan()
        assertEquals("Bowl", bPlan.days[2].lunch)
        assertEquals("Stew", bPlan.days[5].dinner, "B must see the returning device's edit too")
        assertFalse(a.engine.hasPending(householdId, weekKey, "meal_plan"), "outbox must drain after the push")
    }

    @Test
    fun `S5 DoD — cleared cell propagates and a stale device cannot resurrect it`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        val base = plan(day(lunch = "Soup"))
        a.seedRemotePlan(remote, base)
        b.seedRemotePlan(remote, base)

        // A clears Monday lunch and pushes the empty cell to the server
        a.repository.saveMealPlan(edit(base, dayIndex = 0, lunch = ""))

        // B never refreshed: still holds "Soup" locally, edits Thursday dinner offline
        remote.offline = true
        b.repository.saveMealPlan(edit(base, dayIndex = 3, dinner = "Pizza")) // enqueued
        remote.offline = false

        b.repository.refreshFromRemote()

        // Monday stays empty on B (not resurrected by B's stale base); Pizza survives
        val bPlan = b.repository.currentMealPlan()
        assertEquals("", bPlan.days[0].lunch, "a stale device must not resurrect a cleared cell")
        assertEquals("Pizza", bPlan.days[3].dinner, "own offline edit must survive")

        a.repository.refreshFromRemote()
        val aPlan = a.repository.currentMealPlan()
        assertEquals("", aPlan.days[0].lunch)
        assertEquals("Pizza", aPlan.days[3].dinner, "A must see B's Thursday edit")

        val server = serverPlan(remote)
        assertEquals("", server.days[0].lunch, "server must keep the cleared cell empty")
        assertEquals("Pizza", server.days[3].dinner)
        assertEquals(0, remote.blindUpsertCount)
    }

    @Test
    fun `S5 bounded retry — always-conflict server enqueues and leaves the base unchanged`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemotePlan(remote, plan())
        val seedPayload = a.store.lastSyncedPayload("meal_plan")
        remote.alwaysConflict = true

        a.repository.saveMealPlan(edit(plan(), dayIndex = 0, lunch = "Pasta"))

        assertEquals(3, remote.casAttempts, "bounded: three conditional attempts, no more")
        assertEquals(0, remote.blindUpsertCount)
        assertTrue(a.engine.hasPending(householdId, weekKey, "meal_plan"), "the write must stay queued")
        // Base neither advanced nor clobbered by the failed/enqueued push
        assertEquals(seedPayload, a.store.lastSyncedPayload("meal_plan"), "base payload must stay at the seed")
        assertEquals(0L, a.store.lastSyncedVersion("meal_plan"), "base version must stay at the seed")
        // The offline edit is still visible locally
        assertEquals("Pasta", a.repository.currentMealPlan().days[0].lunch)
        assertEquals("", serverPlan(remote).days[0].lunch, "server must be untouched")
    }
@Test
    fun `S5 version-null server — legacy upsert of the MERGED plan, partner cells kept`() = runTest {
        val remote = FakeRemoteDataSource()
        // Server without the version column: the row the partner wrote has no version
        remote.set(
            Triple(householdId, weekKey, "meal_plan"),
            encode(plan(day(), day(), day(), day(dinner = "Soup"))),
            "t0",
            version = null,
        )
        val a = device(remote = remote) // never pulled: no base, no stored version

        a.repository.saveMealPlan(edit(plan(), dayIndex = 0, lunch = "Pasta"))

        // The blind upsert must carry the MERGED plan (local add + partner's cell)
        val server = serverPlan(remote)
        assertEquals("Pasta", server.days[0].lunch, "local edit must be present on the server")
        assertEquals("Soup", server.days[3].dinner, "a partner cell must not be overwritten by the fallback")
        assertEquals("Pasta", a.repository.currentMealPlan().days[0].lunch)
        assertEquals("Soup", a.repository.currentMealPlan().days[3].dinner)
        assertEquals(1, remote.blindUpsertCount, "version-null rows degrade to exactly one blind upsert")
    }

    @Test
    fun `S5 realtime apply — a remote meal_plan row merges a pending offline edit and pushes the merged plan`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        a.seedRemotePlan(remote, plan(day(lunch = "Soup")))

        // A edits Friday dinner offline -> queued, server unchanged
        remote.offline = true
        a.repository.saveMealPlan(edit(plan(day(lunch = "Soup")), dayIndex = 4, dinner = "Pizza"))
        remote.offline = false

        // Partner pushes Tuesday lunch "Bowl"; arrives as a realtime row (version 2 from the partner write)
        val second = device(remote = remote)
        second.seedRemotePlan(remote, plan(day(lunch = "Soup")))
        second.repository.saveMealPlan(edit(plan(day(lunch = "Soup")), dayIndex = 2, lunch = "Bowl"))

        a.repository.applyRemoteWeekData(
            NutritionWeekDataRow(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = "meal_plan",
                payload = encode(plan(day(lunch = "Soup"), day(), day(lunch = "Bowl"))),
                version = 1L,
            ),
        )

        val aPlan = a.repository.currentMealPlan()
        assertEquals("Soup", aPlan.days[0].lunch)
        assertEquals("Bowl", aPlan.days[2].lunch, "realtime remote edit must be applied")
        assertEquals("Pizza", aPlan.days[4].dinner, "pending offline edit must survive the realtime merge")

        // The merged result differs from the remote row, so it is CAS-pushed
        val server = serverPlan(remote)
        assertEquals("Bowl", server.days[2].lunch)
        assertEquals("Pizza", server.days[4].dinner)
        assertFalse(a.engine.hasPending(householdId, weekKey, "meal_plan"), "push success drains the outbox")
    }

    @Test
    fun `S5 convergence — no echo ping-pong on a second refresh`() = runTest {
        val remote = FakeRemoteDataSource()
        val a = device(remote = remote)
        val b = device(remote = remote)
        a.seedRemotePlan(remote, plan())
        b.seedRemotePlan(remote, plan())

        a.repository.saveMealPlan(edit(plan(), dayIndex = 0, lunch = "Pasta"))
        b.repository.saveMealPlan(edit(plan(), dayIndex = 4, dinner = "Pizza"))

        val upsertsAfterBoth = remote.upsertCount
        a.repository.refreshFromRemote()
        b.repository.refreshFromRemote()
        a.repository.refreshFromRemote()
        b.repository.refreshFromRemote()

        assertEquals(upsertsAfterBoth, remote.upsertCount, "refresh after convergence must not write")
        assertEquals("Pasta", a.repository.currentMealPlan().days[0].lunch)
        assertEquals("Pizza", b.repository.currentMealPlan().days[4].dinner)
    }
}