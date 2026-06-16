package app.mymultiverse.kmp.domain.sharing

import app.mymultiverse.kmp.domain.model.sharing.NutritionSharingFeature
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionSharingPresentationTest {

    @Test
    fun orDefaultNutritionFeatures_returnsAllWhenEmpty() {
        val resolved = emptySet<NutritionSharingFeature>().orDefaultNutritionFeatures()

        assertEquals(DefaultNutritionSharingFeatures, resolved)
    }

    @Test
    fun orDefaultNutritionFeatures_preservesExplicitSubset() {
        val subset = setOf(NutritionSharingFeature.Grocery)

        assertEquals(subset, subset.orDefaultNutritionFeatures())
    }
}
