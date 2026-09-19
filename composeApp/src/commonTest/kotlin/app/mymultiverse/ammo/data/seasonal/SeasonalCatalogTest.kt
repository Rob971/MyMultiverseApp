package app.mymultiverse.ammo.data.seasonal

import app.mymultiverse.ammo.data.service.GeminiTextClient
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalFood
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalFoodCategory
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeasonalCatalogTest {

    private val reply = """{"foods":[
        {"id":"zucchini","name":"Zucchine","category":"vegetable"},
        {"id":"figs","name":"Fichi","category":"FRUIT"},
        {"id":"sea-bass","name":"Branzino","category":"fish"}]}"""

    @Test
    fun parse_validReply_returnsFoodsWithCategories() {
        val foods = SeasonalCatalogParser.parse(reply).getOrThrow()

        assertEquals(
            listOf(
                SeasonalFood("zucchini", "Zucchine", SeasonalFoodCategory.VEGETABLE),
                SeasonalFood("figs", "Fichi", SeasonalFoodCategory.FRUIT),
                SeasonalFood("sea-bass", "Branzino", SeasonalFoodCategory.FISH),
            ),
            foods,
        )
    }

    @Test
    fun parse_toleratesFencesAndProseAroundTheJson() {
        val foods = SeasonalCatalogParser.parse("Here you go:\n```json\n$reply\n```\nEnjoy!").getOrThrow()

        assertEquals(3, foods.size)
    }

    @Test
    fun parse_junkOrWrongShape_fails() {
        assertTrue(SeasonalCatalogParser.parse("Sorry, I can't help with that.").isFailure)
        assertTrue(SeasonalCatalogParser.parse("""{"items":[{"id":"figs","name":"Fichi","category":"fruit"}]}""").isFailure)
        assertTrue(SeasonalCatalogParser.parse("""{"foods":"figs"}""").isFailure)
    }

    @Test
    fun parse_dropsInvalidItemsAndDuplicateIds() {
        val foods = SeasonalCatalogParser.parse(
            """{"foods":[
                {"id":"Figs ","name":"Fichi","category":"fruit"},
                {"id":"figs","name":"Fichi again","category":"fruit"},
                {"id":"bad id!","name":"X","category":"fruit"},
                {"id":"kale","name":"  ","category":"vegetable"},
                {"id":"moon-rock","name":"Moon rock","category":"mineral"},
                {"id":"leeks","name":"Porri","category":"vegetable"}]}""",
        ).getOrThrow()

        assertEquals(listOf("figs", "leeks"), foods.map { it.id })
        assertEquals("Fichi", foods.first().name)
    }

    @Test
    fun catalogFor_asksOncePerCountryMonthAndLanguage_andAgainWhenTheMonthChanges() = runTest {
        val client = FakeTextClient(listOf(Result.success(reply), Result.success(reply)))
        val repository = SeasonalCatalogRepository(client, SeasonalCatalogStore(MapSettings()))

        val first = repository.catalogFor("it", 9, "it").getOrThrow()
        repository.catalogFor("IT", 9, "it").getOrThrow()
        assertEquals(1, client.prompts.size, "same country, month and language must come from the cache")
        assertEquals("IT", first.countryCode)

        repository.catalogFor("IT", 10, "it").getOrThrow()
        assertEquals(2, client.prompts.size, "a new month must ask again")
        assertTrue("October" in client.prompts.last() && "IT" in client.prompts.last())
    }

    @Test
    fun catalogFor_cachesNothingWhenTheCallOrTheParseFails() = runTest {
        val store = SeasonalCatalogStore(MapSettings())
        val client = FakeTextClient(listOf(Result.failure(RuntimeException("offline")), Result.success("no json here")))
        val repository = SeasonalCatalogRepository(client, store)

        assertTrue(repository.catalogFor("IT", 9, "it").isFailure)
        assertTrue(repository.catalogFor("IT", 9, "it").isFailure)
        assertNull(store.get())
    }

    @Test
    fun store_roundTripsTheCatalog() {
        val settings = MapSettings()
        val catalog = app.mymultiverse.ammo.domain.model.nutrition.SeasonalCatalog(
            "IT", 9, "it", listOf(SeasonalFood("figs", "Fichi", SeasonalFoodCategory.FRUIT)),
        )

        SeasonalCatalogStore(settings).put(catalog)

        assertEquals(catalog, SeasonalCatalogStore(settings).get())
    }

    private class FakeTextClient(replies: List<Result<String>>) : GeminiTextClient {
        private val queue = replies.toMutableList()
        val prompts = mutableListOf<String>()

        override suspend fun complete(prompt: String, maxOutputTokens: Int, temperature: Double): Result<String> {
            prompts += prompt
            return queue.removeAt(0)
        }
    }
}
