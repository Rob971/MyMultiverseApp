package app.mymultiverse.ammo.domain.nutrition

import app.mymultiverse.ammo.domain.manager.SupportedAppLanguages

object NutritionFoodSuggestionLocalization {

    enum class Intent {
        Protein,
        Vegetarian,
        Budget,
        Allergy,
        Family,
        Vegetable,
        Fish,
        Breakfast,
        Quick,
    }

    private const val EN = "en"
    private const val ES = "es"
    private const val FR = "fr"
    private const val DE = "de"
    private const val IT = "it"
    private const val AR = "ar"
    private const val NAP = "nap"

    private val labels = mapOf(
        "Almond butter" to localized("Almond butter", "Crema de almendras", "Beurre d'amande", "Mandelmus", "Crema di mandorle", "زبدة اللوز", "Crema 'e mennule"),
        "Apples" to localized("Apples", "Manzanas", "Pommes", "Äpfel", "Mele", "تفاح", "Mele"),
        "Asparagus" to localized("Asparagus", "Espárragos", "Asperges", "Spargel", "Asparagi", "هليون", "Sparaci"),
        "Avocado" to localized("Avocado", "Aguacate", "Avocat", "Avocado", "Avocado", "أفوكادو", "Avocado"),
        "Balsamic vinegar" to localized("Balsamic vinegar", "Vinagre balsámico", "Vinaigre balsamique", "Balsamico-Essig", "Aceto balsamico", "خل بلسمي", "Acito balsamico"),
        "Bananas" to localized("Bananas", "Plátanos", "Bananes", "Bananen", "Banane", "موز", "Banane"),
        "Basil" to localized("Basil", "Albahaca", "Basilic", "Basilikum", "Basilico", "ريحان", "Vasinicola"),
        "Bell peppers" to localized("Bell peppers", "Pimientos", "Poivrons", "Paprika", "Peperoni", "فلفل رومي", "Pupacchielle"),
        "Beans" to localized("Beans", "Frijoles", "Haricots", "Bohnen", "Fagioli", "فاصوليا", "Fasule"),
        "Berries" to localized("Berries", "Frutos rojos", "Fruits rouges", "Beeren", "Frutti di bosco", "توت", "Frutte 'e vosco"),
        "Black beans" to localized("Black beans", "Frijoles negros", "Haricots noirs", "Schwarze Bohnen", "Fagioli neri", "فاصوليا سوداء", "Fasule nire"),
        "Black pepper" to localized("Black pepper", "Pimienta negra", "Poivre noir", "Schwarzer Pfeffer", "Pepe nero", "فلفل أسود", "Pepe niro"),
        "Broccoli" to localized("Broccoli", "Brócoli", "Brocoli", "Brokkoli", "Broccoli", "بروكلي", "Broccole"),
        "Brown rice" to localized("Brown rice", "Arroz integral", "Riz complet", "Naturreis", "Riso integrale", "أرز بني", "Riso integrale"),
        "Butter" to localized("Butter", "Mantequilla", "Beurre", "Butter", "Burro", "زبدة", "Butirro"),
        "Canned tomatoes" to localized("Canned tomatoes", "Tomates en conserva", "Tomates en conserve", "Dosentomaten", "Pomodori in scatola", "طماطم معلبة", "Pummarole 'n scatola"),
        "Canned tuna" to localized("Canned tuna", "Atún en lata", "Thon en conserve", "Thunfisch aus der Dose", "Tonno in scatola", "تونة معلبة", "Tonno 'n scatola"),
        "Carrots" to localized("Carrots", "Zanahorias", "Carottes", "Karotten", "Carote", "جزر", "Carote"),
        "Celery" to localized("Celery", "Apio", "Céleri", "Sellerie", "Sedano", "كرفس", "Accio"),
        "Cheese" to localized("Cheese", "Queso", "Fromage", "Käse", "Formaggio", "جبن", "Caso"),
        "Cheese sticks" to localized("Cheese sticks", "Palitos de queso", "Bâtonnets de fromage", "Käsesticks", "Bastoncini di formaggio", "أعواد جبن", "Bastuncielle 'e caso"),
        "Cherry tomatoes" to localized("Cherry tomatoes", "Tomates cherry", "Tomates cerises", "Kirschtomaten", "Pomodorini", "طماطم كرزية", "Pummarulelle"),
        "Chicken" to localized("Chicken", "Pollo", "Poulet", "Hähnchen", "Pollo", "دجاج", "Pullastro"),
        "Chicken breast" to localized("Chicken breast", "Pechuga de pollo", "Blanc de poulet", "Hähnchenbrust", "Petto di pollo", "صدر دجاج", "Pietto 'e pullastro"),
        "Chicken stock" to localized("Chicken stock", "Caldo de pollo", "Bouillon de poulet", "Hühnerbrühe", "Brodo di pollo", "مرق دجاج", "Brodo 'e pullastro"),
        "Chickpeas" to localized("Chickpeas", "Garbanzos", "Pois chiches", "Kichererbsen", "Ceci", "حمص حب", "Cicere"),
        "Coconut yogurt" to localized("Coconut yogurt", "Yogur de coco", "Yaourt de coco", "Kokosjoghurt", "Yogurt al cocco", "زبادي جوز الهند", "Yogurt 'e cocco"),
        "Cottage cheese" to localized("Cottage cheese", "Requesón", "Fromage cottage", "Hüttenkäse", "Fiocchi di latte", "جبن قريش", "Fiocche 'e latte"),
        "Cucumber" to localized("Cucumber", "Pepino", "Concombre", "Gurke", "Cetriolo", "خيار", "Cucummaro"),
        "Cumin" to localized("Cumin", "Comino", "Cumin", "Kreuzkümmel", "Cumino", "كمون", "Cumino"),
        "Dill" to localized("Dill", "Eneldo", "Aneth", "Dill", "Aneto", "شبت", "Aneto"),
        "Dried beans" to localized("Dried beans", "Frijoles secos", "Haricots secs", "Getrocknete Bohnen", "Fagioli secchi", "فاصوليا مجففة", "Fasule secche"),
        "Eggs" to localized("Eggs", "Huevos", "Oeufs", "Eier", "Uova", "بيض", "Ova"),
        "Firm tofu" to localized("Firm tofu", "Tofu firme", "Tofu ferme", "Fester Tofu", "Tofu compatto", "توفو متماسك", "Tofu tuosto"),
        "Fortified plant milk" to localized("Fortified plant milk", "Bebida vegetal fortificada", "Lait végétal enrichi", "Angereicherte Pflanzenmilch", "Latte vegetale fortificato", "حليب نباتي مدعم", "Latte vegetale rinfurzato"),
        "Fresh fruit" to localized("Fresh fruit", "Fruta fresca", "Fruits frais", "Frisches Obst", "Frutta fresca", "فاكهة طازجة", "Frutta fresca"),
        "Frozen vegetables" to localized("Frozen vegetables", "Verduras congeladas", "Légumes surgelés", "Tiefkühlgemüse", "Verdure surgelate", "خضار مجمدة", "Verdure gelate"),
        "Garlic" to localized("Garlic", "Ajo", "Ail", "Knoblauch", "Aglio", "ثوم", "Aglio"),
        "Gluten-free oats" to localized("Gluten-free oats", "Avena sin gluten", "Flocons d'avoine sans gluten", "Glutenfreie Haferflocken", "Avena senza glutine", "شوفان خال من الغلوتين", "Avena senza glutine"),
        "Greek yogurt" to localized("Greek yogurt", "Yogur griego", "Yaourt grec", "Griechischer Joghurt", "Yogurt greco", "زبادي يوناني", "Yogurt greco"),
        "Ground beef" to localized("Ground beef", "Carne molida de res", "Boeuf haché", "Rinderhackfleisch", "Carne macinata di manzo", "لحم بقري مفروم", "Carne macinata"),
        "Herbs" to localized("Herbs", "Hierbas", "Herbes", "Kräuter", "Erbe aromatiche", "أعشاب", "Erve"),
        "Honey" to localized("Honey", "Miel", "Miel", "Honig", "Miele", "عسل", "Mele"),
        "Hummus" to localized("Hummus", "Hummus", "Houmous", "Hummus", "Hummus", "حمص", "Hummus"),
        "Kidney beans" to localized("Kidney beans", "Alubias rojas", "Haricots rouges", "Kidneybohnen", "Fagioli rossi", "فاصوليا حمراء", "Fasule rosse"),
        "Lemon" to localized("Lemon", "Limón", "Citron", "Zitrone", "Limone", "ليمون", "Limone"),
        "Lemons" to localized("Lemons", "Limones", "Citrons", "Zitronen", "Limoni", "ليمون", "Limune"),
        "Lentils" to localized("Lentils", "Lentejas", "Lentilles", "Linsen", "Lenticchie", "عدس", "Lenticchie"),
        "Lettuce" to localized("Lettuce", "Lechuga", "Laitue", "Kopfsalat", "Lattuga", "خس", "Lattuca"),
        "Milk" to localized("Milk", "Leche", "Lait", "Milch", "Latte", "حليب", "Latte"),
        "Mixed greens" to localized("Mixed greens", "Hojas verdes mixtas", "Jeunes pousses mélangées", "Gemischter Blattsalat", "Insalata mista", "خضار ورقية مشكلة", "Nzalandella mmescata"),
        "Mixed salad greens" to localized("Mixed salad greens", "Mezcla de hojas para ensalada", "Mélange de salade verte", "Gemischter Salat", "Misticanza", "خضار سلطة مشكلة", "Nzalandella mmescata"),
        "Mushrooms" to localized("Mushrooms", "Champiñones", "Champignons", "Pilze", "Funghi", "فطر", "Funge"),
        "Oats" to localized("Oats", "Avena", "Flocons d'avoine", "Haferflocken", "Avena", "شوفان", "Avena"),
        "Olive oil" to localized("Olive oil", "Aceite de oliva", "Huile d'olive", "Olivenöl", "Olio d'oliva", "زيت زيتون", "Uoglio 'e uliva"),
        "Onion" to localized("Onion", "Cebolla", "Oignon", "Zwiebel", "Cipolla", "بصل", "Cepolla"),
        "Parmesan" to localized("Parmesan", "Parmesano", "Parmesan", "Parmesan", "Parmigiano", "بارميزان", "Parmigiano"),
        "Pasta" to localized("Pasta", "Pasta", "Pâtes", "Pasta", "Pasta", "معكرونة", "Pasta"),
        "Potatoes" to localized("Potatoes", "Patatas", "Pommes de terre", "Kartoffeln", "Patate", "بطاطس", "Patane"),
        "Pork" to localized("Pork", "Cerdo", "Porc", "Schwein", "Maiale", "لحم خنزير", "Puorco"),
        "Quinoa" to localized("Quinoa", "Quinoa", "Quinoa", "Quinoa", "Quinoa", "كينوا", "Quinoa"),
        "Rice" to localized("Rice", "Arroz", "Riz", "Reis", "Riso", "أرز", "Riso"),
        "Rice cakes" to localized("Rice cakes", "Tortitas de arroz", "Galettes de riz", "Reiswaffeln", "Gallette di riso", "كعكات أرز", "Gallette 'e riso"),
        "Salmon fillets" to localized("Salmon fillets", "Filetes de salmón", "Filets de saumon", "Lachsfilets", "Filetti di salmone", "شرائح سلمون", "Filette 'e salmone"),
        "Salmon" to localized("Salmon", "Salmón", "Saumon", "Lachs", "Salmone", "سلمون", "Salmone"),
        "Salsa" to localized("Salsa", "Salsa", "Sauce salsa", "Salsa", "Salsa", "صلصة سالسا", "Salsa"),
        "Salt" to localized("Salt", "Sal", "Sel", "Salz", "Sale", "ملح", "Sale"),
        "Sardines" to localized("Sardines", "Sardinas", "Sardines", "Sardinen", "Sardine", "سردين", "Sardine"),
        "Scallions" to localized("Scallions", "Cebolletas", "Cébettes", "Frühlingszwiebeln", "Cipollotti", "بصل أخضر", "Cipullette"),
        "Seasonal vegetables" to localized("Seasonal vegetables", "Verduras de temporada", "Légumes de saison", "Saisongemüse", "Verdure di stagione", "خضار موسمية", "Verdure 'e stagione"),
        "Sesame oil" to localized("Sesame oil", "Aceite de sésamo", "Huile de sésame", "Sesamöl", "Olio di sesamo", "زيت سمسم", "Uoglio 'e sesamo"),
        "Shrimp" to localized("Shrimp", "Gambas", "Crevettes", "Garnelen", "Gamberi", "روبيان", "Gammare"),
        "Soy sauce" to localized("Soy sauce", "Salsa de soja", "Sauce soja", "Sojasauce", "Salsa di soia", "صلصة الصويا", "Salsa 'e soia"),
        "Spinach" to localized("Spinach", "Espinacas", "Épinards", "Spinat", "Spinaci", "سبانخ", "Spinace"),
        "Sunflower seed butter" to localized("Sunflower seed butter", "Crema de semillas de girasol", "Beurre de graines de tournesol", "Sonnenblumenkernmus", "Crema di semi di girasole", "زبدة بذور دوار الشمس", "Crema 'e semmente 'e girasole"),
        "Tomatoes" to localized("Tomatoes", "Tomates", "Tomates", "Tomaten", "Pomodori", "طماطم", "Pummarole"),
        "Tortillas" to localized("Tortillas", "Tortillas", "Tortillas", "Tortillas", "Tortillas", "تورتيلا", "Tortillas"),
        "Turkey slices" to localized("Turkey slices", "Lonchas de pavo", "Tranches de dinde", "Putenscheiben", "Fette di tacchino", "شرائح ديك رومي", "Fette 'e tacchino"),
        "Vegetable broth" to localized("Vegetable broth", "Caldo de verduras", "Bouillon de légumes", "Gemüsebrühe", "Brodo vegetale", "مرق خضار", "Brodo 'e verdure"),
        "Whole-grain bread" to localized("Whole-grain bread", "Pan integral", "Pain complet", "Vollkornbrot", "Pane integrale", "خبز حبوب كاملة", "Pane integrale"),
        "Whole-wheat pasta" to localized("Whole-wheat pasta", "Pasta integral", "Pâtes complètes", "Vollkornpasta", "Pasta integrale", "معكرونة قمح كامل", "Pasta integrale"),
    )

