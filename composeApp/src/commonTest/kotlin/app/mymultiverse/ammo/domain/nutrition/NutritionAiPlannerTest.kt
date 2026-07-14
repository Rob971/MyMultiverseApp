package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionAiPlannerTest {

    @Test
    fun generateGroceryList_returnsItemsForProteinKeywords() {
        val items = NutritionAiPlanner.generateGroceryList("high protein family")

        assertTrue(items.any { it.contains("Chicken", ignoreCase = true) || it.contains("yogurt", ignoreCase = true) })
        assertTrue(items.size >= 6)
    }

    @Test
    fun generateGroceryList_localizesFoodSuggestionsForSelectedLanguage() {
        val items = NutritionAiPlanner.generateGroceryList(
            criteria = "Almuerzos proteicos para la familia",
            languageCode = "es",
        )

        assertTrue("Pechuga de pollo" in items)
        assertTrue("Yogur griego" in items)
        assertTrue("Chicken breast" !in items)
    }

    @Test
    fun generateGroceryForMeal_localizesMealIngredientSuggestions() {
        val items = NutritionAiPlanner.generateGroceryForMeal(
            mealDescription = "Pasta con pollo",
            languageCode = "fr",
        )

        assertTrue("Blanc de poulet" in items)
        assertTrue("Huile d'olive" in items)
        assertTrue("Chicken breast" !in items)
    }

    @Test
    fun generateMealPlan_matchesLocalizedQuickPickCriteria() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Comidas altas en proteína",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
        )

        assertTrue(result.summary.contains("high-protein"))
    }

    @Test
    fun generateMealPlan_italianUser_returnsRegionalItalianDishes() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Pasto veloce 20 min",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
            languageCode = "it",
        )

        // Regional Italian quick dishes — not translated English dishes
        assertTrue(result.days.none { it.lunch == "20-min veggie omelette with toast" })
        assertTrue(result.days.none { it.dinner == "20-min chicken stir-fry with rice" })
        // First Italian quick lunch is Pasta aglio olio e peperoncino
        assertTrue(result.days[0].lunch == "Pasta aglio olio e peperoncino")
        assertTrue(result.days.all { it.lunch.isNotBlank() && it.dinner.isNotBlank() })
    }

    @Test
    fun generateMealPlan_spanishUser_returnsRegionalSpanishDishes() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Comidas altas en proteína",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
            languageCode = "es",
        )

        // Regional Spanish protein dishes — not translated English dishes
        assertTrue(result.days.none { it.lunch == "Grilled chicken salad with quinoa" })
        assertTrue(result.days[0].lunch == "Ensalada de pollo a la plancha con legumbres")
        assertTrue(result.days.all { it.lunch.isNotBlank() && it.dinner.isNotBlank() })
    }

    @Test
    fun generateMealPlan_arabicUser_returnsRegionalArabicDishes() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "وجبات سريعة",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
            languageCode = "ar",
        )

        // Regional Arabic quick dishes — not translated English dishes
        assertTrue(result.days.none { it.lunch == "20-min veggie omelette with toast" })
        assertTrue(result.days[0].lunch == "فلافل مع الطحينة وخبز البيتا")
        assertTrue(result.days.all { it.lunch.isNotBlank() && it.dinner.isNotBlank() })
    }

    @Test
    fun generateMealPlan_napoliUser_returnsRegionalNapoliDishes() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Pasto ambressa veloce",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
            languageCode = "nap",
        )

        // Regional Neapolitan quick dishes — not translated English dishes
        assertTrue(result.days.none { it.lunch == "20-min veggie omelette with toast" })
        assertTrue(result.days[0].lunch == "Pasta aglio uoglio e peperuncino")
        assertTrue(result.days.all { it.lunch.isNotBlank() && it.dinner.isNotBlank() })
    }

    @Test
    fun generateMealPlan_defaultLanguage_keepsDishNamesInEnglish() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Quick 20-min lunch",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
        )

        assertTrue(result.days[0].lunch == "20-min veggie omelette with toast")
        assertTrue(result.days[0].dinner == "20-min chicken stir-fry with rice")
    }

    @Test
    fun generateMealPlan_singleMealScope_updatesOnlyTargetSlot() {
        val existing = WeeklyMealPlan(weekKey = "2026-W24").copy(
            days = List(7) { DayMeals(lunch = "Existing lunch", dinner = "Existing dinner") },
        )
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "protein",
            scope = MealPlanGenerationScope.SingleMeal(dayIndex = 2, slot = MealSlot.Lunch),
            currentPlan = existing,
        )

        // Only lunch on day 2 is replaced
        assertTrue(result.days[2].lunch.isNotBlank())
        assertTrue(result.days[2].lunch != "Existing lunch")
        assertEquals("Existing dinner", result.days[2].dinner)
        // All other days are untouched
        for (i in 0 until 7) {
            if (i != 2) {
                assertEquals("Existing lunch", result.days[i].lunch)
                assertEquals("Existing dinner", result.days[i].dinner)
            }
        }
    }

    @Test
    fun generateMealPlan_singleMealScope_dinner_updatesOnlyDinnerSlot() {
        val existing = WeeklyMealPlan(weekKey = "2026-W24").copy(
            days = List(7) { DayMeals(lunch = "My lunch", dinner = "My dinner") },
        )
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Quick 20-min",
            scope = MealPlanGenerationScope.SingleMeal(dayIndex = 0, slot = MealSlot.Dinner),
            currentPlan = existing,
        )

        assertEquals("My lunch", result.days[0].lunch)
        assertTrue(result.days[0].dinner.isNotBlank())
        assertTrue(result.days[0].dinner != "My dinner")
    }

    @Test
    fun generateMealPlan_quickCriteria_usesQuickProfile() {
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "Quick 20-min lunch",
            scope = MealPlanGenerationScope.SingleDay(0),
            currentPlan = WeeklyMealPlan(weekKey = "2026-W24"),
        )

        assertTrue(result.days[0].lunch.contains("20-min", ignoreCase = true))
    }

    @Test
    fun generateMealPlan_fullWeek_replacesAllDays() {
        val plan = WeeklyMealPlan(weekKey = "2026-05-18")
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "vegetarian",
            scope = MealPlanGenerationScope.FullWeek,
            currentPlan = plan,
        )

        assertEquals(7, result.days.size)
        assertTrue(result.days.all { it.lunch.isNotBlank() && it.dinner.isNotBlank() })
    }

    @Test
    fun generateMealPlan_singleDay_updatesOnlyTargetDay() {
        val plan = WeeklyMealPlan(weekKey = "2026-05-18")
        val result = NutritionAiPlanner.generateMealPlan(
            criteria = "protein",
            scope = MealPlanGenerationScope.SingleDay(dayIndex = 2),
            currentPlan = plan,
        )

        assertTrue(result.days[2].lunch.isNotBlank())
        assertTrue(result.days[2].dinner.isNotBlank())
        assertEquals("", plan.days[0].lunch)
    }

    @Test
    fun generateGroceryForMeal_returnsItemsForMealDescription() {
        val items = NutritionAiPlanner.generateGroceryForMeal("Grilled chicken salad")

        assertTrue(items.isNotEmpty())
        assertTrue(items.size <= 12)
    }

    @Test
    fun generateGroceryForMeal_blankMeal_returnsEmpty() {
        assertEquals(emptyList(), NutritionAiPlanner.generateGroceryForMeal("   "))
    }

    @Test
    fun generateGroceryForMeal_italianDishName_resolvesCorrectIngredients() {
        // "Pollo saltato in padella in 20 min con riso" is the Italian translation of
        // "20-min chicken stir-fry with rice". Ingredients should include chicken-related items.
        val items = NutritionAiPlanner.generateGroceryForMeal(
            mealDescription = "Pollo saltato in padella in 20 min con riso",
            languageCode = "it",
        )

        assertTrue(items.any { it.contains("pollo", ignoreCase = true) || it.contains("Petto", ignoreCase = true) })
        assertTrue(items.none { it == "Chicken breast" })
    }

    @Test
    fun generateGroceryForMeal_italianSoupDish_resolvesSoupIngredients() {
        // "Zuppa di lenticchie con pane integrale" → "Lentil soup with whole-grain bread"
        // Should produce soup/vegetable ingredients, not just salt and pepper.
        val items = NutritionAiPlanner.generateGroceryForMeal(
            mealDescription = "Zuppa di lenticchie con pane integrale",
            languageCode = "it",
        )

        assertTrue(items.isNotEmpty())
        assertTrue(items.any { it.contains("brodo", ignoreCase = true) || it.contains("Carote", ignoreCase = true) || it.contains("Patate", ignoreCase = true) })
    }

    @Test
    fun generateGroceryForMeal_spanishFrittata_resolvesEggIngredients() {
        // "Tortilla de verduras de 20 min con tostada" → "20-min veggie omelette with toast"
        val items = NutritionAiPlanner.generateGroceryForMeal(
            mealDescription = "Tortilla de verduras de 20 min con tostada",
            languageCode = "es",
        )

        assertTrue(items.any { it.contains("Huevo", ignoreCase = true) })
        assertTrue(items.none { it == "Eggs" })
    }

    @Test
    fun generateGroceryForMeal_userTypedDish_fallsBackToDirectMatching() {
        // A user-typed dish name not in the catalog should still resolve ingredients
        // via multilingual containsIngredient checks.
        val items = NutritionAiPlanner.generateGroceryForMeal(
            mealDescription = "Pasta con pollo e verdure",
            languageCode = "it",
        )

        assertTrue(items.isNotEmpty())
    }
}
