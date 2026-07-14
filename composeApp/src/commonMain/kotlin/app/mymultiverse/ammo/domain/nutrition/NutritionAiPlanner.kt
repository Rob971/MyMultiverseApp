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
        // Resolve to English canonical for ingredient matching so that localized dish names
        // (e.g. "Pollo saltato in padella in 20 min con riso") correctly trigger the same
        // ingredient rules as their English originals. User-typed names not in the catalog
        // fall back to the original text.
        val englishDish = NutritionFoodSuggestionLocalization.mealDishToEnglish(trimmed)
        val fromMeal = ingredientsForMeal(englishDish)
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
        val profile = RegionalMealProfile.from(languageCode, criteria) ?: MealProfile.from(criteria)
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
            is MealPlanGenerationScope.SingleMeal -> {
                currentPlan.days.toMutableList().also { days ->
                    val index = scope.dayIndex.coerceIn(0, WeeklyMealPlan.DAYS_IN_WEEK - 1)
                    val generated = generatedDays[index]
                    days[index] = when (scope.slot) {
                        MealSlot.Lunch -> days[index].copy(lunch = generated.lunch)
                        MealSlot.Dinner -> days[index].copy(dinner = generated.dinner)
                    }
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
            is MealPlanGenerationScope.SingleMeal -> {
                val dayName = dayNameFor(scope.dayIndex)
                "Updated ${scope.slot.name.lowercase()} on $dayName with a ${profile.label} meal based on: \"$criteria\"."
            }
        }

        return MealPlanGeneration(days = mergedDays, summary = summary)
    }

    private fun buildDayMeals(profile: MealProfile, dayIndex: Int, languageCode: String = "en"): DayMeals {
        val rotation = dayIndex % profile.lunches.size
        // Regional profiles already carry dishes in the user's language; the mealDishFor lookup
        // returns them unchanged (fallback) while generic (English) profiles get translated.
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

    /**
     * Returns culturally-appropriate meal dishes for the user's region when a regional catalog
     * exists (IT / FR / ES / DE). Returns null for EN, AR, NAP, and other locales so the caller
     * falls back to the translated generic [MealProfile].
     *
     * Each regional profile still respects the user's criteria intent (protein, vegetarian, etc.)
     * but offers dishes characteristic of that cuisine rather than translated international ones.
     */
    private object RegionalMealProfile {
        fun from(languageCode: String, criteria: String): MealProfile? {
            val keywords = criteria.lowercase()
            return when (NutritionFoodSuggestionLocalization.normalizeLanguageCode(languageCode)) {
                "it" -> italianProfile(keywords)
                "fr" -> frenchProfile(keywords)
                "es" -> spanishProfile(keywords)
                "de" -> germanProfile(keywords)
                "ar" -> arabicProfile(keywords)
                "nap" -> napoliProfile(keywords)
                else -> null
            }
        }

        private fun italianProfile(keywords: String): MealProfile = when {
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Vegetarian) ->
                MealProfile(
                    label = "vegetariano",
                    lunches = listOf("Pasta e fagioli", "Zuppa di lenticchie rosse", "Insalata di ceci con pomodorini", "Pasta al pesto di basilico", "Bruschetta con pomodorini e mozzarella", "Risotto alle verdure primaverili", "Panino con melanzane grigliate"),
                    dinners = listOf("Parmigiana di melanzane", "Pasta al pesto e zucchine", "Risotto ai funghi porcini", "Minestrone toscano", "Gnocchi al pomodoro e basilico", "Pasta con ricotta e spinaci", "Lasagne di verdure al forno"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Protein) ->
                MealProfile(
                    label = "proteico",
                    lunches = listOf("Petto di pollo alla griglia con ceci", "Bresaola con rucola e grana padano", "Tonno alla griglia con verdure miste", "Frittata di uova con spinaci", "Insalata di pollo con quinoa e peperoni", "Salmone con fagiolini e limone", "Ricotta con frutta secca e mandorle"),
                    dinners = listOf("Tagliata di manzo con rucola e parmigiano", "Pollo alla cacciatora con olive e capperi", "Salmone al forno con broccoli", "Gamberi in padella con aglio e olio", "Cotoletta di vitello con insalata verde", "Polpo e patate con prezzemolo", "Pollo alla diavola con patate dolci"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Budget) ->
                MealProfile(
                    label = "economico",
                    lunches = listOf("Pasta e ceci al pomodoro", "Zuppa di fagioli con pane", "Pasta con patate e rosmarino", "Minestra di orzo e verdure", "Frittata di patate e cipolla", "Pane con olio e pomodori", "Polenta con formaggio e funghi"),
                    dinners = listOf("Salsiccia con fagioli al sugo", "Pasta al ragù semplice", "Pappa al pomodoro con pane", "Lenticchie con salsiccia e sedano", "Frittata mista di verdure", "Pasta e verdure in padella", "Zuppa di pane e fagioli"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Allergy) ->
                MealProfile(
                    label = "senza allergeni",
                    lunches = listOf("Riso con pollo e zucchine", "Pollo al limone con patate arrosto", "Carpaccio di manzo con rucola e limone", "Salmone con riso e fagiolini", "Insalata di tonno con fagioli e pomodori", "Pollo arrosto con carote e sedano", "Vitello con insalata verde"),
                    dinners = listOf("Branzino al cartoccio con patate", "Pollo alla griglia con verdure al vapore", "Gamberi al forno con zucchine", "Manzo stufato con patate e carote", "Merluzzo al forno con patate dolci", "Tacchino arrosto con fagiolini", "Pollo in umido con olive e capperi"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Family) ->
                MealProfile(
                    label = "per la famiglia",
                    lunches = listOf("Pasta al ragù classico", "Pizza margherita fatta in casa", "Lasagna al forno bolognese", "Pasta alla carbonara semplice", "Gnocchi con burro e salvia", "Pasta con pollo e piselli", "Pasta al sugo di pomodoro e basilico"),
                    dinners = listOf("Pollo arrosto domenicale", "Cotolette di pollo con patate al forno", "Polpette di carne al sugo con pasta", "Spaghetti alla bolognese", "Scaloppine al sugo con contorno", "Pasta con salsiccia e piselli", "Risotto alla milanese"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Quick) ->
                MealProfile(
                    label = "veloce",
                    lunches = listOf("Pasta aglio olio e peperoncino", "Frittata di verdure con pane tostato", "Tramezzino di tonno e pomodori", "Insalata caprese con pane croccante", "Pasta al tonno con olive", "Uova strapazzate con asparagi", "Riso con tonno e piselli"),
                    dinners = listOf("Pasta al pomodoro veloce", "Pollo saltato in padella con peperoni", "Frittata di prosciutto e formaggio", "Pasta con salmone e zucchine", "Riso fritto con uova e piselli", "Petto di pollo con patate in 20 minuti", "Spaghetti con aglio olio e tonno"),
                )
            else ->
                MealProfile(
                    label = "equilibrato",
                    lunches = listOf("Pasta al pomodoro e basilico", "Insalata di rucola con pollo e parmigiano", "Zuppa di verdure con farro", "Tramezzino di tonno con pomodori", "Pollo con patate al forno", "Salmone con insalata di stagione", "Riso integrale con legumi e cipolla"),
                    dinners = listOf("Pollo alla cacciatora con olive", "Branzino al forno con patate", "Pasta alla norma", "Melanzane alla parmigiana", "Salmone con spinaci saltati", "Pasta e fagioli toscana", "Scaloppine al limone con insalata"),
                )
        }

        private fun frenchProfile(keywords: String): MealProfile = when {
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Vegetarian) ->
                MealProfile(
                    label = "végétarien",
                    lunches = listOf("Soupe au pistou provençale", "Quiche aux épinards et feta", "Salade de lentilles avec vinaigrette", "Tartine végétarienne aux légumes grillés", "Soupe de légumes du marché", "Galettes de sarrasin avec fromage", "Ratatouille froide en salade"),
                    dinners = listOf("Gratin de courgettes au fromage", "Ratatouille provençale avec riz", "Poêlée de légumes méditerranéens", "Soupe de potiron avec crème fraîche", "Tarte aux légumes du soleil", "Pasta aux légumes rôtis et chèvre", "Quiche aux légumes de saison"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Protein) ->
                MealProfile(
                    label = "protéiné",
                    lunches = listOf("Salade de poulet grillé avec haricots verts", "Omelette au jambon et fromage", "Thon mi-cuit avec salade mêlée", "Pavé de saumon avec purée de pois", "Assiette de charcuterie avec pain", "Oeufs en cocotte avec épinards", "Blanc de poulet avec quinoa"),
                    dinners = listOf("Magret de canard aux figues", "Bavette à l'échalote avec légumes", "Saumon grillé avec haricots verts", "Poulet au citron avec légumes vapeur", "Crevettes sautées à l'ail avec riz", "Filet de boeuf avec pommes de terre", "Lieu noir rôti avec purée"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Budget) ->
                MealProfile(
                    label = "économique",
                    lunches = listOf("Soupe de légumes maison avec pain", "Pasta aux lentilles et tomates", "Omelette aux pommes de terre", "Tartine aux haricots blancs et herbes", "Riz aux légumes sautés", "Soupe de poireaux avec pommes de terre", "Pain doré salé avec salade"),
                    dinners = listOf("Hachis Parmentier maison", "Gratin de pâtes au fromage", "Soupe de légumineuses avec pain complet", "Poulet en cocotte avec légumes", "Riz au lait de coco avec légumes", "Pasta au fromage et jambon", "Poêlée de pommes de terre avec lardons"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Allergy) ->
                MealProfile(
                    label = "sans allergènes",
                    lunches = listOf("Poulet grillé avec riz et concombres", "Saumon avec pommes de terre vapeur", "Salade de roquette avec poulet et citron", "Thon naturel avec riz et courgettes", "Filet de dinde avec carottes et riz", "Oeufs durs avec haricots verts", "Boeuf haché avec riz et légumes"),
                    dinners = listOf("Cabillaud au four avec pommes de terre", "Poulet rôti avec légumes vapeur", "Saumon avec riz et haricots verts", "Crevettes grillées avec riz basmati", "Gigot d'agneau avec haricots verts", "Escalope de dinde avec ratatouille", "Omelette nature avec salade verte"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Family) ->
                MealProfile(
                    label = "en famille",
                    lunches = listOf("Croque-monsieur avec salade verte", "Pasta à la bolognaise maison", "Quiche au fromage et jambon", "Hachis Parmentier individuel", "Sandwich poulet-crudités", "Pasta aux saucisses et tomates", "Riz pilaf avec poulet et petits pois"),
                    dinners = listOf("Poulet rôti du dimanche avec frites", "Gratin de macaronis au jambon", "Spaghettis bolognaise maison", "Côtelettes de porc avec purée", "Pizza maison tomate-mozzarella", "Crêpes garnies au jambon et fromage", "Boulettes de viande sauce tomate"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Quick) ->
                MealProfile(
                    label = "rapide",
                    lunches = listOf("Omelette aux herbes avec toast", "Salade rapide au thon et olives", "Tartine avocat-tomates-oeuf", "Sandwich fromage-tomates-basilic", "Riz sauté aux oeufs et légumes", "Pasta rapide à la tomate et basilic", "Soupe rapide de légumes avec pain"),
                    dinners = listOf("Poêlée de poulet aux champignons", "Pasta carbonara rapide", "Saumon à la poêle avec brocoli", "Omelette jambon-fromage", "Crevettes sautées avec riz express", "Escalope de dinde avec salade", "Soupe de légumes express avec pain"),
                )
            else ->
                MealProfile(
                    label = "équilibré",
                    lunches = listOf("Quiche aux légumes avec salade verte", "Salade niçoise avec olives", "Sandwich jambon-beurre avec crudités", "Poulet rôti avec légumes de saison", "Saumon fumé avec pommes de terre", "Omelette aux herbes et salade", "Soupe à l'oignon avec pain gratiné"),
                    dinners = listOf("Poulet aux herbes de Provence", "Cabillaud à la moutarde avec légumes", "Gratin dauphinois avec salade", "Boeuf bourguignon avec pommes de terre", "Saumon en papillote avec brocoli", "Ratatouille avec riz", "Côtelettes d'agneau avec haricots verts"),
                )
        }

        private fun spanishProfile(keywords: String): MealProfile = when {
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Vegetarian) ->
                MealProfile(
                    label = "vegetariano",
                    lunches = listOf("Gazpacho fresco con pan", "Ensalada de garbanzos con espinacas", "Tortilla de patatas con pimientos", "Pisto manchego con huevos", "Pasta con verduras del mediterráneo", "Crema de calabaza con pan integral", "Ensalada de lentejas con vinagreta"),
                    dinners = listOf("Pisto manchego con arroz", "Berenjenas rellenas de verduras", "Pasta con salsa de tomate y albahaca", "Crema de puerros con pan tostado", "Revuelto de espárragos trigueros", "Arroz con verduras de temporada", "Tortilla de verduras con ensalada"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Protein) ->
                MealProfile(
                    label = "proteico",
                    lunches = listOf("Ensalada de pollo a la plancha con legumbres", "Jamón serrano con queso y fruta", "Atún a la plancha con ensalada verde", "Revuelto de gambas con ajetes", "Pechuga de pollo con quinoa y pimientos", "Salmón ahumado con aguacate", "Huevos revueltos con jamón y espárragos"),
                    dinners = listOf("Solomillo de cerdo con judías verdes", "Pollo en salsa verde con patatas", "Salmón al horno con brócoli", "Gambas al pil pil con arroz", "Ternera con champiñones y arroz", "Pechuga de pollo con patatas asadas", "Bacalao con tomates y olivas"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Budget) ->
                MealProfile(
                    label = "económico",
                    lunches = listOf("Lentejas con chorizo y verduras", "Arroz con pollo al estilo casero", "Pasta con garbanzos y espinacas", "Potaje de alubias con verduras", "Sopa de ajo con huevo escalfado", "Pan con tomate y aceite de oliva", "Croquetas de pollo con ensalada"),
                    dinners = listOf("Cocido madrileño simplificado", "Arroz con alubias y verduras", "Pasta a la boloñesa económica", "Sopa de fideos con pollo", "Patatas a la riojana", "Judías blancas con chorizo y panceta", "Arroz caldoso con verduras"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Allergy) ->
                MealProfile(
                    label = "sin alérgenos",
                    lunches = listOf("Pollo a la plancha con arroz y pepino", "Atún natural con patatas cocidas", "Salmón con arroz y judías verdes", "Ensalada de pollo sin gluten", "Ternera asada con zanahorias", "Merluza con patatas al vapor", "Pechuga de pavo con arroz integral"),
                    dinners = listOf("Merluza al horno con patatas", "Pollo al limón con verduras al vapor", "Gambas a la plancha con arroz", "Salmón con brócoli al vapor", "Ternera estofada con patatas", "Pechuga de pollo al ajillo", "Tortilla de patatas sin cebolla"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Family) ->
                MealProfile(
                    label = "familiar",
                    lunches = listOf("Pasta a la boloñesa para toda la familia", "Tortilla española con ensalada", "Pizza casera de jamón y queso", "Pollo asado con patatas fritas", "Macarrones con tomate y queso", "Arroz con pollo y verduras", "Croquetas de jamón y pollo"),
                    dinners = listOf("Pollo al horno con patatas y romero", "Spaghetti boloñesa caseros", "Milanesas de pollo con puré de patatas", "Hamburguesas caseras con patatas", "Macarrones con queso y bacon", "Pollo guisado con arroz y verduras", "Fideos con gambas y mejillones"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Quick) ->
                MealProfile(
                    label = "rápido",
                    lunches = listOf("Tostada de tomate con aceite de oliva", "Tortilla de patatas rápida con ensalada", "Bocadillo de atún con tomates", "Ensalada de pasta con atún y maíz", "Revuelto de huevos con verduras", "Arroz tres delicias rápido", "Sopa de tomate en 20 minutos"),
                    dinners = listOf("Pasta aglio e olio con gambas", "Pollo salteado con pimientos y cebolla", "Tortilla francesa con jamón", "Arroz salteado con huevo y verduras", "Salmón a la plancha con ensalada rápida", "Huevos al plato con chorizo y tomate", "Fideos fritos con gambas y huevo"),
                )
            else ->
                MealProfile(
                    label = "equilibrado",
                    lunches = listOf("Paella de verduras con arroz", "Ensalada mixta con atún y huevos", "Gazpacho andaluz con pan integral", "Tortilla española con ensalada", "Pollo con arroz y verduras", "Lentejas estofadas con chorizo", "Sandwich de jamón y tomate"),
                    dinners = listOf("Pollo al ajillo con patatas", "Merluza al horno con patatas", "Pasta con salsa de tomate casera", "Bacalao con tomates y olivas", "Salmón a la plancha con ensalada", "Caldo de pollo con fideos", "Cerdo asado con verduras"),
                )
        }

        private fun germanProfile(keywords: String): MealProfile = when {
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Vegetarian) ->
                MealProfile(
                    label = "vegetarisch",
                    lunches = listOf("Linseneintopf mit Vollkornbrot", "Käsespätzle mit Salat", "Gemüsesuppe mit Vollkornbrot", "Erbsensuppe mit Brot", "Kartoffelsuppe mit Kräutern", "Gemischter Salat mit Käse und Ei", "Pasta mit Pesto und Tomaten"),
                    dinners = listOf("Gemüsecurry mit Reis", "Kartoffelgratin mit Salat", "Pasta mit Tomatensoße und Käse", "Vegetarische Gemüsepfanne mit Reis", "Kartoffelauflauf mit Gemüse", "Spinatquiche mit Salat", "Ratatouille mit Brot"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Protein) ->
                MealProfile(
                    label = "proteinreich",
                    lunches = listOf("Hähnchenbrust-Salat mit Bohnen", "Thunfischsalat mit Vollkornbrot", "Rührei mit Spinat und Vollkorntoast", "Lachsfilet mit Gurke auf Brot", "Putenbrust mit Quinoa und Gemüse", "Rindfleischsalat mit Essig-Öl", "Griechischer Joghurt mit Nüssen und Beeren"),
                    dinners = listOf("Gegrilltes Hähnchen mit grünen Bohnen", "Lachs im Ofen mit Brokkoli", "Rinderfilet mit Karotten und Kartoffeln", "Putenbrust mit Zucchini und Reis", "Schweinefilet mit Bohnen und Senf", "Forelle mit Kartoffeln und Dill", "Hähnchenschenkel mit Süßkartoffeln"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Budget) ->
                MealProfile(
                    label = "günstig",
                    lunches = listOf("Erbsensuppe mit Brot", "Pasta mit Speck und Tomaten", "Linsensuppe mit Würstchen", "Kartoffelgulasch mit Brot", "Graupensuppe mit Gemüse", "Ei-Pfannkuchen mit Apfelmus", "Tomatensuppe mit Vollkornbrot"),
                    dinners = listOf("Würstchen mit Kartoffelsalat", "Pasta mit Soße und Käse", "Linseneintopf mit Kartoffeln", "Nudeln mit Tomatensoße und Hackfleisch", "Eintopf mit Bohnen und Speck", "Kartoffelauflauf mit Käse", "Kartoffelspalten mit Spiegelei"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Allergy) ->
                MealProfile(
                    label = "allergikerfreundlich",
                    lunches = listOf("Hähnchen mit Reis und Gurken", "Putenbrust mit Kartoffeln und Erbsen", "Lachsfilet mit Reis und Bohnen", "Thunfisch mit Reis und Tomaten", "Putenschnitzel mit Kartoffeln", "Rührei mit Kartoffeln", "Rindfleischsalat ohne Nüsse"),
                    dinners = listOf("Kabeljau im Ofen mit Kartoffeln", "Hähnchen mit Gemüse ohne Nüsse", "Lachs mit Reis und grünen Bohnen", "Putengulasch mit Kartoffeln", "Rindfleischeintopf mit Karotten", "Forelle mit Kartoffeln und Kräutern", "Hähnchenschenkel mit Erbsen und Reis"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Family) ->
                MealProfile(
                    label = "familienfreundlich",
                    lunches = listOf("Spaghetti Bolognese für die Familie", "Schnitzel mit Pommes und Salat", "Kartoffelsuppe mit Würstchen", "Pasta mit Käsesoße und Schinken", "Pfannkuchen mit Wurst und Salat", "Nudeln mit Tomatensoße und Käse", "Hähnchen-Nuggets mit Kartoffeln"),
                    dinners = listOf("Hähnchen im Ofen mit Kartoffeln", "Hackbraten mit Kartoffelpüree", "Spaghetti mit Hackfleischsoße", "Wiener Schnitzel mit Pommes", "Fleischbällchen in Tomatensoße", "Pasta mit Schinken und Erbsen", "Fischstäbchen mit Kartoffeln und Erbsen"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Quick) ->
                MealProfile(
                    label = "schnell",
                    lunches = listOf("Rührei mit Gemüse und Toast", "Thunfischsalat mit Brot", "Nudeln mit Pesto und Tomaten", "Käsebrot mit Gurke und Paprika", "Joghurt-Bowl mit Obst und Granola", "Tomatensuppe schnell mit Brot", "Brot mit Käse und Tomate"),
                    dinners = listOf("Pasta mit Tomatensoße in 20 Minuten", "Hähnchen-Pfanne mit Paprika", "Rührei mit Schinken und Toast", "Nudeln mit Ei und Parmesan", "Gebratener Reis mit Ei und Erbsen", "Lachs mit Brokkoli in 20 Minuten", "Thunfisch-Toast mit Mozzarella"),
                )
            else ->
                MealProfile(
                    label = "ausgewogen",
                    lunches = listOf("Kartoffelsalat mit Hähnchen und Kräutern", "Linsensuppe mit Vollkornbrot", "Hähnchensalat mit gemischtem Gemüse", "Vollkornnudeln mit Tomatensoße", "Gemüsesuppe mit Einlage", "Lachs mit Kartoffeln und Dill", "Brot mit Aufschnitt und Salat"),
                    dinners = listOf("Hähnchen mit Kartoffeln und Gemüse", "Fischfilet mit Kartoffelpüree und Erbsen", "Schweinebraten mit Rotkohl und Kartoffeln", "Pasta mit hausgemachter Tomatensoße", "Lachs mit grünem Spargel", "Gemüseeintopf mit Einlage", "Schnitzel mit Salat und Kartoffeln"),
                )
        }

        private fun arabicProfile(keywords: String): MealProfile = when {
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Vegetarian) ->
                MealProfile(
                    label = "نباتي",
                    lunches = listOf("حمص بالطحينة مع خبز عربي وزيت الزيتون", "فلافل مع السلطة الخضراء والطحينة", "مجدرة بالعدس والأرز والبصل المقلي", "فتوش بالخضار والسماق", "شوربة الطماطم مع الخبز المحمص", "سلطة الفاصوليا البيضاء بالزيتون", "بيض مع الخضار والجبنة"),
                    dinners = listOf("ورق عنب محشو بالأرز والخضار", "مقلوبة الخضار مع الأرز", "يخنة الحمص مع الطماطم والبهارات", "هريسة الخضار مع الأرز", "ملوخية الخضار مع الأرز", "شوربة العدس الحمراء مع الخبز", "كوسا محشوة بالأرز والخضار"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Protein) ->
                MealProfile(
                    label = "غني بالبروتين",
                    lunches = listOf("صدر الدجاج المشوي مع الحمص والخيار", "سلطة التونة مع البيض والطماطم", "لحم مشوي مع الفاصوليا والسلطة", "دجاج بالبهارات مع الكينوا والخضار", "سمك السلمون مع الأرز والليمون", "شيش كباب مع السلطة الخضراء", "مرق الدجاج بالحمص والبيض"),
                    dinners = listOf("كباب اللحم مع البرغل والبقدونس", "دجاج بالزيتون والليمون والثوم", "سمك السلمون بالفرن مع الخضار", "جمبري مشوي بالثوم مع الأرز", "لحم بالخضار المشكلة والبهارات", "دجاج مشوي مع الفاصوليا الخضراء", "بيفتيك اللحم مع السلطة الخضراء"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Budget) ->
                MealProfile(
                    label = "اقتصادي",
                    lunches = listOf("فول مدمس مع خبز وزيت الزيتون", "عدس مع الأرز والبصل المقلي", "شوربة الخضار البسيطة", "مجدرة بالعدس والأرز", "خبز مع زيت الزيتون والزعتر", "بيض مع الطماطم والبصل", "أرز بالشعرية بالمرق"),
                    dinners = listOf("يخنة الفاصوليا مع الخبز", "شوربة العدس مع عصير الليمون", "مكرونة بالصلصة البسيطة", "أرز بالكمون والبصل المقلي", "بطاطس مشوية مع البيض", "فتة الحمص مع الخبز المحمص", "عجة الخضار مع الخبز"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Allergy) ->
                MealProfile(
                    label = "خالي من المسببات الحساسية",
                    lunches = listOf("دجاج مشوي مع الأرز والخيار", "سلطة التونة مع الطماطم بدون مكسرات", "سمك مشوي مع البطاطس", "أرز بالدجاج والجزر", "صدر الدجاج مع البازلاء والجزر", "لحم مشوي مع البطاطس والجزر", "سمك السلمون مع الأرز والفاصوليا الخضراء"),
                    dinners = listOf("سمك بالفرن مع البطاطس", "دجاج مشوي مع الخضار بدون مكسرات", "لحم بالخضار البسيطة", "جمبري مع الأرز والليمون", "دجاج بالطماطم والبطاطس", "سمك بالكزبرة والليمون مع الأرز", "دجاج مسلوق مع البطاطس والجزر"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Family) ->
                MealProfile(
                    label = "للعائلة",
                    lunches = listOf("كبسة الدجاج مع الأرز للعائلة", "منسف الدجاج بالزبادي", "شاورما الدجاج مع الخبز والبطاطس", "دجاج مشوي مع البطاطس للعائلة", "مكرونة باللحم المفروم والصلصة", "ملوخية الدجاج مع الأرز", "رز اللحم للعائلة بالبهارات"),
                    dinners = listOf("شيش طاووق مع الأرز والسلطة", "دجاج محمر بالثوم مع البطاطس للعائلة", "كباب اللحم مع الأرز والسلطة", "برجر دجاج منزلي مع البطاطس", "مكرونة بالصلصة الحمراء واللحم", "دجاج بالأرز والزبادي", "سمك مشوي مع البطاطس والسلطة"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Quick) ->
                MealProfile(
                    label = "سريع",
                    lunches = listOf("فلافل مع الطحينة وخبز البيتا", "شاورما دجاج سريعة", "بيض مع الطماطم والبهارات", "حمص مع خبز عربي وزيت الزيتون", "تبولة سريعة بالخضار", "أرز بالشعرية في 20 دقيقة", "سلطة الفاصوليا البيضاء مع الزيتون"),
                    dinners = listOf("عجة بالخضار والجبنة في 20 دقيقة", "دجاج مشوي سريع مع الأرز", "مكرونة بصلصة الطماطم السريعة", "رز بالدجاج السريع", "بيض مع اللحم المفروم والطماطم", "جمبري مقلي بالثوم مع الأرز", "فتة بسيطة سريعة"),
                )
            else ->
                MealProfile(
                    label = "متوازن",
                    lunches = listOf("فتوش بالدجاج المشوي والخبز المحمص", "شوربة العدس الحمراء بالكمون", "ورق عنب محشو بالأرز والبندورة", "تبولة مع الجبنة البيضاء والخضار", "سلطة الحمص بالطحينة وخبز عربي", "مجدرة بالأرز والبصل المقلي", "سمك مشوي مع البرغل والبقدونس"),
                    dinners = listOf("كبسة الدجاج بالتوابل والأرز", "شيش طاووق مع الأرز والسلطة الخضراء", "دجاج محمر بالثوم مع البطاطس", "مقلوبة الدجاج بالخضار", "سمك بالطماطم والكزبرة والكمون", "ملوخية مع الدجاج والأرز", "لحم بالخضار والبهارات"),
                )
        }

        private fun napoliProfile(keywords: String): MealProfile = when {
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Vegetarian) ->
                MealProfile(
                    label = "vegetariano",
                    lunches = listOf("Pasta e fasule napulitana", "Minestrone 'e verdure cu pane cafone", "Zuppa 'e lenticchie e cepolla", "Insalata 'e cece cu pummarulelle", "Pasta cu pesto 'e basilico", "Bruschetta napulitana cu pummarola", "Panino cu mulignane a' griglia"),
                    dinners = listOf("Parmigiana 'e mulignane classica", "Pasta cu pesto e zucchine", "Risotto a' funge porcine", "Minestrone cu legume e verdure", "Gnocchi a' pummarola e basilico", "Pasta 'e ricotta e spinace", "Lasagne 'e verdure a' furrno"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Protein) ->
                MealProfile(
                    label = "proteico",
                    lunches = listOf("Petto 'e pullastro a' griglia cu cece", "Insalata 'e tonno cu fasule e pummarola", "Frittata 'e uova e spinace cu pane", "Pullastro cu quinoa e pummarulelle", "Salmone cu fagiolini e limone", "Bresaola cu rucola e grana", "Ricotta fresca cu frutta secca e nuce"),
                    dinners = listOf("Tagliata 'e manzo cu rucola e parmigiano", "Pullastro a' cacciatora cu olive e capperi", "Salmone a' furrno cu broccole", "Gammare a' padella cu aglio e uoglio", "Cotoletta 'e vitello cu nzalandella", "Polipetti cu patate e prezzemolo", "Pullastro a' diavola cu patate dolce"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Budget) ->
                MealProfile(
                    label = "economico",
                    lunches = listOf("Pasta e cece a' pummarola", "Zuppa 'e fasule cu pane cafone", "Pasta cu patane e rosmarino", "Minestra 'e orzo e verdure", "Frittata 'e patane e cepolla", "Pane cu uoglio e pummarola", "Pasta 'e lenticchie cu 'o sedano"),
                    dinners = listOf("Salsiccia cu fasule a sugo", "Pasta a ragù semplice", "Pappa a pummarola cu pane", "Lenticchie cu salsiccia e sedano", "Frittata mmescata 'e verdure", "Pasta e verdure a' padella", "Zuppa 'e pane e fasule"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Allergy) ->
                MealProfile(
                    label = "senza allergeni",
                    lunches = listOf("Riso cu pullastro e zucchine", "Pullastro a limone cu patane a' furrno", "Carpaccio 'e manzo cu rucola e limone", "Salmone cu riso e fagiolini", "Insalata 'e tonno cu fasule e pummarola", "Pullastro arrustuto cu carote e sedano", "Vitello cu nzalandella verde"),
                    dinners = listOf("Branzino a' furrno cu patane", "Pullastro a' griglia cu verdure a vapore", "Gammare a' furrno cu zucchine", "Manzo stufato cu patane e carote", "Merluzzo a' furrno cu patate dolce", "Tacchino arrustuto cu fagiolini", "Pullastro 'nfucuato cu olive e capperi"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Family) ->
                MealProfile(
                    label = "pe' 'a famiglia",
                    lunches = listOf("Pasta a ragù classico napoletano", "Pizza margherita fatta 'n casa", "Lasagna a' furrno bolognese", "Pasta a' carbonara semprece", "Gnocchi cu burro e salvia", "Pasta cu pullastro e piselle", "Spaghetti a pummarola cu basilico"),
                    dinners = listOf("Pullastro arrustuto a domenica", "Cotolette 'e pullastro cu patane a' furrno", "Purpette 'e carne a sugo cu pasta", "Spaghetti a' bolognese", "Scaloppine a sugo cu contorno", "Pasta cu salsiccia e piselle", "Sartu 'e riso cu pullastro"),
                )
            NutritionFoodSuggestionLocalization.hasIntent(keywords, NutritionFoodSuggestionLocalization.Intent.Quick) ->
                MealProfile(
                    label = "ambressa",
                    lunches = listOf("Pasta aglio uoglio e peperuncino", "Frittata 'e ova cu pane tostato", "Panino cu Pruvulone e pummarola", "Pasta cu 'o tonno e olive nere", "Caprese cu pane cafone", "Zuppa 'e lenticchie veloce", "Ova strapazzate cu pummarulelle"),
                    dinners = listOf("Pasta cu 'e vongole 'n venti minuti", "Pullastro a' padella cu vino bianco", "Frittata 'e prosciutto e provola", "Spaghetti cu 'o tonno e basilico", "Riso fritto cu ova e piselle", "Cozze a' tarantina cu pane", "Ova a occhio 'e bue cu pane tostato"),
                )
            else ->
                MealProfile(
                    label = "equilibrato",
                    lunches = listOf("Pasta e fasule napulitana cu pane", "Frittata 'e maccheroni cu provola", "Insalata caprese cu pane cafone", "Sartu 'e riso cu piselle e carne", "Pasta a pummarola ca' n'uocchio", "Zuppa 'e pesce cu pane", "Pasta cu 'o tonno e pummarulelle"),
                    dinners = listOf("Salsiccia e friarielli a' padella", "Baccalà a' piscaiola cu pummarola", "Genovese 'e manzo cu paccheri", "Pullastro a' furrno cu patane e rosmarino", "Ragù napoletano a' domenica", "Salmone a griglia cu limone e capperi", "Purpette 'e manzo a sugo"),
                )
        }
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