    private val intentTerms = mapOf(
        Intent.Protein to listOf("protein", "proteína", "proteico", "protéiné", "proteinreich", "proteine", "proteici", "بروتين", "muscle", "muscolo"),
        Intent.Vegetarian to listOf("vegetarian", "vegetariano", "végétarien", "vegetarisch", "vegetariana", "vegetale", "نباتي", "vegan", "vegano"),
        Intent.Budget to listOf("budget", "cheap", "save", "económico", "economica", "économique", "günstig", "economico", "اقتصادي", "risparmio"),
        Intent.Allergy to listOf("allerg", "alérgen", "allergène", "allergiker", "allergeni", "حساسية", "allergie"),
        Intent.Family to listOf("kid", "child", "family", "niños", "famille", "enfants", "familie", "kinder", "famiglia", "bambini", "عائلة", "أطفال", "criature"),
        Intent.Vegetable to listOf("vegetable", "veggie", "salad", "verdura", "verduras", "légume", "gemüse", "insalata", "خضار", "verdure"),
        Intent.Fish to listOf("fish", "omega", "pescado", "poisson", "fisch", "pesce", "سمك"),
        Intent.Breakfast to listOf("breakfast", "desayuno", "petit-déjeuner", "frühstück", "colazione", "فطور", "colazzione"),
        Intent.Quick to listOf("quick", "20-min", "20 min", "rápido", "rapide", "schnell", "veloce", "سريع", "ambressa"),
    )

