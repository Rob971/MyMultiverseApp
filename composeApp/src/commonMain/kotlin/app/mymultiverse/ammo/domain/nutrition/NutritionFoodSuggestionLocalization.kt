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

    private val mealDishes = mapOf(
        // Vegetarian lunches
        "Lentil soup with whole-grain bread" to localized("Lentil soup with whole-grain bread", "Sopa de lentejas con pan integral", "Soupe de lentilles avec pain complet", "Linsensuppe mit Vollkornbrot", "Zuppa di lenticchie con pane integrale", "شوربة عدس مع خبز حبوب كاملة", "Zuppa 'e lenticchie cu pane integrale"),
        "Chickpea salad bowl with tahini" to localized("Chickpea salad bowl with tahini", "Ensalada de garbanzos con tahini", "Salade de pois chiches au tahin", "Kichererbsensalat mit Tahini", "Insalata di ceci con tahini", "سلطة حمص مع طحينة", "Nzalandella 'e cicere cu tahini"),
        "Veggie wrap with hummus" to localized("Veggie wrap with hummus", "Wrap de verduras con hummus", "Wrap aux légumes et houmous", "Gemüse-Wrap mit Hummus", "Wrap di verdure con hummus", "راب خضار مع حمص", "Wrap 'e verdure cu hummus"),
        "Tofu stir-fry with brown rice" to localized("Tofu stir-fry with brown rice", "Tofu salteado con arroz integral", "Tofu sauté au riz complet", "Tofu-Pfanne mit Naturreis", "Tofu saltato in padella con riso integrale", "توفو مقلي مع أرز بني", "Tofu fritto cu riso integrale"),
        "Minestrone with beans and greens" to localized("Minestrone with beans and greens", "Minestrone con judías y verduras", "Minestrone aux haricots et légumes verts", "Minestrone mit Bohnen und Gemüse", "Minestrone con fagioli e verdure", "مينيسترون مع فاصوليا وخضروات", "Minestrone cu fasule e verdure"),
        "Quinoa tabbouleh with falafel" to localized("Quinoa tabbouleh with falafel", "Tabulé de quinoa con falafel", "Taboulé de quinoa avec falafel", "Quinoa-Taboulé mit Falafel", "Tabulè di quinoa con falafel", "تبولة كينوا مع فلافل", "Tabulè 'e quinoa cu falafel"),
        "Roasted vegetable pasta" to localized("Roasted vegetable pasta", "Pasta con verduras asadas", "Pâtes aux légumes rôtis", "Pasta mit geröstetem Gemüse", "Pasta con verdure arrosto", "معكرونة مع خضار مشوية", "Pasta cu verdure arrustute"),
        // Vegetarian dinners
        "Black bean tacos with salsa" to localized("Black bean tacos with salsa", "Tacos de frijoles negros con salsa", "Tacos aux haricots noirs et salsa", "Schwarze-Bohnen-Tacos mit Salsa", "Tacos di fagioli neri con salsa", "تاكو فاصوليا سوداء مع صلصة", "Tacos 'e fasule nire cu salsa"),
        "Eggplant parmesan with salad" to localized("Eggplant parmesan with salad", "Berenjena a la parmesana con ensalada", "Aubergine à la parmesane avec salade", "Auberginen-Parmesan mit Salat", "Parmigiana di melanzane con insalata", "باذنجان بالبارميزان مع سلطة", "Parmigiana 'e mulignane cu nzalandella"),
        "Thai coconut vegetable curry" to localized("Thai coconut vegetable curry", "Curry tailandés de coco y verduras", "Curry thaï de légumes au lait de coco", "Thai-Gemüse-Curry mit Kokosmilch", "Curry thai di verdure al cocco", "كاري خضار تايلاندي بجوز الهند", "Curry thai 'e verdure a cocco"),
        "Stuffed bell peppers with rice" to localized("Stuffed bell peppers with rice", "Pimientos rellenos con arroz", "Poivrons farcis au riz", "Gefüllte Paprika mit Reis", "Peperoni ripieni con riso", "فلفل محشو بالأرز", "Pupacchielle chiene cu riso"),
        "Mushroom risotto with peas" to localized("Mushroom risotto with peas", "Risotto de champiñones con guisantes", "Risotto aux champignons et petits pois", "Pilzrisotto mit Erbsen", "Risotto ai funghi con piselli", "ريزوتو فطر مع بازلاء", "Risotto 'e funge cu piselle"),
        "Baked sweet potato with tahini bowl" to localized("Baked sweet potato with tahini bowl", "Batata al horno con tahini", "Patate douce au four avec tahin", "Gebackene Süßkartoffel mit Tahini", "Patata dolce al forno con tahini", "بطاطا حلوة بالفرن مع طحينة", "Patata doce a furno cu tahini"),
        "Vegetable lasagna" to localized("Vegetable lasagna", "Lasaña de verduras", "Lasagnes aux légumes", "Gemüselasagne", "Lasagne di verdure", "لازانيا خضار", "Lasagne 'e verdure"),
        // High-protein lunches
        "Grilled chicken salad with quinoa" to localized("Grilled chicken salad with quinoa", "Ensalada de pollo a la plancha con quinoa", "Salade de poulet grillé et quinoa", "Gegrillter Hähnchensalat mit Quinoa", "Insalata di pollo alla griglia con quinoa", "سلطة دجاج مشوي مع كينوا", "Nzalandella 'e pullastro a griglia cu quinoa"),
        "Turkey and avocado whole-grain wrap" to localized("Turkey and avocado whole-grain wrap", "Wrap integral de pavo y aguacate", "Wrap complet dinde et avocat", "Vollkorn-Wrap mit Pute und Avocado", "Wrap integrale di tacchino e avocado", "راب حبوب كاملة بالديك الرومي والأفوكادو", "Wrap integrale 'e tacchino e avocado"),
        "Tuna niçoise salad" to localized("Tuna niçoise salad", "Ensalada niçoise de atún", "Salade niçoise au thon", "Thunfisch-Niçoise-Salat", "Insalata nizzarda con tonno", "سلطة نيسواز بالتونة", "Nzalandella nizzarda cu tonno"),
        "Greek yogurt bowl with nuts and berries" to localized("Greek yogurt bowl with nuts and berries", "Bol de yogur griego con nueces y frutos rojos", "Bol de yaourt grec aux noix et fruits rouges", "Griechischer Joghurt-Bowl mit Nüssen und Beeren", "Ciotola di yogurt greco con noci e frutti di bosco", "وعاء يوناني بالزبادي والمكسرات والتوت", "Ciotola 'e yogurt greco cu nuce e frutte 'e vosco"),
        "Beef and vegetable stir-fry" to localized("Beef and vegetable stir-fry", "Salteado de carne y verduras", "Sauté de boeuf et légumes", "Rindfleisch-Gemüse-Pfanne", "Manzo saltato in padella con verdure", "لحم بقري مقلي مع خضار", "Carne macinata fritta cu verdure"),
        "Salmon poke bowl" to localized("Salmon poke bowl", "Poke bowl de salmón", "Poke bowl au saumon", "Lachs-Poke-Bowl", "Poke bowl al salmone", "بوكي بول بالسلمون", "Poke bowl cu salmone"),
        "Egg and spinach frittata with fruit" to localized("Egg and spinach frittata with fruit", "Frittata de huevo y espinacas con fruta", "Frittata aux œufs et épinards avec fruit", "Ei-Spinat-Frittata mit Obst", "Frittata di uova e spinaci con frutta", "فريتاتا البيض والسبانخ مع فاكهة", "Frittata 'e ova e spinace cu frutta"),
        // High-protein dinners
        "Baked salmon with roasted broccoli" to localized("Baked salmon with roasted broccoli", "Salmón al horno con brócoli asado", "Saumon au four et brocoli rôti", "Gebackener Lachs mit geröstetem Brokkoli", "Salmone al forno con broccoli arrostiti", "سلمون بالفرن مع بروكلي مشوي", "Salmone a furno cu broccole arrustuto"),
        "Lean beef chili with beans" to localized("Lean beef chili with beans", "Chili de carne magra con judías", "Chili de boeuf maigre aux haricots", "Mageres Rindfleisch-Chili mit Bohnen", "Chili di manzo magro con fagioli", "تشيلي لحم بقري قليل الدسم مع فاصوليا", "Chili 'e carne magra cu fasule"),
        "Chicken thighs with sweet potato" to localized("Chicken thighs with sweet potato", "Muslos de pollo con batata", "Cuisses de poulet à la patate douce", "Hähnchenschenkel mit Süßkartoffel", "Cosce di pollo con patata dolce", "أفخاذ دجاج مع بطاطا حلوة", "Cosce 'e pullastro cu patata doce"),
        "Shrimp and vegetable skewers" to localized("Shrimp and vegetable skewers", "Brochetas de gambas y verduras", "Brochettes de crevettes et légumes", "Garnelen-Gemüse-Spieße", "Spiedini di gamberi e verdure", "أسياخ روبيان وخضار", "Spiedini 'e gammare e verdure"),
        "Turkey meatballs with zucchini noodles" to localized("Turkey meatballs with zucchini noodles", "Albóndigas de pavo con fideos de calabacín", "Boulettes de dinde avec spaghettis de courgette", "Putenfrikadellen mit Zucchinispaghetti", "Polpette di tacchino con spaghetti di zucchine", "كرات لحم الديك الرومي مع نودلز الكوسة", "Purpette 'e tacchino cu spaghette 'e cucuzziell"),
        "Pork tenderloin with green beans" to localized("Pork tenderloin with green beans", "Lomo de cerdo con judías verdes", "Filet de porc aux haricots verts", "Schweinefilet mit grünen Bohnen", "Filetto di maiale con fagiolini", "فيليه لحم الخنزير مع فاصوليا خضراء", "Filetto 'e puorco cu fagiolini"),
        "White fish with herbed lentils" to localized("White fish with herbed lentils", "Pescado blanco con lentejas a las hierbas", "Poisson blanc aux lentilles aux herbes", "Weißfisch mit Kräuterlinsen", "Pesce bianco con lenticchie alle erbe", "سمك أبيض مع عدس بالأعشاب", "Pesce bianco cu lenticchie alle erve"),
        // Budget lunches
        "Rice and beans with sautéed peppers" to localized("Rice and beans with sautéed peppers", "Arroz y judías con pimientos salteados", "Riz et haricots aux poivrons sautés", "Reis und Bohnen mit sautierten Paprika", "Riso e fagioli con peperoni saltati", "أرز وفاصوليا مع فلفل مقلي", "Riso e fasule cu pupacchielle fritte"),
        "Pasta with tomato and chickpeas" to localized("Pasta with tomato and chickpeas", "Pasta con tomate y garbanzos", "Pâtes à la tomate et aux pois chiches", "Pasta mit Tomaten und Kichererbsen", "Pasta al pomodoro con ceci", "معكرونة بالطماطم والحمص", "Pasta cu pummarola e cicere"),
        "Egg fried rice with frozen vegetables" to localized("Egg fried rice with frozen vegetables", "Arroz frito con huevo y verduras congeladas", "Riz frit aux œufs et légumes surgelés", "Gebratener Reis mit Ei und Tiefkühlgemüse", "Riso fritto con uova e verdure surgelate", "أرز مقلي مع بيض وخضار مجمدة", "Riso fritto cu ova e verdure gelate"),
        "Lentil stew with bread" to localized("Lentil stew with bread", "Guiso de lentejas con pan", "Ragoût de lentilles avec pain", "Linseneintopf mit Brot", "Stufato di lenticchie con pane", "يخنة العدس مع خبز", "Stufato 'e lenticchie cu pane"),
        "Tuna pasta salad" to localized("Tuna pasta salad", "Ensalada de pasta con atún", "Salade de pâtes au thon", "Thunfisch-Nudelsalat", "Insalata di pasta al tonno", "سلطة معكرونة بالتونة", "Nzalandella 'e pasta cu tonno"),
        "Potato and vegetable soup" to localized("Potato and vegetable soup", "Sopa de patatas y verduras", "Soupe de pommes de terre et légumes", "Kartoffel-Gemüse-Suppe", "Zuppa di patate e verdure", "شوربة البطاطس والخضار", "Zuppa 'e patane e verdure"),
        "Peanut butter sandwich with carrot sticks" to localized("Peanut butter sandwich with carrot sticks", "Sándwich de mantequilla de cacahuete con zanahorias", "Sandwich au beurre de cacahuète avec carottes", "Erdnussbutter-Sandwich mit Karottensticks", "Sandwich al burro di arachidi con carote", "ساندويش زبدة الفول السوداني مع عيدان الجزر", "Panino cu butirro 'e nocelle cu carotine"),
        // Budget dinners
        "Chili con carne with rice" to localized("Chili con carne with rice", "Chili con carne y arroz", "Chili con carne avec riz", "Chili con carne mit Reis", "Chili con carne con riso", "تشيلي كون كارني مع أرز", "Chili cu carne e riso"),
        "Baked pasta with vegetables" to localized("Baked pasta with vegetables", "Pasta al horno con verduras", "Pâtes au four avec légumes", "Überbackene Pasta mit Gemüse", "Pasta al forno con verdure", "معكرونة بالفرن مع خضار", "Pasta a furno cu verdure"),
        "Chicken and cabbage skillet" to localized("Chicken and cabbage skillet", "Sartén de pollo y repollo", "Poêlée de poulet et chou", "Hähnchen-Kohl-Pfanne", "Padella di pollo e cavolo", "مقلاة دجاج وملفوف", "Padella 'e pullastro e cavolo"),
        "Bean burritos with salsa" to localized("Bean burritos with salsa", "Burritos de judías con salsa", "Burritos aux haricots et salsa", "Bohnen-Burritos mit Salsa", "Burritos di fagioli con salsa", "بريتو فاصوليا مع صلصة", "Burritos 'e fasule cu salsa"),
        "Vegetable omelette with toast" to localized("Vegetable omelette with toast", "Tortilla de verduras con tostada", "Omelette aux légumes avec toast", "Gemüse-Omelett mit Toast", "Frittata di verdure con pane tostato", "عجة خضار مع توست", "Frittata 'e verdure cu toast"),
        "Sardine tomato pasta" to localized("Sardine tomato pasta", "Pasta de sardinas y tomate", "Pâtes aux sardines et tomates", "Sardinen-Tomaten-Pasta", "Pasta con sardine e pomodoro", "معكرونة السردين والطماطم", "Pasta cu sardine e pummarola"),
        "Slow-cooker lentil curry" to localized("Slow-cooker lentil curry", "Curry de lentejas en olla lenta", "Curry de lentilles à la mijoteuse", "Slow-Cooker-Linsen-Curry", "Curry di lenticchie in pentola lenta", "كاري عدس بالطهي البطيء", "Curry 'e lenticchie a fuoco lento"),
        // Allergy lunches
        "Grilled chicken with rice and cucumbers" to localized("Grilled chicken with rice and cucumbers", "Pollo a la plancha con arroz y pepinos", "Poulet grillé avec riz et concombres", "Gegrilltes Hähnchen mit Reis und Gurken", "Pollo alla griglia con riso e cetrioli", "دجاج مشوي مع أرز وخيار", "Pullastro a griglia cu riso e cucummari"),
        "Turkey lettuce cups with fruit" to localized("Turkey lettuce cups with fruit", "Rollitos de lechuga con pavo y fruta", "Coupes de laitue à la dinde avec fruit", "Putensalat-Cups mit Obst", "Coppe di lattuga con tacchino e frutta", "كوب خس بالديك الرومي مع فاكهة", "Coppe 'e lattuca cu tacchino e frutta"),
        "Rice noodles with tamari vegetables" to localized("Rice noodles with tamari vegetables", "Fideos de arroz con verduras y tamari", "Nouilles de riz aux légumes et tamari", "Reisnudeln mit Tamari-Gemüse", "Noodles di riso con verdure al tamari", "نودلز أرز مع خضار تاماري", "Noodles 'e riso cu verdure e tamari"),
        "Sunflower butter banana sandwich (GF)" to localized("Sunflower butter banana sandwich (GF)", "Sándwich de mantequilla de girasol y plátano (sin gluten)", "Sandwich beurre de tournesol banane (sans gluten)", "Sonnenblumenbutter-Bananen-Sandwich (glutenfrei)", "Panino con burro di girasole e banana (senza glutine)", "ساندويش زبدة عباد الشمس بالموز (خالي من الغلوتين)", "Panino cu butirro 'e girasole e banana (senza glutine)"),
        "Baked cod with potatoes" to localized("Baked cod with potatoes", "Bacalao al horno con patatas", "Cabillaud au four avec pommes de terre", "Gebackener Kabeljau mit Kartoffeln", "Merluzzo al forno con patate", "قد بالفرن مع بطاطس", "Merluzzo a furno cu patane"),
        "Quinoa salad with olive oil dressing" to localized("Quinoa salad with olive oil dressing", "Ensalada de quinoa con vinagreta de aceite de oliva", "Salade de quinoa avec vinaigrette à l'huile d'olive", "Quinoa-Salat mit Olivenöl-Dressing", "Insalata di quinoa con condimento all'olio d'oliva", "سلطة كينوا مع تتبيلة زيت الزيتون", "Nzalandella 'e quinoa cu condimento 'e uoglio 'e uliva"),
        "Roasted chicken with carrots" to localized("Roasted chicken with carrots", "Pollo asado con zanahorias", "Poulet rôti avec carottes", "Gebratenes Hähnchen mit Karotten", "Pollo arrosto con carote", "دجاج مشوي مع جزر", "Pullastro arrustuto cu carote"),
        // Allergy dinners
        "Beef and vegetable stew (nut-free)" to localized("Beef and vegetable stew (nut-free)", "Estofado de carne y verduras (sin frutos secos)", "Ragoût de boeuf et légumes (sans noix)", "Rindfleisch-Gemüse-Eintopf (nussfrei)", "Stufato di manzo e verdure (senza noci)", "يخنة لحم وخضار (بدون مكسرات)", "Stufato 'e carne e verdure (senza nuce)"),
        "Salmon with rice and green beans" to localized("Salmon with rice and green beans", "Salmón con arroz y judías verdes", "Saumon avec riz et haricots verts", "Lachs mit Reis und grünen Bohnen", "Salmone con riso e fagiolini", "سلمون مع أرز وفاصوليا خضراء", "Salmone cu riso e fagiolini"),
        "Chicken stir-fry with coconut aminos" to localized("Chicken stir-fry with coconut aminos", "Salteado de pollo con aminoácidos de coco", "Sauté de poulet aux aminos de noix de coco", "Hähnchen-Pfanne mit Kokosaminos", "Pollo saltato in padella con aminoacidi di cocco", "دجاج مقلي مع أمينو جوز الهند", "Pullastro fritto cu aminoacidi 'e cocco"),
        "Pork chops with mashed potatoes" to localized("Pork chops with mashed potatoes", "Chuletas de cerdo con puré de patatas", "Côtelettes de porc avec purée de pommes de terre", "Schweinekoteletts mit Kartoffelpüree", "Braciole di maiale con purè di patate", "شرائح لحم الخنزير مع بيوريه البطاطس", "Braciole 'e puorco cu purè 'e patane"),
        "Turkey patties with roasted squash" to localized("Turkey patties with roasted squash", "Hamburguesas de pavo con calabaza asada", "Galettes de dinde avec courge rôtie", "Putenpastetchen mit geröstetem Kürbis", "Polpette di tacchino con zucca arrostita", "قطع الديك الرومي مع قرع مشوي", "Purpette 'e tacchino cu zucca arrustuta"),
        "White fish with herb rice" to localized("White fish with herb rice", "Pescado blanco con arroz de hierbas", "Poisson blanc avec riz aux herbes", "Weißfisch mit Kräuterreis", "Pesce bianco con riso alle erbe", "سمك أبيض مع أرز بالأعشاب", "Pesce bianco cu riso alle erve"),
        "Hearty vegetable soup with bread" to localized("Hearty vegetable soup with bread", "Sopa abundante de verduras con pan", "Soupe de légumes généreuse avec pain", "Herzhafte Gemüsesuppe mit Brot", "Zuppa abbondante di verdure con pane", "شوربة خضار دسمة مع خبز", "Zuppa abbondante 'e verdure cu pane"),
        // Family lunches
        "Mini whole-wheat pizzas with salad" to localized("Mini whole-wheat pizzas with salad", "Mini pizzas integrales con ensalada", "Mini pizzas au blé complet avec salade", "Mini-Vollkornpizzas mit Salat", "Mini pizze integrali con insalata", "ميني بيتزا بالقمح الكامل مع سلطة", "Mini pizze integrali cu nzalandella"),
        "Chicken quesadilla with fruit" to localized("Chicken quesadilla with fruit", "Quesadilla de pollo con fruta", "Quesadilla au poulet avec fruit", "Hähnchen-Quesadilla mit Obst", "Quesadilla di pollo con frutta", "كيساديا دجاج مع فاكهة", "Quesadilla 'e pullastro cu frutta"),
        "Pasta with mild tomato sauce" to localized("Pasta with mild tomato sauce", "Pasta con salsa de tomate suave", "Pâtes avec sauce tomate douce", "Pasta mit milder Tomatensauce", "Pasta con salsa di pomodoro leggera", "معكرونة مع صلصة طماطم خفيفة", "Pasta cu salsa 'e pummarola leggera"),
        "Turkey and cheese roll-ups" to localized("Turkey and cheese roll-ups", "Rollitos de pavo y queso", "Roulés dinde et fromage", "Puten-Käse-Röllchen", "Rotolini di tacchino e formaggio", "لفائف الديك الرومي والجبن", "Rotulille 'e tacchino e caso"),
        "Homemade chicken soup with bread" to localized("Homemade chicken soup with bread", "Sopa de pollo casera con pan", "Soupe de poulet maison avec pain", "Hausgemachte Hühnersuppe mit Brot", "Zuppa di pollo fatta in casa con pane", "حساء الدجاج المنزلي مع خبز", "Zuppa 'e pullastro fatta 'n casa cu pane"),
        "Fish sticks with peas and carrots" to localized("Fish sticks with peas and carrots", "Palitos de pescado con guisantes y zanahorias", "Bâtonnets de poisson avec petits pois et carottes", "Fischstäbchen mit Erbsen und Karotten", "Bastoncini di pesce con piselli e carote", "أصابع السمك مع بازلاء وجزر", "Bastuncielle 'e pesce cu piselle e carote"),
        "Build-your-own taco bar" to localized("Build-your-own taco bar", "Barra de tacos para armar tus propios", "Bar à tacos à composer soi-même", "Selbstgebauter Taco-Bar", "Taco bar fai da te", "بار تاكو قم ببنائه بنفسك", "Taco bar fa da te"),
        // Family dinners
        "Spaghetti with turkey meatballs" to localized("Spaghetti with turkey meatballs", "Espaguetis con albóndigas de pavo", "Spaghettis aux boulettes de dinde", "Spaghetti mit Putenfrikadellen", "Spaghetti con polpette di tacchino", "سباغيتي مع كرات لحم الديك الرومي", "Spaghette cu purpette 'e tacchino"),
        "Baked chicken strips with potatoes" to localized("Baked chicken strips with potatoes", "Tiras de pollo al horno con patatas", "Lanières de poulet au four avec pommes de terre", "Gebackene Hähnchen-Streifen mit Kartoffeln", "Strisce di pollo al forno con patate", "شرائح الدجاج بالفرن مع بطاطس", "Strisce 'e pullastro a furno cu patane"),
        "Mild vegetable curry with rice" to localized("Mild vegetable curry with rice", "Curry suave de verduras con arroz", "Curry de légumes doux avec riz", "Mildes Gemüse-Curry mit Reis", "Curry di verdure leggero con riso", "كاري خضار خفيف مع أرز", "Curry leggero 'e verdure cu riso"),
        "Homemade burgers with sweet potato fries" to localized("Homemade burgers with sweet potato fries", "Hamburguesas caseras con patatas fritas de batata", "Burgers maison avec frites de patate douce", "Hausgemachte Burger mit Süßkartoffel-Pommes", "Hamburger fatti in casa con patatine di patata dolce", "برجر منزلي مع بطاطس حلوة مقلية", "Hamburger fatti 'n casa cu patatine 'e patata doce"),
        "Baked mac and cheese with broccoli" to localized("Baked mac and cheese with broccoli", "Mac and cheese al horno con brócoli", "Mac and cheese au four avec brocoli", "Gebackenes Mac and Cheese mit Brokkoli", "Mac and cheese al forno con broccoli", "مكرونة بالجبن بالفرن مع بروكلي", "Mac and cheese a furno cu broccole"),
        "Sheet-pan sausage and vegetables" to localized("Sheet-pan sausage and vegetables", "Salchichas y verduras en bandeja", "Saucisses et légumes à la plaque", "Bratwurst und Gemüse vom Backblech", "Salsicce e verdure in teglia", "نقانق وخضار على صينية", "Salsiccio e verdure 'n teglia"),
        "Breakfast-for-dinner: eggs and pancakes" to localized("Breakfast-for-dinner: eggs and pancakes", "Desayuno para cenar: huevos y tortitas", "Petit-déjeuner au dîner: œufs et pancakes", "Frühstück zum Abendessen: Eier und Pancakes", "Colazione a cena: uova e pancake", "فطور في العشاء: بيض وفطائر", "Colazzione a cena: ova e pancake"),
        // Quick lunches
        "20-min veggie omelette with toast" to localized("20-min veggie omelette with toast", "Tortilla de verduras de 20 min con tostada", "Omelette aux légumes en 20 min avec toast", "20-min-Gemüse-Omelett mit Toast", "Frittata di verdure in 20 min con pane tostato", "عجة خضار 20 دقيقة مع توست", "Frittata 'e verdure 'n 20 min cu toast"),
        "Quick turkey and cheese wrap" to localized("Quick turkey and cheese wrap", "Wrap rápido de pavo y queso", "Wrap rapide dinde et fromage", "Schneller Puten-Käse-Wrap", "Wrap veloce di tacchino e formaggio", "راب سريع بالديك الرومي والجبن", "Wrap veloce 'e tacchino e caso"),
        "Tuna salad sandwich with fruit" to localized("Tuna salad sandwich with fruit", "Sándwich de ensalada de atún con fruta", "Sandwich salade de thon avec fruit", "Thunfischsalat-Sandwich mit Obst", "Sandwich di insalata di tonno con frutta", "ساندويش سلطة التونة مع فاكهة", "Panino 'e nzalandella 'e tonno cu frutta"),
        "Microwave lentil soup with bread" to localized("Microwave lentil soup with bread", "Sopa de lentejas al microondas con pan", "Soupe de lentilles au micro-ondes avec pain", "Linsensuppe aus der Mikrowelle mit Brot", "Zuppa di lenticchie al microonde con pane", "شوربة عدس بالميكروويف مع خبز", "Zuppa 'e lenticchie a microonde cu pane"),
        "Greek yogurt bowl with granola" to localized("Greek yogurt bowl with granola", "Bol de yogur griego con granola", "Bol de yaourt grec avec granola", "Griechischer Joghurt-Bowl mit Granola", "Ciotola di yogurt greco con granola", "وعاء زبادي يوناني مع غرانولا", "Ciotola 'e yogurt greco cu granola"),
        "Quesadilla with black beans" to localized("Quesadilla with black beans", "Quesadilla con frijoles negros", "Quesadilla aux haricots noirs", "Quesadilla mit schwarzen Bohnen", "Quesadilla con fagioli neri", "كيساديا مع فاصوليا سوداء", "Quesadilla cu fasule nire"),
        "Caprese salad with whole-grain crackers" to localized("Caprese salad with whole-grain crackers", "Ensalada caprese con crackers integrales", "Salade caprese avec crackers complets", "Caprese-Salat mit Vollkorncrackern", "Insalata caprese con crackers integrali", "سلطة كابريزي مع كراكر حبوب كاملة", "Nzalandella caprese cu crackers integrali"),
        // Quick dinners
        "20-min chicken stir-fry with rice" to localized("20-min chicken stir-fry with rice", "Pollo salteado en 20 min con arroz", "Sauté de poulet en 20 min avec riz", "20-min-Hähnchen-Pfanne mit Reis", "Pollo saltato in padella in 20 min con riso", "دجاج مقلي 20 دقيقة مع أرز", "Pullastro fritto 'n 20 min cu riso"),
        "Sheet-pan salmon and broccoli" to localized("Sheet-pan salmon and broccoli", "Salmón y brócoli en bandeja", "Saumon et brocoli à la plaque", "Lachs und Brokkoli vom Backblech", "Salmone e broccoli in teglia", "سلمون وبروكلي على صينية", "Salmone e broccole 'n teglia"),
        "Quick pasta with tomato and basil" to localized("Quick pasta with tomato and basil", "Pasta rápida con tomate y albahaca", "Pâtes rapides tomate et basilic", "Schnelle Pasta mit Tomate und Basilikum", "Pasta veloce al pomodoro e basilico", "معكرونة سريعة بالطماطم والريحان", "Pasta veloce cu pummarola e vasinicola"),
        "Beef and vegetable skillet" to localized("Beef and vegetable skillet", "Sartén de carne y verduras", "Poêlée de boeuf et légumes", "Rindfleisch-Gemüse-Pfanne", "Padella di manzo e verdure", "مقلاة لحم وخضار", "Padella 'e carne e verdure"),
        "Shrimp tacos with slaw" to localized("Shrimp tacos with slaw", "Tacos de gambas con ensalada de col", "Tacos aux crevettes avec salade de chou", "Garnelen-Tacos mit Krautsalat", "Tacos di gamberi con insalata di cavolo", "تاكو روبيان مع سلطة الملفوف", "Tacos 'e gammare cu nzalandella 'e cavolo"),
        "Egg fried rice with peas" to localized("Egg fried rice with peas", "Arroz frito con huevo y guisantes", "Riz frit aux œufs et petits pois", "Gebratener Reis mit Ei und Erbsen", "Riso fritto con uova e piselli", "أرز مقلي مع بيض وبازلاء", "Riso fritto cu ova e piselle"),
        "Baked gnocchi with spinach" to localized("Baked gnocchi with spinach", "Ñoquis al horno con espinacas", "Gnocchis au four avec épinards", "Gebackene Gnocchi mit Spinat", "Gnocchi al forno con spinaci", "نيوكي بالفرن مع سبانخ", "Gnocchi a furno cu spinace"),
        // Balanced lunches
        "Mediterranean grain bowl with chicken" to localized("Mediterranean grain bowl with chicken", "Bol mediterráneo de cereales con pollo", "Bol de céréales méditerranéen avec poulet", "Mediterraner Getreide-Bowl mit Hähnchen", "Ciotola mediterranea di cereali con pollo", "وعاء حبوب متوسطية مع دجاج", "Ciotola mediterranea 'e cereali cu pullastro"),
        "Vegetable soup with whole-grain roll" to localized("Vegetable soup with whole-grain roll", "Sopa de verduras con panecillo integral", "Soupe de légumes avec petit pain complet", "Gemüsesuppe mit Vollkornbrötchen", "Zuppa di verdure con panino integrale", "شوربة خضار مع خبز حبوب كاملة", "Zuppa 'e verdure cu panino integrale"),
        "Salmon salad with mixed greens" to localized("Salmon salad with mixed greens", "Ensalada de salmón con hojas verdes mixtas", "Salade de saumon aux jeunes pousses", "Lachssalat mit gemischtem Blattsalat", "Insalata di salmone con misticanza", "سلطة سلمون مع خضار ورقية مشكلة", "Nzalandella 'e salmone cu nzalandella mmescata"),
        "Brown rice burrito bowl" to localized("Brown rice burrito bowl", "Bol burrito de arroz integral", "Bol burrito au riz complet", "Naturreis-Burrito-Bowl", "Burrito bowl con riso integrale", "وعاء بوريتو بالأرز البني", "Burrito bowl cu riso integrale"),
        "Greek salad with chickpeas and feta" to localized("Greek salad with chickpeas and feta", "Ensalada griega con garbanzos y feta", "Salade grecque aux pois chiches et feta", "Griechischer Salat mit Kichererbsen und Feta", "Insalata greca con ceci e feta", "سلطة يونانية مع حمص وفيتا", "Nzalandella greca cu cicere e feta"),
        "Turkey sandwich with side salad" to localized("Turkey sandwich with side salad", "Sándwich de pavo con ensalada de acompañamiento", "Sandwich à la dinde avec salade d'accompagnement", "Putensandwich mit Beilagensalat", "Sandwich di tacchino con insalata di contorno", "ساندويش الديك الرومي مع سلطة جانبية", "Panino 'e tacchino cu nzalandella 'e contorno"),
        "Stir-fried vegetables with tofu" to localized("Stir-fried vegetables with tofu", "Verduras salteadas con tofu", "Légumes sautés au tofu", "Sautiertes Gemüse mit Tofu", "Verdure saltate in padella con tofu", "خضار مقلية مع توفو", "Verdure fritte cu tofu"),
        // Balanced dinners
        "Roasted chicken with vegetables" to localized("Roasted chicken with vegetables", "Pollo asado con verduras", "Poulet rôti avec légumes", "Gebratenes Hähnchen mit Gemüse", "Pollo arrosto con verdure", "دجاج مشوي مع خضار", "Pullastro arrustuto cu verdure"),
        "Baked fish with quinoa and greens" to localized("Baked fish with quinoa and greens", "Pescado al horno con quinoa y verduras", "Poisson au four avec quinoa et légumes verts", "Gebackener Fisch mit Quinoa und Gemüse", "Pesce al forno con quinoa e verdure", "سمك بالفرن مع كينوا وخضروات", "Pesce a furno cu quinoa e verdure"),
        "Vegetable and bean chili" to localized("Vegetable and bean chili", "Chili de verduras y judías", "Chili aux légumes et haricots", "Gemüse-Bohnen-Chili", "Chili di verdure e fagioli", "تشيلي الخضار والفاصوليا", "Chili 'e verdure e fasule"),
        "Pork tenderloin with roasted carrots" to localized("Pork tenderloin with roasted carrots", "Lomo de cerdo con zanahorias asadas", "Filet de porc avec carottes rôties", "Schweinefilet mit gerösteten Karotten", "Filetto di maiale con carote arrosto", "فيليه الخنزير مع جزر مشوي", "Filetto 'e puorco cu carote arrustute"),
        "Shrimp tacos with cabbage slaw" to localized("Shrimp tacos with cabbage slaw", "Tacos de gambas con ensalada de col", "Tacos aux crevettes avec salade de chou", "Garnelen-Tacos mit Krautsalat", "Tacos di gamberi con insalata di cavolo", "تاكو روبيان مع سلطة الملفوف", "Tacos 'e gammare cu nzalandella 'e cavolo"),
        "Mushroom and spinach pasta" to localized("Mushroom and spinach pasta", "Pasta con champiñones y espinacas", "Pâtes aux champignons et épinards", "Pilz-Spinat-Pasta", "Pasta con funghi e spinaci", "معكرونة بالفطر والسبانخ", "Pasta cu funge e spinace"),
        "Hearty lentil and vegetable stew" to localized("Hearty lentil and vegetable stew", "Guiso abundante de lentejas y verduras", "Ragoût généreux de lentilles et légumes", "Herzhafter Linsen-Gemüse-Eintopf", "Stufato abbondante di lenticchie e verdure", "يخنة عدس وخضار دسمة", "Stufato abbondante 'e lenticchie e verdure"),
    )

    fun labelFor(englishLabel: String, languageCode: String): String =
        labels[englishLabel]?.get(languageKey(languageCode)) ?: englishLabel

    /**
     * Reverse map: any localized dish string (any language, lowercased) → its English canonical.
     * Built lazily once from [mealDishes]. Falls back to the input when not found so user-typed
     * dish names are passed through unchanged.
     */
    private val mealDishReverse: Map<String, String> by lazy {
        buildMap {
            for ((english, translations) in mealDishes) {
                for ((_, localized) in translations) {
                    putIfAbsent(localized.lowercase(), english)
                }
            }
        }
    }

    fun mealDishFor(englishDish: String, languageCode: String): String =
        mealDishes[englishDish]?.get(languageKey(languageCode)) ?: englishDish

    /**
     * Given a dish name in any supported language, returns the English canonical used for
     * ingredient matching. Falls back to the original string for user-typed dish names that
     * are not in the built-in catalog.
     */
    fun mealDishToEnglish(localizedDish: String): String =
        mealDishReverse[localizedDish.trim().lowercase()] ?: localizedDish

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
