package app.mymultiverse.kmp.data.sync

import app.mymultiverse.kmp.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.kmp.data.local.nutrition.PendingNutritionPush
import app.mymultiverse.kmp.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.kmp.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.kmp.domain.sync.NutritionSyncStatus
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class NutritionSyncEngineTest {

    @Test
    fun pushNowOrEnqueue_queuesWhenRemoteFails() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        val engine = NutritionSyncEngine(FailingRemote, outbox)

        engine.pushNowOrEnqueue("space-1", "2025-W24", "grocery", "payload")

        val status = engine.observeStatus().first()
        assertIs<NutritionSyncStatus.PendingPush>(status)
        assertEquals(1, status.pendingCount)
        assertEquals(1, outbox.pendingFor("space-1", "2025-W24").size)
    }

    @Test
    fun flushPending_clearsOutboxAfterSuccessfulPush() = runTest {
        val outbox = NutritionSyncOutbox(MapSettings())
        outbox.enqueue(
            PendingNutritionPush(
                spaceId = "space-1",
                weekKey = "2025-W24",
                dataKind = "grocery",
                payload = "payload",
                enqueuedAtEpochMs = 1L,
            ),
        )
        val remote = RecordingRemote()
        val engine = NutritionSyncEngine(remote, outbox)

        engine.flushPending("space-1", "2025-W24")

        assertEquals(0, outbox.pendingFor("space-1", "2025-W24").size)
        assertEquals(1, remote.upsertCount)
        assertEquals(NutritionSyncStatus.Idle, engine.observeStatus().first())
    }

    @Test
    fun pullRemote_reportsRemoteUnavailableWhenFetchFails() = runTest {
        val engine = NutritionSyncEngine(FailingFetchRemote, NutritionSyncOutbox(MapSettings()))
        var applied = false

        engine.pullRemote("space-1", "2025-W24") {
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
                    spaceId = "space-1",
                    weekKey = "2025-W24",
                    dataKind = "grocery",
                    payload = "old-grocery",
                    updatedAt = "2026-06-16T10:00:00Z",
                ),
                NutritionWeekDataRow(
                    spaceId = "space-1",
                    weekKey = "2025-W24",
                    dataKind = "meal_plan",
                    payload = "latest-meal-plan",
                    updatedAt = "2026-06-16T10:30:00Z",
                ),
                NutritionWeekDataRow(
                    spaceId = "space-1",
                    weekKey = "2025-W24",
                    dataKind = "grocery",
                    payload = "latest-grocery",
                    updatedAt = "2026-06-16T11:00:00Z",
                ),
            ),
        )
        val engine = NutritionSyncEngine(remote, NutritionSyncOutbox(MapSettings()))
        val applied = mutableListOf<NutritionWeekDataRow>()

        engine.pullRemote("space-1", "2025-W24") { row ->
            applied += row
        }

        assertEquals(listOf("latest-grocery", "latest-meal-plan"), applied.map { it.payload }.sorted())
    }

    private object FailingRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) {
            throw IllegalStateException("offline")
        }
    }

    private object FailingFetchRemote : NutritionRemoteDataSource {
        override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> {
            throw IllegalStateException("offline")
        }

        override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }

    private class RecordingRemote : NutritionRemoteDataSource {
        var upsertCount = 0

        override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()

        override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) {
            upsertCount++
        }
    }

    private class StaticRemote(
        private val rows: List<NutritionWeekDataRow>,
    ) : NutritionRemoteDataSource {
        override suspend fun fetchWeek(spaceId: String, weekKey: String): List<NutritionWeekDataRow> = rows

        override suspend fun upsert(spaceId: String, weekKey: String, dataKind: String, payload: String) = Unit
    }
}
