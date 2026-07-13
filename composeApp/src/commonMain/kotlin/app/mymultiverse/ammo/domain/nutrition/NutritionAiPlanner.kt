package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.model.nutrition.DayMeals
import app.mymultiverse.ammo.domain.model.nutrition.WeeklyMealPlan

object NutritionAiPlanner {

    data class MealPlanGeneration(
        val days: List<DayMeals>,
        val summary: String,
    )

    fun generateGroceryList(criteria: String, languageCode: String = "en"): List<String> {
        if (criteria.isBlank()) return emptyList()
        val keywords = criteria.lowercase()
        val items = linkedSetOf<String>()

        fun add(vararg labels: String) {
            labels.forEach { items += it }
        }

        add("Seasonal vegetables", "Fresh fruit", "Whole-grain bread", "Olive oil", "Eggs", "Milk")

        when {
            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Protein,
            ) ->
                add("Chicken breast", "Greek yogurt", "Lentils", "Canned tuna", "Cottage cheese", "Quinoa")

            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Vegetarian,
            ) ->
                add("Firm tofu", "Chickpeas", "Spinach", "Brown rice", "Almond butter", "Fortified plant milk")

            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Budget,
            ) ->
                add("Dried beans", "Frozen vegetables", "Rice", "Pasta", "Canned tomatoes", "Oats")

            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Allergy,
            ) || keywords.contains("nut-free") ->
                add("Sunflower seed butter", "Rice cakes", "Gluten-free oats", "Coconut yogurt")

            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Family,
            ) ->
                add("Whole-wheat pasta", "Cheese sticks", "Carrots", "Apples", "Turkey slices", "Hummus")
        }

        if (NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Vegetable,
            )
        ) {
            add("Mixed salad greens", "Broccoli", "Cherry tomatoes", "Cucumber", "Avocado")
        }
        if (NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Fish,
            )
        ) {
            add("Salmon fillets", "Sardines", "Lemons")
        }
        if (NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Breakfast,
            )
        ) {
            add("Oats", "Bananas", "Berries", "Honey")
        }

        return items
            .take(14)
            .map { label -> NutritionFoodSuggestionLocalization.labelFor(label, languageCode) }
    }

    fun generateGroceryForMeal(mealDescription: String, languageCode: String = "en"): List<String> {
        val trimmed = mealDescription.trim()
        if (trimmed.isEmpty()) return emptyList()
        val fromMeal = ingredientsForMeal(trimmed)
        val fromKeywords = generateGroceryList(trimmed, languageCode = "en")
        return (fromMeal + fromKeywords)
            .distinct()
            .take(12)
            .map { label -> NutritionFoodSuggestionLocalization.labelFor(label, languageCode) }
    }

    fun generateMealPlan(
        criteria: String,
        scope: MealPlanGenerationScope,
        currentPlan: WeeklyMealPlan,
        languageCode: String = "en",
    ): MealPlanGeneration {
        val profile = MealProfile.from(criteria)
        val generatedDays = List(WeeklyMealPlan.DAYS_IN_WEEK) { index ->
            buildDayMeals(profile, index, languageCode)
        }

        val mergedDays = when (scope) {
            is MealPlanGenerationScope.FullWeek -> generatedDays
            is MealPlanGenerationScope.SingleDay -> {
                currentPlan.days.toMutableList().also { days ->
                    val index = scope.dayIndex.coerceIn(0, WeeklyMealPlan.DAYS_IN_WEEK - 1)
                    days[index] = generatedDays[index]
                }
            }
        }

        val summary = when (scope) {
            is MealPlanGenerationScope.FullWeek ->
                "Created a ${profile.label} plan for the full week based on: \"$criteria\"."
            is MealPlanGenerationScope.SingleDay -> {
                val dayName = dayNameFor(scope.dayIndex)
                "Updated $dayName with ${profile.label} meals based on: \"$criteria\"."
            }
        }

        return MealPlanGeneration(days = mergedDays, summary = summary)
    }

    private fun buildDayMeals(profile: MealProfile, dayIndex: Int, languageCode: String = "en"): DayMeals {
        val rotation = dayIndex % profile.lunches.size
        return DayMeals(
            lunch = NutritionFoodSuggestionLocalization.mealDishFor(profile.lunches[rotation], languageCode),
            dinner = NutritionFoodSuggestionLocalization.mealDishFor(profile.dinners[rotation], languageCode),
        )
    }

    private fun ingredientsForMeal(meal: String): List<String> {
        val normalized = meal.lowercase()
        val items = linkedSetOf<String>()

        fun add(vararg labels: String) {
            labels.forEach { items += it }
        }

        when {
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, "chicken") ->
                add("Chicken breast", "Olive oil", "Garlic", "Onion", "Chicken stock")
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, "salmon") ->
                add("Salmon fillets", "Lemon", "Dill", "Asparagus", "Butter")
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, "beef") || normalized.contains("chili") ->
                add("Ground beef", "Kidney beans", "Tomatoes", "Onion", "Cumin")
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, "pasta") || normalized.contains("lasagna") ->
                add("Pasta", "Parmesan", "Tomatoes", "Basil", "Garlic")
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, "rice") ||
                normalized.contains("stir-fry") ||
                normalized.contains("bowl") ->
                add("Rice", "Soy sauce", "Ginger", "Bell peppers", "Scallions")
            NutritionFoodSuggestionLocalization.hasIntent(
                normalized,
                NutritionFoodSuggestionLocalization.Intent.Vegetable,
            ) ->
                add("Mixed greens", "Cherry tomatoes", "Cucumber", "Olive oil", "Balsamic vinegar")
            normalized.contains("soup") || normalized.contains("stew") ->
                add("Vegetable broth", "Carrots", "Celery", "Potatoes", "Herbs")
            normalized.contains("taco") || normalized.contains("burrito") ->
                add("Tortillas", "Black beans", "Salsa", "Lettuce", "Cheese")
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, "eggs") ||
                normalized.contains("frittata") ||
                normalized.contains("omelette") ->
                add("Eggs", "Spinach", "Cheese", "Milk", "Butter")
            NutritionFoodSuggestionLocalization.containsIngredient(normalized, "tofu") ||
                NutritionFoodSuggestionLocalization.hasIntent(
                    normalized,
                    NutritionFoodSuggestionLocalization.Intent.Vegetable,
                ) ->
                add("Firm tofu", "Broccoli", "Soy sauce", "Sesame oil", "Brown rice")
        }

        add("Salt", "Black pepper")
        return items.toList()
    }

    private fun dayNameFor(dayIndex: Int): String = when (dayIndex) {
        0 -> "Monday"
        1 -> "Tuesday"
        2 -> "Wednesday"
        3 -> "Thursday"
        4 -> "Friday"
        5 -> "Saturday"
        else -> "Sunday"
    }

    private data class MealProfile(
        val label: String,
        val lunches: List<String>,
        val dinners: List<String>,
    ) {
        companion object {
            fun from(criteria: String): MealProfile {
                val keywords = criteria.lowercase()
                return when {
                    NutritionFoodSuggestionLocalization.hasIntent(
                        keywords,
                        NutritionFoodSuggestionLocalization.Intent.Vegetarian,
                    ) ->
                        MealProfile(
                            label = "plant-forward",
                            lunches = listOf(
                                "Lentil soup with whole-grain bread",
                                "Chickpea salad bowl with tahini",
                                "Veggie wrap with hummus",
                                "Tofu stir-fry with brown rice",
                                "Minestrone with beans and greens",
                                "Quinoa tabbouleh with falafel",
                                "Roasted vegetable pasta",
                            ),
                            dinners = listOf(
                                "Black bean tacos with salsa",
                                "Eggplant parmesan with salad",
                                "Thai coconut vegetable curry",
                                "Stuffed bell peppers with rice",
                                "Mushroom risotto with peas",
                                "Baked sweet potato with tahini bowl",
                                "Vegetable lasagna",
                            ),
                        )

                    NutritionFoodSuggestionLocalization.hasIntent(
                        keywords,
                        NutritionFoodSuggestionLocalization.Intent.Protein,
                    ) ->
                        MealProfile(
                            label = "high-protein",
                            lunches = listOf(
                                "Grilled chicken salad with quinoa",
                                "Turkey and avocado whole-grain wrap",
                                "Tuna niçoise salad",
                                "Greek yogurt bowl with nuts and berries",
                                "Beef and vegetable stir-fry",
                                "Salmon poke bowl",
                                "Egg and spinach frittata with fruit",
                            ),
                            dinners = listOf(
                                "Baked salmon with roasted broccoli",
                                "Lean beef chili with beans",
                                "Chicken thighs with sweet potato",
                                "Shrimp and vegetable skewers",
                                "Turkey meatballs with zucchini noodles",
                                "Pork tenderloin with green beans",
                                "White fish with herbed lentils",
                            ),
                        )

                    NutritionFoodSuggestionLocalization.hasIntent(
                        keywords,
                        NutritionFoodSuggestionLocalization.Intent.Budget,
                    ) ->
                        MealProfile(
                            label = "budget-friendly",
                            lunches = listOf(
                                "Rice and beans with sautéed peppers",
                                "Pasta with tomato and chickpeas",
                                "Egg fried rice with frozen vegetables",
                                "Lentil stew with bread",
                                "Tuna pasta salad",
                                "Potato and vegetable soup",
                                "Peanut butter sandwich with carrot sticks",
                            ),
                            dinners = listOf(
                                "Chili con carne with rice",
                                "Baked pasta with vegetables",
                                "Chicken and cabbage skillet",
                                "Bean burritos with salsa",
                                "Vegetable omelette with toast",
                                "Sardine tomato pasta",
                                "Slow-cooker lentil curry",
                            ),
                        )

                    NutritionFoodSuggestionLocalization.hasIntent(
                        keywords,
                        NutritionFoodSuggestionLocalization.Intent.Allergy,
                    ) ->
                        MealProfile(
                            label = "allergy-aware",
                            lunches = listOf(
                                "Grilled chicken with rice and cucumbers",
                                "Turkey lettuce cups with fruit",
                                "Rice noodles with tamari vegetables",
                                "Sunflower butter banana sandwich (GF)",
                                "Baked cod with potatoes",
                                "Quinoa salad with olive oil dressing",
                                "Roasted chicken with carrots",
                            ),
                            dinners = listOf(
                                "Beef and vegetable stew (nut-free)",
                                "Salmon with rice and green beans",
                                "Chicken stir-fry with coconut aminos",
                                "Pork chops with mashed potatoes",
                                "Turkey patties with roasted squash",
                                "White fish with herb rice",
                                "Hearty vegetable soup with bread",
                            ),
                        )

                    NutritionFoodSuggestionLocalization.hasIntent(
                        keywords,
                        NutritionFoodSuggestionLocalization.Intent.Family,
                    ) ->
                        MealProfile(
                            label = "family-friendly",
                            lunches = listOf(
                                "Mini whole-wheat pizzas with salad",
                                "Chicken quesadilla with fruit",
                                "Pasta with mild tomato sauce",
                                "Turkey and cheese roll-ups",
                                "Homemade chicken soup with bread",
                                "Fish sticks with peas and carrots",
                                "Build-your-own taco bar",
                            ),
                            dinners = listOf(
                                "Spaghetti with turkey meatballs",
                                "Baked chicken strips with potatoes",
                                "Mild vegetable curry with rice",
                                "Homemade burgers with sweet potato fries",
                                "Baked mac and cheese with broccoli",
                                "Sheet-pan sausage and vegetables",
                                "Breakfast-for-dinner: eggs and pancakes",
                            ),
                        )

                    NutritionFoodSuggestionLocalization.hasIntent(
                        keywords,
                        NutritionFoodSuggestionLocalization.Intent.Quick,
                    ) ->
                        MealProfile(
                            label = "quick",
                            lunches = listOf(
                                "20-min veggie omelette with toast",
                                "Quick turkey and cheese wrap",
                                "Tuna salad sandwich with fruit",
                                "Microwave lentil soup with bread",
                                "Greek yogurt bowl with granola",
                                "Quesadilla with black beans",
                                "Caprese salad with whole-grain crackers",
                            ),
                            dinners = listOf(
                                "20-min chicken stir-fry with rice",
                                "Sheet-pan salmon and broccoli",
                                "Quick pasta with tomato and basil",
                                "Beef and vegetable skillet",
                                "Shrimp tacos with slaw",
                                "Egg fried rice with peas",
                                "Baked gnocchi with spinach",
                            ),
                        )

                    else ->
                        MealProfile(
                            label = "balanced",
                            lunches = listOf(
                                "Mediterranean grain bowl with chicken",
                                "Vegetable soup with whole-grain roll",
                                "Salmon salad with mixed greens",
                                "Brown rice burrito bowl",
                                "Greek salad with chickpeas and feta",
                                "Turkey sandwich with side salad",
                                "Stir-fried vegetables with tofu",
                            ),
                            dinners = listOf(
                                "Roasted chicken with vegetables",
                                "Baked fish with quinoa and greens",
                                "Vegetable and bean chili",
                                "Pork tenderloin with roasted carrots",
                                "Shrimp tacos with cabbage slaw",
                                "Mushroom and spinach pasta",
                                "Hearty lentil and vegetable stew",
                            ),
                        )
                }
            }
        }
    }
}
