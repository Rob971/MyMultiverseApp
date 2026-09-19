package app.mymultiverse.ammo.data.seasonal

import app.mymultiverse.ammo.data.service.GeminiResponseParser
import app.mymultiverse.ammo.data.service.GeminiTextClient
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalCatalog
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalFood
import app.mymultiverse.ammo.domain.model.nutrition.SeasonalFoodCategory
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class SeasonalFoodDto(val id: String = "", val name: String = "", val category: String = "")

@Serializable
internal data class SeasonalFoodsDto(val foods: List<SeasonalFoodDto> = emptyList())

@Serializable
internal data class SeasonalCatalogDto(
    val countryCode: String,
    val month: Int,
    val languageCode: String,
    val foods: List<SeasonalFoodDto>,
)

private val catalogJson = Json { ignoreUnknownKeys = true; isLenient = true }

internal object SeasonalCatalogPrompt {
    private val MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    fun build(countryCode: String, month: Int, languageCode: String): String = buildString {
        append(GeminiResponseParser.languagePromptDirective(languageCode))
        append(" List 30 to 40 foods that are in season in the country with ISO code ")
        append(countryCode.uppercase())
        append(" in ")
        append(MONTHS[(month - 1).coerceIn(0, 11)])
        append(": fresh, healthy foods people commonly cook at home, including at least 12 vegetables. ")
        append("Return ONLY JSON of the form {\"foods\":[{\"id\":\"...\",\"name\":\"...\",\"category\":\"...\"}]}. ")
        append("id is a unique lowercase English slug (a-z, 0-9 and hyphens), name is the food in the user's language, ")
        append("category is one of: vegetable, fruit, legume, grain, fish, meat, dairy, other. No explanation.")
    }
}

internal object SeasonalCatalogParser {
    private val ID = Regex("^[a-z0-9]+(-[a-z0-9]+)*$")

    /**
     * Foods from a Gemini reply. Tolerates fences and prose around the JSON object; drops
     * items with a bad id, a blank name or an unknown category; keeps the first of duplicate ids.
     * Fails when the reply holds no valid food.
     */
    fun parse(text: String): Result<List<SeasonalFood>> = runCatching {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        require(start in 0 until end) { "seasonal_catalog_no_json" }
        val dto = catalogJson.decodeFromString<SeasonalFoodsDto>(text.substring(start, end + 1))
        val foods = dto.foods.mapNotNull { it.toDomainOrNull() }.distinctBy { it.id }
        require(foods.isNotEmpty()) { "seasonal_catalog_empty" }
        foods
    }

    fun SeasonalFoodDto.toDomainOrNull(): SeasonalFood? {
        val cleanId = id.trim().lowercase()
        val cleanName = name.trim()
        val kind = SeasonalFoodCategory.entries.firstOrNull { it.name.equals(category.trim(), ignoreCase = true) }
        if (!ID.matches(cleanId) || cleanName.isEmpty() || cleanName.length > 60 || kind == null) return null
        return SeasonalFood(id = cleanId, name = cleanName, category = kind)
    }
}

/** Keeps the latest catalog only: a new country, month or language replaces it. */
internal class SeasonalCatalogStore(private val settings: Settings) {
    fun get(): SeasonalCatalog? {
        val raw = settings.getStringOrNull(KEY) ?: return null
        val dto = runCatching { catalogJson.decodeFromString<SeasonalCatalogDto>(raw) }.getOrNull() ?: return null
        val foods = with(SeasonalCatalogParser) { dto.foods.mapNotNull { it.toDomainOrNull() } }
        return SeasonalCatalog(dto.countryCode, dto.month, dto.languageCode, foods).takeIf { foods.isNotEmpty() }
    }

    fun put(catalog: SeasonalCatalog) {
        val dto = SeasonalCatalogDto(
            countryCode = catalog.countryCode,
            month = catalog.month,
            languageCode = catalog.languageCode,
            foods = catalog.foods.map { SeasonalFoodDto(it.id, it.name, it.category.name.lowercase()) },
        )
        settings.putString(KEY, catalogJson.encodeToString(SeasonalCatalogDto.serializer(), dto))
    }

    private companion object {
        const val KEY = "seasonal_catalog"
    }
}

/**
 * The in-season catalog for a country, month and language: served from the cache, or asked of
 * Gemini once (through [GeminiTextClient], i.e. the ai-generate proxy) and cached. Nothing is
 * cached when the call or the parse fails.
 */
internal class SeasonalCatalogRepository(
    private val textClient: GeminiTextClient,
    private val store: SeasonalCatalogStore,
) {
    suspend fun catalogFor(countryCode: String, month: Int, languageCode: String): Result<SeasonalCatalog> {
        store.get()?.takeIf { it.isFor(countryCode, month, languageCode) }?.let { return Result.success(it) }
        val reply = textClient.complete(
            prompt = SeasonalCatalogPrompt.build(countryCode, month, languageCode),
            maxOutputTokens = CATALOG_MAX_TOKENS,
            temperature = CATALOG_TEMPERATURE,
        ).getOrElse { return Result.failure(it) }
        val foods = SeasonalCatalogParser.parse(reply).getOrElse { return Result.failure(it) }
        val catalog = SeasonalCatalog(countryCode.uppercase(), month, languageCode, foods)
        store.put(catalog)
        return Result.success(catalog)
    }

    private companion object {
        const val CATALOG_MAX_TOKENS = 2_048
        const val CATALOG_TEMPERATURE = 0.3
    }
}
