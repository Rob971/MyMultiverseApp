package app.mymultiverse.ammo.data.seasonal

import app.mymultiverse.ammo.data.service.GeminiTextClient
import app.mymultiverse.ammo.domain.location.DeviceRegion
import app.mymultiverse.ammo.domain.location.FakeDeviceRegionService
import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.FavoriteDish
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalCatalog
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalFood
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalFoodCategory
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import app.mymultiverse.ammo.domain.repository.FavoriteDishesRepository
import app.mymultiverse.ammo.domain.service.GeminiApiException
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SeasonalWeekTest {

    private val vegetables = (1..8).map { "veg-$it" }
    private val catalogReply = buildString {
        append("{\"foods\":[")
        append(vegetables.joinToString(",") { "{\"id\":\"$it\",\"name\":\"Veg ${it.last()}\",\"category\":\"vegetable\"}" })
        append(",{\"id\":\"chickpeas\",\"name\":\"Ceci\",\"category\":\"legume\"}]}")
    }
    private val catalog = SeasonalCatalog(
        "IT", 9, "it",
        vegetables.map { SeasonalFood(it, it, SeasonalFoodCategory.VEGETABLE) } +
            SeasonalFood("chickpeas", "Ceci", SeasonalFoodCategory.LEGUME),
    )

    /** A week JSON: [dishes] are 14 names (lunch, dinner per day); each meal uses one food id. */
    private fun weekReply(dishes: List<String>, foods: List<String>): String = buildString {
        append("{\"days\":[")
        append((0 until dishes.size / 2).joinToString(",") { day ->
            val lunch = day * 2
            "{\"lunch\":{\"dish\":\"${dishes[lunch]}\",\"foods\":[\"${foods[lunch % foods.size]}\"]}," +
                "\"dinner\":{\"dish\":\"${dishes[lunch + 1]}\",\"foods\":[\"${foods[(lunch + 1) % foods.size]}\"]}}"
        })
        append("],\"summary\":\"A seasonal week\"}")
    }

    private val goodDishes = (1..14).map { "Dish $it" }
    private val goodWeek = weekReply(goodDishes, vegetables)

    // ── validator ──

    @Test
    fun validator_acceptsAWeekThatFollowsEveryRule() {
        val draft = SeasonalWeekParser.parse(goodWeek).getOrThrow()

        assertEquals(emptyList(), SeasonalWeekValidator.problems(draft, catalog, favoriteKeys = emptySet()))
    }

    @Test
    fun validator_namesEachBrokenRule() {
        val sixVegetables = SeasonalWeekParser.parse(weekReply(goodDishes, vegetables.take(6))).getOrThrow()
        val repeated = SeasonalWeekParser.parse(weekReply(goodDishes.dropLast(1) + "dish 1", vegetables)).getOrThrow()
        val sixDays = SeasonalWeekParser.parse(weekReply(goodDishes.take(12), vegetables)).getOrThrow()
        val favorites = setOf("dish 1", "dish 2", "dish 3")

        assertTrue(SeasonalWeekValidator.problems(sixVegetables, catalog, emptySet()).single().contains("only 6 different vegetables"))
        assertTrue(SeasonalWeekValidator.problems(repeated, catalog, emptySet()).single().contains("repeated: dish 1"))
        assertTrue(SeasonalWeekValidator.problems(sixDays, catalog, emptySet()).any { it.contains("exactly 7 days") })
        assertTrue(SeasonalWeekValidator.problems(SeasonalWeekParser.parse(goodWeek).getOrThrow(), catalog, favorites)
            .single().contains("3 favorite dishes"))
        assertEquals(false, SeasonalWeekValidator.isComplete(sixDays))
    }

    @Test
    fun parser_toleratesFencesAndRejectsJunk() {
        assertEquals(7, SeasonalWeekParser.parse("```json\n$goodWeek\n```").getOrThrow().days.size)
        assertTrue(SeasonalWeekParser.parse("I cannot do that").isFailure)
    }

    // ── suggester ──

    @Test
    fun suggestWeek_returnsTheWeek_andTellsTheModelTheFavoritesAndPlannedMeals() = runTest {
        val client = FakeTextClient(listOf(Result.success(catalogReply), Result.success(goodWeek)))
        val suggester = suggester(client, favorites = listOf("Pasta al pesto"))
        val plan = WeeklyMealPlan(weekKey = "2026-W38").let { it.copy(days = it.days.toMutableList().apply { this[0] = DayMeals(lunch = "Risotto") }) }

        val week = suggester.suggestWeek(plan).getOrThrow()

        assertEquals(7, week.days.size)
        assertEquals(DayMeals(lunch = "Dish 1", dinner = "Dish 2"), week.days.first())
        val weekPrompt = client.prompts.last()
        assertTrue("Pasta al pesto" in weekPrompt && "at most 2" in weekPrompt)
        assertTrue("do not repeat: Risotto" in weekPrompt)
        assertTrue("veg-1: Veg 1 (vegetable)" in weekPrompt)
    }

    @Test
    fun suggestWeek_retriesOnceWithTheBrokenRules() = runTest {
        val tooFewVegetables = weekReply(goodDishes, vegetables.take(3))
        val client = FakeTextClient(listOf(Result.success(catalogReply), Result.success(tooFewVegetables), Result.success(goodWeek)))

        val week = suggester(client).suggestWeek(WeeklyMealPlan(weekKey = "2026-W38")).getOrThrow()

        assertEquals(3, client.prompts.size, "catalog, first week, retry")
        assertTrue("only 3 different vegetables" in client.prompts.last())
        assertEquals("Dish 1", week.days.first().lunch)
    }

    @Test
    fun suggestWeek_showsACompleteWeekThatStillBreaksARuleAfterTheRetry() = runTest {
        val tooFewVegetables = weekReply(goodDishes, vegetables.take(3))
        val client = FakeTextClient(listOf(Result.success(catalogReply), Result.success(tooFewVegetables), Result.success(tooFewVegetables)))

        val week = suggester(client).suggestWeek(WeeklyMealPlan(weekKey = "2026-W38")).getOrThrow()

        assertEquals(7, week.days.size)
    }

    @Test
    fun suggestWeek_failsWhenNoReplyIsACompleteWeek() = runTest {
        val client = FakeTextClient(listOf(Result.success(catalogReply), Result.success("junk"), Result.success(weekReply(goodDishes.take(12), vegetables))))

        val error = suggester(client).suggestWeek(WeeklyMealPlan(weekKey = "2026-W38")).exceptionOrNull()

        assertIs<GeminiApiException>(error)
        assertEquals(GeminiApiException.Reason.PARSE_ERROR, error.reason)
    }

    @Test
    fun suggestWeek_failsWithoutACatalog_andAsksNothingElse() = runTest {
        val client = FakeTextClient(listOf(Result.failure(RuntimeException("offline"))))

        assertTrue(suggester(client).suggestWeek(WeeklyMealPlan(weekKey = "2026-W38")).isFailure)
        assertEquals(1, client.prompts.size)
    }

    @Test
    fun catalogCountry_fallsBackToTheLanguagesCountry_whenTheDeviceRegionIsUnknown() = runTest {
        val client = FakeTextClient(listOf(Result.success(catalogReply)))

        suggester(client, region = null, language = "fr").prefetchCatalog()

        assertTrue("ISO code FR" in client.prompts.single())
    }

    // ── S5: prefetch refreshes only when the month (or country, language) changes ──

    @Test
    fun prefetchCatalog_asksOnceAMonth() = runTest {
        var month = 9
        val client = FakeTextClient(listOf(Result.success(catalogReply), Result.success(catalogReply)))
        val suggester = suggester(client, month = { month })

        suggester.prefetchCatalog()
        suggester.prefetchCatalog()
        assertEquals(1, client.prompts.size, "same month: cached")

        month = 10
        suggester.prefetchCatalog()
        assertEquals(2, client.prompts.size, "new month: refreshed")
        assertTrue("October" in client.prompts.last())
    }

    private fun suggester(
        client: FakeTextClient,
        favorites: List<String> = emptyList(),
        region: DeviceRegion? = DeviceRegion("IT"),
        language: String = "it",
        month: () -> Int = { 9 },
    ) = GeminiSeasonalWeekSuggester(
        textClient = client,
        catalogs = SeasonalCatalogRepository(client, SeasonalCatalogStore(MapSettings())),
        regionService = FakeDeviceRegionService(regionToReturn = region),
        favorites = FakeFavorites(favorites.map { FavoriteDish(label = it, normalisedLabel = it.trim().lowercase()) }),
        currentLanguageCode = { language },
        currentMonth = month,
    )

    private class FakeTextClient(replies: List<Result<String>>) : GeminiTextClient {
        private val queue = replies.toMutableList()
        val prompts = mutableListOf<String>()

        override suspend fun complete(prompt: String, maxOutputTokens: Int, temperature: Double): Result<String> {
            prompts += prompt
            return queue.removeAt(0)
        }
    }

    private class FakeFavorites(initial: List<FavoriteDish>) : FavoriteDishesRepository {
        override val favorites: StateFlow<List<FavoriteDish>> = MutableStateFlow(initial)
        override val remoteAvailable: StateFlow<Boolean> = MutableStateFlow(true)
        override suspend fun refresh() = Result.success(Unit)
        override suspend fun addFavorite(label: String) = Result.success(Unit)
        override suspend fun removeFavorite(normalisedLabel: String) = Result.success(Unit)
        override suspend fun replaceFavorite(removeNormalisedLabel: String, newLabel: String) = Result.success(Unit)
    }
}
