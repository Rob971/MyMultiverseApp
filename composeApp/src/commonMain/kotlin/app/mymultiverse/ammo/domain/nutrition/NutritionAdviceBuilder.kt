package app.mymultiverse.ammo.domain.nutrition

object NutritionAdviceBuilder {
    fun buildAdvice(question: String): String {
        val normalized = question.lowercase()
        return when {
            normalized.contains("protein") || normalized.contains("muscle") ->
                "Aim for protein at lunch and dinner: legumes, eggs, fish, yogurt, or lean meat. " +
                    "Pair each meal with vegetables and whole grains so energy stays steady through the day."

            normalized.contains("vegetarian") || normalized.contains("vegan") ->
                "Build plates around beans, lentils, tofu, nuts, and whole grains. " +
                    "Add iron-rich foods with vitamin C (tomatoes, citrus) and consider B12 if the household is fully plant-based."

            normalized.contains("child") || normalized.contains("kid") || normalized.contains("family") ->
                "Keep family meals simple: one familiar base, one new vegetable, and predictable portions. " +
                    "Involve children in choosing one dinner per week to improve buy-in."

            normalized.contains("budget") || normalized.contains("cheap") || normalized.contains("save") ->
                "Plan two batch-cook meals per week and reuse leftovers for lunch. " +
                    "Base the grocery list on seasonal produce and pantry staples before adding extras."

            normalized.contains("allerg") ->
                "Treat allergens as hard constraints: list them at the top of your meal plan and grocery list. " +
                    "When unsure about packaged foods, check labels every time—recipes can change."

            normalized.contains("weight") || normalized.contains("calorie") ->
                "Focus on balanced plates (half vegetables, quarter protein, quarter starch) and consistent meal timing. " +
                    "Small sustainable changes beat strict short-term diets for household health."

            else ->
                "For your household, anchor the week with two easy dinners, one batch lunch, and a grocery list tied to those meals. " +
                    "Keep questions specific (age, allergies, time, budget) for sharper guidance. " +
                    "You asked: \"$question\""
        }
    }
}