    private val ingredientTerms = mapOf(
        "beans" to listOf("beans", "frijoles", "haricots", "bohnen", "fagioli", "فاصوليا", "fasule"),
        "beef" to listOf("beef", "ground beef", "steak", "res", "boeuf", "rind", "manzo", "لحم بقري", "carne"),
        "broccoli" to listOf("broccoli", "brócoli", "brocoli", "brokkoli", "بروكلي", "broccole"),
        "cheese" to listOf("cheese", "mozzarella", "cheddar", "queso", "fromage", "käse", "formaggio", "جبن", "caso"),
        "chicken" to listOf("chicken", "pollo", "poulet", "hähnchen", "دجاج", "pullastro"),
        "eggs" to listOf("egg", "eggs", "huevo", "oeuf", "oeufs", "ei", "eier", "uovo", "uova", "بيض", "ova"),
        "mushrooms" to listOf("mushroom", "mushrooms", "champiñones", "champignons", "pilze", "funghi", "فطر"),
        "pasta" to listOf("pasta", "spaghetti", "penne", "pâtes", "معكرونة"),
        "pork" to listOf("pork", "pork chop", "bacon", "cerdo", "porc", "schwein", "maiale", "لحم خنزير"),
        "potatoes" to listOf("potato", "potatoes", "patata", "patatas", "pomme de terre", "kartoffel", "patate", "بطاطس", "patane"),
        "rice" to listOf("rice", "arroz", "riz", "reis", "riso", "أرز"),
        "salmon" to listOf("salmon", "fish", "cod", "salmón", "pescado", "saumon", "poisson", "lachs", "fisch", "salmone", "pesce", "سلمون", "سمك"),
        "shrimp" to listOf("shrimp", "prawn", "gambas", "crevettes", "garnelen", "gamberi", "روبيان"),
        "spinach" to listOf("spinach", "espinacas", "épinards", "spinat", "spinaci", "سبانخ", "spinace"),
        "tofu" to listOf("tofu", "توفو"),
        "tomatoes" to listOf("tomato", "tomatoes", "tomate", "tomates", "tomaten", "pomodori", "طماطم", "pummarole"),
        "tortillas" to listOf("tortilla", "tortillas", "تورتيلا"),
    )

