package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.local.nutrition.PendingNutritionPush
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.sync.NutritionSyncStatus
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NutritionSyncEngineTest {

    @Test
    fun pushNowOrEnqueue_queuesWhenRemoteFails() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        val engine = NutritionSyncEngine(FailingRemote, outbox, TestObservability.logger)

        engine.pushNowOrEnqueue("household-1", "2025-W24", "grocery", "payload")

        val status = engine.observeStatus().first()
        assertIs<NutritionSyncStatus.PendingPush>(status)
        assertEquals(1, status.pendingCount)
        assertEquals(1, outbox.pendingFor("household-1", "2025-W24").size)
    }

    @Test
    fun flushPending_clearsOutboxAfterSuccessfulPush() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(
            PendingNutritionPush(
                householdId = "household-1",
                weekKey = "2025-W24",
                dataKind = "grocery",
                payload = "payload",
                enqueuedAtEpochMs = 1L,
            ),
        )
        val remote = RecordingRemote()
        val engine = NutritionSyncEngine(remote, outbox, TestObservability.logger)

        val result = engine.flushPending("household-1", "2025-W24")

        assertEquals(1, result.flushed)
        assertEquals(0, outbox.pendingFor("household-1", "2025-W24").size)
        assertEquals(1, remote.upsertCount)
        assertEquals(NutritionSyncStatus.Idle, engine.observeStatus().first())
    }

    @Test
    fun flushPending_continuesAfterSingleKindFailure() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(
            PendingNutritionPush(
                householdId = "household-1",
                weekKey = "2025-W24",
                dataKind = "grocery",
                payload = "grocery-payload",
                enqueuedAtEpochMs = 1L,
            ),
        )
        outbox.enqueue(
            PendingNutritionPush(
                householdId = "household-1",
                weekKey = "2025-W24",
                dataKind = "meal_plan",
                payload = "meal-plan-payload",
                enqueuedAtEpochMs = 2L,
            ),
        )
        val remote = SelectiveFailingRemote(failKinds = setOf("grocery"))
        val engine = NutritionSyncEngine(remote, outbox, TestObservability.logger)

        val result = engine.flushPending("household-1", "2025-W24")

        assertEquals(1, result.flushed)
        assertEquals(1, result.failed)
        assertEquals(1, outbox.pendingFor("household-1", "2025-W24").size)
        assertEquals("grocery", outbox.pendingFor("household-1", "2025-W24").single().dataKind)
        assertIs<NutritionSyncStatus.PendingPush>(engine.observeStatus().first())
    }

    @Test
    fun flushAllPendingForHousehold_flushesEveryWeekKey() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(
            PendingNutritionPush("household-1", "2025-W24", "grocery", "w24", 1L),
        )
        outbox.enqueue(
            PendingNutritionPush("household-1", "2025-W25", "meal_plan", "w25", 2L),
        )
        val remote = RecordingRemote()
        val engine = NutritionSyncEngine(remote, outbox, TestObservability.logger)

        val result = engine.flushAllPendingForHousehold("household-1")

        assertEquals(2, result.flushed)
        assertTrue(outbox.pendingForHousehold("household-1").isEmpty())
        assertEquals(2, remote.upsertCount)
    }

    @Test
    fun flushPending_skipsStalePendingWhenRemoteIsNewer() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(
            PendingNutritionPush(
                householdId = "household-1",
                weekKey = "2025-W24",
                dataKind = "grocery",
                payload = "stale-local",
                enqueuedAtEpochMs = 1_000L,
            ),
        )
        val remote = RecordingRemote(
            fetchRows = listOf(
                NutritionWeekDataRow(
                    householdId = "household-1",
                    weekKey = "2025-W24",
                    dataKind = "grocery",
                    payload = "newer-remote",
                    updatedAt = "2026-06-16T12:00:00Z",
                ),
            ),
        )
        val engine = NutritionSyncEngine(remote, outbox, TestObservability.logger)

        val result = engine.flushPending("household-1", "2025-W24")

        assertEquals(0, result.flushed)
        assertEquals(1, result.skippedStale)
        assertEquals(0, remote.upsertCount)
        assertTrue(outbox.pendingFor("household-1", "2025-W24").isEmpty())
    }

    @Test
    fun pushNowOrEnqueue_successClearsStalePendingForSameKind() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(
            PendingNutritionPush(
                householdId = "household-1",
                weekKey = "2025-W24",
                dataKind = "grocery",
                payload = "stale",
                enqueuedAtEpochMs = 1L,
            ),
        )
        val remote = RecordingRemote()
        val engine = NutritionSyncEngine(remote, outbox, TestObservability.logger)

        engine.pushNowOrEnqueue("household-1", "2025-W24", "grocery", "latest")

        assertEquals(emptyList(), outbox.pendingFor("household-1", "2025-W24"))
        assertEquals(listOf("latest"), remote.upserts.map { it.payload })
        assertEquals(NutritionSyncStatus.Idle, engine.observeStatus().first())
    }

    @Test
    fun pullRemote_reportsRemoteUnavailableWhenFetchFails() = runTest {
        val engine = NutritionSyncEngine(FailingFetchRemote, NutritionSyncOutbox(MapSettings()), TestObservability.logger)
        var applied = false

        engine.pullRemote("household-1", "2025-W24") {
            applied = true
        }

        assertFalse(applied)
        assertEquals(NutritionSyncStatus.RemoteUnavailable, engine.observeStatus().first())
    }

    @Test
    fun pullRemote_appliesLatestRowForEachDataKind() = runTest {
        val remote = StaticRemote(
            listOf(
                NutritionWeekDataRow(
                    householdId = "household-1",
                    weekKey = "2025-W24",
                    dataKind = "grocery",
                    payload = "old-grocery",
                    updatedAt = "2026-06-16T10:00:00Z",
                ),
                NutritionWeekDataRow(
                    householdId = "household-1",
                    weekKey = "2025-W24",
                    dataKind = "meal_plan",
                    payload = "latest-meal-plan",
                    updatedAt = "2026-06-16T10:30:00Z",
                ),
                NutritionWeekDataRow(
                    householdId = "household-1",
                    weekKey = "2025-W24",
                    dataKind = "grocery",
                    payload = "latest-grocery",
                    updatedAt = "2026-06-16T11:00:00Z",
                ),
            ),
        )
        val engine = NutritionSyncEngine(remote, NutritionSyncOutbox(MapSettings()), TestObservability.logger)
        val applied = mutableListOf<NutritionWeekDataRow>()

        engine.pullRemote("household-1", "2025-W24") { row ->
            applied += row
        }

        assertEquals(listOf("latest-grocery", "latest-meal-plan"), applied.map { it.payload }.sorted())
    }

    @Test
    fun hasPending_returnsFalseWhenOutboxEmpty() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        val engine = NutritionSyncEngine(null, outbox, TestObservability.logger)

        assertFalse(engine.hasPending("household-1", "2025-W24", "grocery"))
    }

    @Test
    fun hasPending_returnsTrueAfterEnqueue() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(
            PendingNutritionPush(
                householdId = "household-1", weekKey = "2025-W24", dataKind = "grocery",
                payload = "payload", enqueuedAtEpochMs = 1L,
            ),
        )
        val engine = NutritionSyncEngine(null, outbox, TestObservability.logger)

        assertTrue(engine.hasPending("household-1", "2025-W24", "grocery"))
        assertFalse(engine.hasPending("household-1", "2025-W24", "meal_plan"))
    }

    @Test
    fun shouldApplyRemoteOverPending_prefersNewerRemoteTimestamp() = runTest {
        val engine = NutritionSyncEngine(null, NutritionSyncOutbox(MapSettings()), TestObservability.logger)
        val pending = PendingNutritionPush(
            householdId = "h1",
            weekKey = "2025-W24",
            dataKind = "grocery",
            payload = "local",
            enqueuedAtEpochMs = Instant.parse("2026-06-16T11:00:00Z").toEpochMilliseconds(),
        )
        val newerRemote = NutritionWeekDataRow(
            householdId = "h1",
            weekKey = "2025-W24",
            dataKind = "grocery",
            payload = "remote",
            updatedAt = "2026-06-16T12:00:00Z",
        )
        val olderRemote = newerRemote.copy(updatedAt = "2026-06-16T10:00:00Z")

        assertTrue(engine.shouldApplyRemoteOverPending(newerRemote, pending))
        assertFalse(engine.shouldApplyRemoteOverPending(olderRemote, pending))
    }

    private object FailingRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) {
            throw IllegalStateException("offline")
        }
    }

    private object FailingFetchRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            throw IllegalStateException("offline")
        }

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private class RecordingRemote(
        private val fetchRows: List<NutritionWeekDataRow> = emptyList(),
    ) : NutritionRemoteDataSource {
        var upsertCount = 0
        val upserts = mutableListOf<PendingNutritionPush>()

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = fetchRows

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) {
            upsertCount++
            upserts += PendingNutritionPush(
                householdId = householdId,
                weekKey = weekKey,
                dataKind = dataKind,
                payload = payload,
                enqueuedAtEpochMs = 0L,
            )
        }
    }

    private class SelectiveFailingRemote(
        private val failKinds: Set<String>,
    ) : NutritionRemoteDataSource {
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) {
            if (dataKind in failKinds) throw IllegalStateException("offline")
        }
    }

    private class StaticRemote(
        private val rows: List<NutritionWeekDataRow>,
    ) : NutritionRemoteDataSource {
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = rows

        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }
}
