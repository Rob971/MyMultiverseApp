package app.mymultiverse.ammo.data.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class GeminiResponseParserTest {

    // ── extractJsonArray ──────────────────────────────────────────────────────

    @Test
    fun extractJsonArray_plainArray_returnsList() {
        val result = GeminiResponseParser.extractJsonArray(
            """["Polpo","Patate","Prezzemolo","Aglio","Olio d'oliva","Sale"]""",
        )
        assertEquals(listOf("Polpo", "Patate", "Prezzemolo", "Aglio", "Olio d'oliva", "Sale"), result)
    }

    @Test
    fun extractJsonArray_markdownJsonFence_stripsAndParses() {
        val input = """
            ```json
            ["Octopus","Potatoes","Parsley","Garlic","Olive oil","Salt"]
            ```
        """.trimIndent()
        val result = GeminiResponseParser.extractJsonArray(input)
        assertEquals(listOf("Octopus", "Potatoes", "Parsley", "Garlic", "Olive oil", "Salt"), result)
    }

    @Test
    fun extractJsonArray_genericCodeFence_stripsAndParses() {
        val input = "```\n[\"Pollo\",\"Ail\"]\n```"
        val result = GeminiResponseParser.extractJsonArray(input)
        assertEquals(listOf("Pollo", "Ail"), result)
    }

    @Test
    fun extractJsonArray_noArray_throwsIllegalArgument() {
        assertFails {
            GeminiResponseParser.extractJsonArray("Here are some ingredients for the dish.")
        }
    }

    @Test
    fun extractJsonArray_filtersBlankEntries() {
        val result = GeminiResponseParser.extractJsonArray("""["Salt","","Pepper","  "]""")
        assertEquals(listOf("Salt", "Pepper"), result)
    }

    // ── parseIngredients ─────────────────────────────────────────────────────

    @Test
    fun parseIngredients_validGeminiResponse_returnsIngredients() {
        val responseBody = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      { "text": "[\"Polpo\",\"Patate\",\"Prezzemolo\",\"Aglio\",\"Sale\"]" }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val result = GeminiResponseParser.parseIngredients(responseBody)

        assertEquals(listOf("Polpo", "Patate", "Prezzemolo", "Aglio", "Sale"), result)
    }

    @Test
    fun parseIngredients_textWithMarkdownFence_returnsIngredients() {
        val responseBody = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [
                      { "text": "```json\n[\"Chicken breast\",\"Garlic\",\"Lemon\"]\n```" }
                    ]
                  }
                }
              ]
            }
        """.trimIndent()

        val result = GeminiResponseParser.parseIngredients(responseBody)

        assertEquals(listOf("Chicken breast", "Garlic", "Lemon"), result)
    }

    @Test
    fun parseIngredients_missingCandidates_throwsException() {
        val responseBody = """{"error": {"message": "API key not valid"}}"""

        assertFails {
            GeminiResponseParser.parseIngredients(responseBody)
        }
    }

    // ── languageNameFor ───────────────────────────────────────────────────────

    @Test
    fun languageNameFor_itCode_returnsItalian() {
        assertEquals("Italian", GeminiResponseParser.languageNameFor("it"))
    }

    @Test
    fun languageNameFor_frCode_returnsFrench() {
        assertEquals("French", GeminiResponseParser.languageNameFor("fr"))
    }

    @Test
    fun languageNameFor_esCode_returnsSpanish() {
        assertEquals("Spanish", GeminiResponseParser.languageNameFor("es"))
    }

    @Test
    fun languageNameFor_deCode_returnsGerman() {
        assertEquals("German", GeminiResponseParser.languageNameFor("de"))
    }

    @Test
    fun languageNameFor_arCode_returnsArabic() {
        assertEquals("Arabic", GeminiResponseParser.languageNameFor("ar"))
    }

    @Test
    fun languageNameFor_napCode_returnsNeapolitanItalian() {
        assertEquals("Neapolitan Italian", GeminiResponseParser.languageNameFor("nap"))
    }

    @Test
    fun languageNameFor_unknownCode_returnsEnglish() {
        assertEquals("English", GeminiResponseParser.languageNameFor("zh"))
        assertEquals("English", GeminiResponseParser.languageNameFor("en"))
    }

    @Test
    fun languageNameFor_caseInsensitive() {
        assertEquals("Italian", GeminiResponseParser.languageNameFor("IT"))
        assertEquals("French", GeminiResponseParser.languageNameFor("FR"))
    }
}