    fun labelFor(englishLabel: String, languageCode: String): String =
        labels[englishLabel]?.get(languageKey(languageCode)) ?: englishLabel

    fun ingredientNameFor(id: String, languageCode: String): String =
        labels[ingredientLabelKey(id)]?.get(languageKey(languageCode)) ?: id

    fun hasIntent(text: String, intent: Intent): Boolean =
        containsAny(text, intentTerms.getValue(intent))

    fun containsIngredient(text: String, ingredientId: String): Boolean =
        containsAny(text, ingredientTerms.getValue(ingredientId))

    private fun ingredientLabelKey(id: String): String = when (id) {
        "beans" -> "Beans"
        "beef" -> "Ground beef"
        "chicken" -> "Chicken"
        "mushrooms" -> "Mushrooms"
        "pork" -> "Pork"
        "salmon" -> "Salmon"
        "shrimp" -> "Shrimp"
        "tomatoes" -> "Tomatoes"
        else -> labels.keys.firstOrNull { it.equals(id, ignoreCase = true) } ?: id
    }

    private fun containsAny(text: String, terms: List<String>): Boolean {
        val normalized = text.lowercase()
        return terms.any { term -> normalized.contains(term.lowercase()) }
    }

    private fun languageKey(languageCode: String): String {
        if (languageCode == AR || languageCode == "ar-rSA") return AR
        return when (SupportedAppLanguages.normalize(languageCode)) {
            ES -> ES
            FR -> FR
            DE -> DE
            IT -> IT
            NAP -> NAP
            else -> EN
        }
    }

    private fun localized(
        en: String,
        es: String,
        fr: String,
        de: String,
        it: String,
        ar: String,
        nap: String,
    ): Map<String, String> = mapOf(
        EN to en,
        ES to es,
        FR to fr,
        DE to de,
        IT to it,
        AR to ar,
        NAP to nap,
    )
}
