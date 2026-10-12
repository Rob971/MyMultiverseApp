package app.mymultiverse.ammo.data.sync

import app.mymultiverse.ammo.data.local.nutrition.NutritionLocalStore
import app.mymultiverse.ammo.data.local.nutrition.NutritionSyncOutbox
import app.mymultiverse.ammo.data.local.nutrition.NutritionWeekMaintenanceStore
import app.mymultiverse.ammo.data.observability.TestObservability
import app.mymultiverse.ammo.data.remote.nutrition.NutritionRemoteDataSource
import app.mymultiverse.ammo.data.remote.nutrition.PushResult
import app.mymultiverse.ammo.data.remote.nutrition.pushViaUpsert
import app.mymultiverse.ammo.data.repository.NutritionStorageCodec
import app.mymultiverse.ammo.data.supabase.dto.NutritionWeekDataRow
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.GroceryItem
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionWeekMaintenanceRunnerTest {

    @Test
    fun runForCurrentWeek_carriesUncheckedGroceryAndPrunesPastMeals() = runTest {
        val settings = MapSettings()
        val householdId = "household-a"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)

        val previousStore = NutritionLocalStore(settings, householdId, previousWeek)
        previousStore.saveGroceryItems(
            listOf(
                GroceryItem(id = "1", label = "Milk"),
                GroceryItem(id = "2", label = "Bread", isChecked = true),
            ),
        )

        val repository = offlineRepository(settings, householdId, currentWeek)
        repository.saveMealPlan(
            WeeklyMealPlan(
                weekKey = currentWeek,
                days = List(7) { index ->
                    DayMeals(lunch = if (index < 3) "Lunch $index" else "", dinner = "")
                },
            ),
        )

        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = null,
            newItemId = { "carried" },
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = currentWeek,
            today = wednesday,
        )

        assertEquals(listOf("Milk"), repository.currentGroceryItems().map { it.label })
        assertEquals("", repository.currentMealPlan().days[0].lunch)
        assertEquals("", repository.currentMealPlan().days[1].lunch)
        assertEquals("Lunch 2", repository.currentMealPlan().days[2].lunch)
        assertEquals(
            currentWeek,
            NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId),
        )
    }

    @Test
    fun runForCurrentWeek_skipsWhenViewingNonCurrentWeek() = runTest {
        val settings = MapSettings()
        val householdId = "household-b"
        val pastWeek = "2026-06-08"
        val previousWeek = "2026-06-01"

        NutritionLocalStore(settings, householdId, previousWeek).saveGroceryItems(
            listOf(GroceryItem(id = "1", label = "Milk")),
        )

        val repository = offlineRepository(settings, householdId, pastWeek)
        repository.saveMealPlan(
            WeeklyMealPlan(
                weekKey = pastWeek,
                days = listOf(DayMeals(lunch = "Old lunch")) + List(6) { DayMeals() },
            ),
        )

        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = null,
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = pastWeek,
            today = LocalDate(2026, 6, 17),
        )

        assertTrue(repository.currentGroceryItems().isEmpty())
        assertEquals("Old lunch", repository.currentMealPlan().days[0].lunch)
        assertEquals(null, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))
    }

    @Test
    fun runForCurrentWeek_prefersRemotePreviousWeekGrocery() = runTest {
        val settings = MapSettings()
        val householdId = "household-c"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)
        val repository = offlineRepository(settings, householdId, currentWeek)
        val remote = RecordingRemote(
            rowsByWeek = mapOf(
                previousWeek to listOf(
                    NutritionWeekDataRow(
                        householdId = householdId,
                        weekKey = previousWeek,
                        dataKind = "grocery",
                        payload = NutritionStorageCodec.encodeGrocery(
                            listOf(GroceryItem(id = "remote", label = "Remote eggs")),
                        ),
                        updatedAt = "2026-06-14T12:00:00Z",
                    ),
                ),
            ),
        )

        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = remote,
            newItemId = { "carried-remote" },
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = currentWeek,
            today = wednesday,
        )

        assertEquals(listOf("Remote eggs"), repository.currentGroceryItems().map { it.label })
        assertEquals(1, remote.fetchCount)
    }

    @Test
    fun runForCurrentWeek_doesNotMarkMaintainedWhenRemoteFetchFails() = runTest {
        val settings = MapSettings()
        val householdId = "household-fail"
        val currentWeek = "2026-06-15"
        val wednesday = LocalDate(2026, 6, 17)
        val repository = offlineRepository(settings, householdId, currentWeek)

        val failingRemote = object : NutritionRemoteDataSource {

            override suspend fun pushIfVersion(
                householdId: String,
                weekKey: String,
                dataKind: String,
                payload: String,
                expectedVersion: Long?,
            ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
            override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
                throw RuntimeException("network_unavailable")
            }
            override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean = true
            override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
        }

        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = failingRemote,
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = currentWeek,
            today = wednesday,
        )

        // Maintain mark must NOT be set — carry will retry on next session bind.
        assertEquals(null, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))
        // Grocery list must be untouched.
        assertTrue(repository.currentGroceryItems().isEmpty())
    }

    @Test
    fun runForCurrentWeek_multipleCarriedItemsHaveUniqueIds() = runTest {
        val settings = MapSettings()
        val householdId = "household-d"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)

        NutritionLocalStore(settings, householdId, previousWeek).saveGroceryItems(
            listOf(
                GroceryItem(id = "p1", label = "Milk"),
                GroceryItem(id = "p2", label = "Eggs"),
                GroceryItem(id = "p3", label = "Butter"),
            ),
        )

        val repository = offlineRepository(settings, householdId, currentWeek)
        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = null,
        )

        runner.runForCurrentWeek(
            repository = repository,
            householdId = householdId,
            weekKey = currentWeek,
            today = wednesday,
        )

        val ids = repository.currentGroceryItems().map { it.id }
        assertEquals(ids.size, ids.toSet().size, "Carried items must have unique IDs: $ids")
    }

    @Test
    fun runForCurrentWeek_claimsExactlyOnce_andCarriesWhenClaimSucceeds() = runTest {
        val settings = MapSettings()
        val householdId = "household-claim"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)
        val repository = offlineRepository(settings, householdId, currentWeek)
        val remote = RecordingRemote(
            rowsByWeek = mapOf(
                previousWeek to listOf(
                    NutritionWeekDataRow(
                        householdId = householdId,
                        weekKey = previousWeek,
                        dataKind = "grocery",
                        payload = NutritionStorageCodec.encodeGrocery(listOf(GroceryItem(id = "p1", label = "Milk"))),
                        updatedAt = "2026-06-14T12:00:00Z",
                    ),
                ),
            ),
        )
        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = remote,
            newItemId = { "carried" },
        )

        runner.runForCurrentWeek(repository, householdId, currentWeek, today = wednesday)

        assertEquals(1, remote.claimCount, "the claim must be recorded exactly once")
        assertEquals(listOf("Milk"), repository.currentGroceryItems().map { it.label })
        assertEquals(currentWeek, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))
    }

    @Test
    fun runForCurrentWeek_claimThrows_noCarryNoMarker_thenRetrySucceeds() = runTest {
        val settings = MapSettings()
        val householdId = "household-claim-retry"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)
        val repository = offlineRepository(settings, householdId, currentWeek)
        val remote = RecordingRemote(
            rowsByWeek = mapOf(
                previousWeek to listOf(
                    NutritionWeekDataRow(
                        householdId = householdId,
                        weekKey = previousWeek,
                        dataKind = "grocery",
                        payload = NutritionStorageCodec.encodeGrocery(listOf(GroceryItem(id = "p1", label = "Milk"))),
                        updatedAt = "2026-06-14T12:00:00Z",
                    ),
                ),
            ),
        )
        remote.failNextClaims = 1
        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = remote,
            newItemId = { "carried" },
        )

        runner.runForCurrentWeek(repository, householdId, currentWeek, today = wednesday)
        // M3: claim throws -> no carry, no local marker
        assertTrue(repository.currentGroceryItems().isEmpty(), "claim throw must not carry")
        assertEquals(null, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))

        // Next bind retries and succeeds.
        runner.runForCurrentWeek(repository, householdId, currentWeek, today = wednesday)
        assertEquals(listOf("Milk"), repository.currentGroceryItems().map { it.label })
        assertEquals(currentWeek, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))
        assertEquals(1, remote.claimCount, "one successful claim after the failed attempt")
    }

    @Test
    fun runForCurrentWeek_fetchThrows_claimNeverCalled() = runTest {
        val settings = MapSettings()
        val householdId = "household-claim-order"
        val currentWeek = "2026-06-15"
        val wednesday = LocalDate(2026, 6, 17)
        val repository = offlineRepository(settings, householdId, currentWeek)

        val fetchFailingRemote = object : NutritionRemoteDataSource {

            override suspend fun pushIfVersion(
                householdId: String,
                weekKey: String,
                dataKind: String,
                payload: String,
                expectedVersion: Long?,
            ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
            var claimCount = 0
            override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
                throw RuntimeException("network_unavailable")
            }
            override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean {
                claimCount++
                return true
            }
            override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
        }
        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = fetchFailingRemote,
        )

        runner.runForCurrentWeek(repository, householdId, currentWeek, today = wednesday)

        // M2: a failed previous-week fetch must NOT burn the claim.
        assertEquals(0, fetchFailingRemote.claimCount, "claim must come after the successful fetch")
        assertEquals(null, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))
    }

    @Test
    fun runForCurrentWeek_claimFalse_setsMarkerWithoutCarrying() = runTest {
        val settings = MapSettings()
        val householdId = "household-m4"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)
        // Local previous-week items exist, so WITHOUT the claim a carry would happen.
        NutritionLocalStore(settings, householdId, previousWeek).saveGroceryItems(
            listOf(GroceryItem(id = "p1", label = "Milk")),
        )
        val repository = offlineRepository(settings, householdId, currentWeek)
        // Another device already claimed this week.
        val remote = RecordingRemote(
            rowsByWeek = emptyMap(),
            claims = mutableMapOf(householdId to mutableSetOf(currentWeek)),
        )
        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = remote,
            newItemId = { "carried" },
        )

        runner.runForCurrentWeek(repository, householdId, currentWeek, today = wednesday)

        assertEquals(1, remote.claimCount)
        assertTrue(repository.currentGroceryItems().isEmpty(), "claim=false means no carry")
        assertEquals(
            currentWeek,
            NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId),
            "M4: local marker must be set when the claim returns false",
        )
    }

    @Test
    fun runForCurrentWeek_twoRunnersSharingClaim_exactlyOneCarries() = runTest {
        val claims = mutableMapOf<String, MutableSet<String>>()
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)
        val rowsByWeek = mapOf(
            previousWeek to listOf(
                NutritionWeekDataRow(
                    householdId = "household-two",
                    weekKey = previousWeek,
                    dataKind = "grocery",
                    payload = NutritionStorageCodec.encodeGrocery(
                        listOf(GroceryItem(id = "p1", label = "Milk"), GroceryItem(id = "p2", label = "Eggs")),
                    ),
                    updatedAt = "2026-06-14T12:00:00Z",
                ),
            ),
        )

        // Device A — claims and carries exactly once.
        val settingsA = MapSettings()
        val repositoryA = offlineRepository(settingsA, "household-two", currentWeek)
        val remoteA = RecordingRemote(rowsByWeek, claims)
        val runnerA = NutritionWeekMaintenanceRunner(
            settings = settingsA,
            maintenanceStore = NutritionWeekMaintenanceStore(settingsA),
            remoteApi = remoteA,
            newItemId = { "a" },
        )
        runnerA.runForCurrentWeek(repositoryA, "household-two", currentWeek, today = wednesday)

        // Device B — shares the claim registry; must NOT carry again.
        val settingsB = MapSettings()
        val repositoryB = offlineRepository(settingsB, "household-two", currentWeek)
        val remoteB = RecordingRemote(rowsByWeek, claims)
        val runnerB = NutritionWeekMaintenanceRunner(
            settings = settingsB,
            maintenanceStore = NutritionWeekMaintenanceStore(settingsB),
            remoteApi = remoteB,
            newItemId = { "b" },
        )
        runnerB.runForCurrentWeek(repositoryB, "household-two", currentWeek, today = wednesday)

        // Exactly one device carried, with no duplicate labels.
        val aLabels = repositoryA.currentGroceryItems().map { it.label }
        assertEquals(setOf("Milk", "Eggs"), aLabels.toSet())
        assertEquals(aLabels.size, aLabels.toSet().size, "no duplicate labels after one carry")
        assertTrue(repositoryB.currentGroceryItems().isEmpty(), "second device must not carry")
        assertEquals(currentWeek, NutritionWeekMaintenanceStore(settingsA).lastMaintainedWeekKey("household-two"))
        assertEquals(currentWeek, NutritionWeekMaintenanceStore(settingsB).lastMaintainedWeekKey("household-two"))
        assertEquals(1, remoteA.claimCount)
        assertEquals(1, remoteB.claimCount)
    }

    @Test
    fun runForCurrentWeek_viewerClaimDenied_noCarryNoCrashNoMarker() = runTest {
        val settings = MapSettings()
        val householdId = "household-viewer"
        val currentWeek = "2026-06-15"
        val previousWeek = "2026-06-08"
        val wednesday = LocalDate(2026, 6, 17)
        NutritionLocalStore(settings, householdId, previousWeek).saveGroceryItems(
            listOf(GroceryItem(id = "p1", label = "Milk")),
        )
        val repository = offlineRepository(settings, householdId, currentWeek)

        // A viewer's insert is RLS-denied — surfaces as an exception, not as false.
        val viewerRemote = object : NutritionRemoteDataSource {

            override suspend fun pushIfVersion(
                householdId: String,
                weekKey: String,
                dataKind: String,
                payload: String,
                expectedVersion: Long?,
            ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
            var claimAttempts = 0
            override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> = emptyList()
            override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean {
                claimAttempts++
                throw IllegalStateException("rls_denied_42501")
            }
            override suspend fun upsert(householdId: String, weekKey: String, dataKind: String, payload: String) = Unit
        }
        val runner = NutritionWeekMaintenanceRunner(
            settings = settings,
            maintenanceStore = NutritionWeekMaintenanceStore(settings),
            remoteApi = viewerRemote,
        )

        runner.runForCurrentWeek(repository, householdId, currentWeek, today = wednesday)

        // No crash, no carry, no local marker — retried on the next bind.
        assertTrue(repository.currentGroceryItems().isEmpty(), "viewer must not carry")
        assertEquals(null, NutritionWeekMaintenanceStore(settings).lastMaintainedWeekKey(householdId))
        assertEquals(1, viewerRemote.claimAttempts)
    }

    private fun offlineRepository(
        settings: MapSettings,
        householdId: String,
        weekKey: String,
    ): OfflineFirstNutritionRepository =
        OfflineFirstNutritionRepository(
            localStore = NutritionLocalStore(settings, householdId, weekKey),
            syncEngine = NutritionSyncEngine(null, NutritionSyncOutbox(settings), TestObservability.logger),
            householdId = householdId,
            weekKey = weekKey,
            remoteEnabled = false,
        )

    private class RecordingRemote(
        private val rowsByWeek: Map<String, List<NutritionWeekDataRow>>,
        /**
         * Simulates a server-held claim registry for testing.
         * When a claim is attempted, returns true if (household, week) is new, false already claimed.
         */
        private val claims: MutableMap<String, MutableSet<String>> = mutableMapOf(),
    ) : NutritionRemoteDataSource {

        override suspend fun pushIfVersion(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
            expectedVersion: Long?,
        ): PushResult = pushViaUpsert(householdId, weekKey, dataKind, payload, expectedVersion)
        var fetchCount = 0
        var claimCount = 0
        var failNextClaims = 0

        override suspend fun fetchWeek(householdId: String, weekKey: String): List<NutritionWeekDataRow> {
            fetchCount++
            return rowsByWeek[weekKey].orEmpty()
        }

        override suspend fun claimWeekCarry(householdId: String, weekKey: String): Boolean {
            if (failNextClaims > 0) {
                failNextClaims--
                throw IllegalStateException("claim_denied")
            }
            claimCount++
            val weekSet = claims.getOrPut(householdId) { mutableSetOf() }
            return weekSet.add(weekKey)
        }

        override suspend fun upsert(
            householdId: String,
            weekKey: String,
            dataKind: String,
            payload: String,
        ) = Unit
    }
}
