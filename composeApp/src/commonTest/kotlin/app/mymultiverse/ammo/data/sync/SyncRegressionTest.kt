package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
import app.mymultiverse.ammo.data.remote.nutrition.pushViaUpsert
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SyncRegressionTest {
    private val hh = "household-1"
    private val wk = "2026-W40"

    private class Remote : NutritionRemoteDataSource {

        override suspend fun pushIfVersion(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
            expectedVersion: Long?,
        ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
        val rows = mutableMapOf<String, Pair<String, String>>()
        var offline = false
        var failUpserts = false
        var clock = 0
        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            if (offline) throw IllegalStateException("offline")
            return rows.map {
                NutritionWeekDataRow(householdId = householdId, weekKey = weekKey, dataKind = it.key,
                    payload = it.value.first, updatedAt = it.value.second)
            }
        }
        override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean = true
        override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) {
            if (offline || failUpserts) throw IllegalStateException("push failed")
            clock++
            rows[dataKind] = payload to "t$clock"
        }
    }

    private fun dev(settings: MapSettings, remote: Remote) = OfflineFirstNutritionRepository(
        localStore = NutritionLocalStore(settings, hh, wk),
        syncEngine = NutritionSyncEngine(remote, NutritionSyncOutbox(settings), TestObservability.logger),
        householdId = hh, weekKey = wk, remoteEnabled = true,
    )

    private fun i(id: String) = GroceryItem(id, id, false, false)

    @Test
    fun ownOfflineAddSurvivesWhenPushesKeepFailing() = runTest {
        val remote = Remote()
        val a = dev(MapSettings(), remote)
        val b = dev(MapSettings(), remote)
        a.saveGroceryItems(listOf(i("a"), i("b"), i("c"), i("d")))
        b.refreshFromRemote()
        remote.offline = true
        a.saveGroceryItems(listOf(i("a"), i("b"), i("c"), i("d"), i("x")))
        remote.offline = false
        b.saveGroceryItems(listOf(i("a"), i("b"), i("c")))
        remote.failUpserts = true
        a.refreshFromRemote()
        remote.failUpserts = false
        b.saveGroceryItems(listOf(i("a"), i("b"), i("c"), i("y")))
        remote.failUpserts = true
        a.refreshFromRemote()
        val ids = a.currentGroceryItems().map { it.id }
        assertTrue("x" in ids, "A's offline add x was silently dropped; list=$ids")
    }

    @Test
    fun refreshFlushesPendingWhenRemoteHasNoRow() = runTest {
        val remote = Remote()
        val a = dev(MapSettings(), remote)
        remote.offline = true
        a.saveGroceryItems(listOf(i("a")))
        remote.offline = false
        a.refreshFromRemote()
        assertTrue(remote.rows["grocery"] != null, "pending push was never flushed after a successful pull")
    }
}