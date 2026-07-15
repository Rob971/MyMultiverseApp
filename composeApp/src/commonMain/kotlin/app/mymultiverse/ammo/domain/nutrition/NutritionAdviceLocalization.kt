package app.mymultiverse.ammo.domain.nutrition

/**
 * Localized nutrition-advice templates for the offline [NutritionAdviceBuilder] fallback.
 * Gemini prompts use [app.mymultiverse.ammo.data.service.GeminiResponseParser.languagePromptDirective].
 */
internal object NutritionAdviceLocalization {

    private const val EN = "en"
    private const val ES = "es"
    private const val FR = "fr"
    private const val DE = "de"
    private const val IT = "it"
    private const val AR = "ar"
    private const val NAP = "nap"

    enum class Category {
        Protein,
        Vegetarian,
        Family,
        Budget,
        Allergy,
        Weight,
        Generic,
    }

    private val weightTerms = listOf(
        "weight", "calorie", "calories", "caloria", "calorie", "peso", "poids", "gewicht",
        "وزن", "سعرات", "dimagr", "perdere peso",
    )

    fun categoryFor(question: String): Category {
        val keywords = question.lowercase()
        return when {
            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Protein,
            ) -> Category.Protein
            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Vegetarian,
            ) -> Category.Vegetarian
            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Family,
            ) -> Category.Family
            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Budget,
            ) -> Category.Budget
            NutritionFoodSuggestionLocalization.hasIntent(
                keywords,
                NutritionFoodSuggestionLocalization.Intent.Allergy,
            ) -> Category.Allergy
            weightTerms.any { keywords.contains(it) } -> Category.Weight
            else -> Category.Generic
        }
    }

    fun advice(category: Category, languageCode: String, question: String = ""): String {
        val lang = NutritionFoodSuggestionLocalization.normalizeLanguageCode(languageCode)
        val template = templates.getValue(category)[lang] ?: templates.getValue(category)[EN].orEmpty()
        return if (category == Category.Generic) {
            template.replace("{question}", question)
        } else {
            template
        }
    }

    private val templates: Map<Category, Map<String, String>> = mapOf(
        Category.Protein to localized(
            en = "Aim for protein at lunch and dinner: legumes, eggs, fish, yogurt, or lean meat. " +
                "Pair each meal with vegetables and whole grains so energy stays steady through the day.",
            es = "Apunta a proteína en comida y cena: legumbres, huevos, pescado, yogur o carne magra. " +
                "Acompaña cada plato con verduras y cereales integrales para mantener la energía estable.",
            fr = "Visez des protéines au déjeuner et au dîner : légumineuses, œufs, poisson, yaourt ou viande maigre. " +
                "Associez chaque repas à des légumes et des céréales complètes pour une énergie stable.",
            de = "Setze auf Protein bei Mittag- und Abendessen: Hülsenfrüchte, Eier, Fisch, Joghurt oder mageres Fleisch. " +
                "Kombiniere jede Mahlzeit mit Gemüse und Vollkorn für gleichmäßige Energie.",
            it = "Punta sulle proteine a pranzo e cena: legumi, uova, pesce, yogurt o carne magra. " +
                "Abbina ogni pasto a verdure e cereali integrali per energia costante durante la giornata.",
            ar = "اهدف إلى البروتين في الغداء والعشاء: البقوليات والبيض والسمك والزبادي أو اللحم قليل الدهن. " +
                "أضف الخضار والحبوب الكاملة لكل وجبة لطاقة مستقرة طوال اليوم.",
            nap = "Punta ncopp' 'e proteine a pranzo e a cena: legume, uova, pesce, yogurt o carne magra. " +
                "Abbina ogne pasto cu verdurielle e cereale integrale pe tené 'a energia ferma.",
        ),
        Category.Vegetarian to localized(
            en = "Build plates around beans, lentils, tofu, nuts, and whole grains. " +
                "Add iron-rich foods with vitamin C (tomatoes, citrus) and consider B12 if the household is fully plant-based.",
            es = "Construye platos con legumbres, lentejas, tofu, frutos secos y cereales integrales. " +
                "Añade alimentos ricos en hierro con vitamina C (tomate, cítricos) y valora B12 si la familia es vegana.",
            fr = "Composez les assiettes autour de légumineuses, lentilles, tofu, noix et céréales complètes. " +
                "Ajoutez du fer avec de la vitamine C (tomates, agrumes) et pensez à la B12 si le foyer est végétalien.",
            de = "Baue Mahlzeiten um Bohnen, Linsen, Tofu, Nüsse und Vollkorn auf. " +
                "Ergänze eisenreiche Lebensmittel mit Vitamin C (Tomaten, Zitrus) und denke an B12 bei rein pflanzlicher Ernährung.",
            it = "Costruisci i piatti con legumi, lenticchie, tofu, frutta secca e cereali integrali. " +
                "Aggiungi ferro con vitamina C (pomodori, agrumi) e valuta la B12 se la famiglia è vegana.",
            ar = "اعتمد على البقوليات والعدس والتوفو والمكسرات والحبوب الكاملة. " +
                "أضف أطعمة غنية بالحديد مع فيتامين ج (طماطم، حمضيات) وفكّر في B12 إذا كان النظام نباتياً بالكامل.",
            nap = "Fà 'e portate cu fasule, lenticchie, tofu, frutta secca e cereale integrale. " +
                "Miette ferro cu vitamina C (pummarola, agrume) e pensa a B12 si 'a famiglia è vegana.",
        ),
        Category.Family to localized(
            en = "Keep family meals simple: one familiar base, one new vegetable, and predictable portions. " +
                "Involve children in choosing one dinner per week to improve buy-in.",
            es = "Mantén las comidas familiares simples: una base conocida, una verdura nueva y porciones predecibles. " +
                "Involucra a los niños en elegir una cena a la semana para que participen más.",
            fr = "Gardez les repas en famille simples : une base familière, un nouveau légume et des portions régulières. " +
                "Impliquez les enfants dans le choix d'un dîner par semaine pour les motiver.",
            de = "Halte Familienmahlzeiten einfach: eine vertraute Basis, ein neues Gemüse und planbare Portionen. " +
                "Lass Kinder ein Abendessen pro Woche mitwählen — das steigert die Beteiligung.",
            it = "Tieni i pasti in famiglia semplici: una base familiare, una verdura nuova e porzioni prevedibili. " +
                "Coinvolgi i bambini nella scelta di una cena a settimana per aumentare l'aderenza.",
            ar = "اجعل وجبات العائلة بسيطة: قاعدة مألوفة وخضار جديد وحصص ثابتة. " +
                "أشرك الأطفال في اختيار عشاء واحد أسبوعياً لزيادة التفاعل.",
            nap = "Tien' 'e pastate 'e famiglia semplici: na base ca conoscono, na verduriela nova e porzioni chiare. " +
                "Fa' scegliere ai criature na cena a settimana pe fàlli partecipà.",
        ),
        Category.Budget to localized(
            en = "Plan two batch-cook meals per week and reuse leftovers for lunch. " +
                "Base the grocery list on seasonal produce and pantry staples before adding extras.",
            es = "Planifica dos comidas en lote por semana y reutiliza sobras para el almuerzo. " +
                "Basa la lista de la compra en producto de temporada y despensa antes de extras.",
            fr = "Prévoyez deux repas en batch par semaine et réutilisez les restes pour le déjeuner. " +
                "Basez les courses sur les produits de saison et le garde-manger avant les extras.",
            de = "Plane zwei Vorkoch-Mahlzeiten pro Woche und nutze Reste fürs Mittagessen. " +
                "Stütze die Einkaufsliste auf Saisonware und Vorrat, bevor du Extras hinzufügst.",
            it = "Pianifica due pasti da preparare in batch a settimana e riusa gli avanzi per il pranzo. " +
                "Basa la spesa su prodotti di stagione e dispensa prima degli extra.",
            ar = "خطط لوجبتين للطبخ المسبق أسبوعياً وأعد استخدام البقايا للغداء. " +
                "ابنِ قائمة التسوق على الموسم والمخزن قبل الإضافات.",
            nap = "Pianifica doje pastate 'n batch a settimana e riusa 'e reste pe' 'o pranzo. " +
                "Fà 'a spesa cu robba 'e stagione e dispensa primma 'e extra.",
        ),
        Category.Allergy to localized(
            en = "Treat allergens as hard constraints: list them at the top of your meal plan and grocery list. " +
                "When unsure about packaged foods, check labels every time—recipes can change.",
            es = "Trata los alérgenos como restricciones estrictas: anótalos arriba del plan y de la lista de la compra. " +
                "Si dudas de un producto envasado, revisa la etiqueta cada vez—las recetas cambian.",
            fr = "Traitez les allergènes comme des contraintes strictes : listez-les en tête du plan et des courses. " +
                "En cas de doute sur un produit emballé, relisez l'étiquette à chaque fois.",
            de = "Behandle Allergene als harte Grenzen: notiere sie oben im Essensplan und auf der Einkaufsliste. " +
                "Bei Fertigprodukten jedes Mal das Etikett prüfen—Rezepte können sich ändern.",
            it = "Tratta gli allergeni come vincoli rigidi: elencali in cima al piano pasti e alla spesa. " +
                "Se dubiti su un prodotto confezionato, controlla l'etichetta ogni volta—le ricette cambiano.",
            ar = "اعتبر مسببات الحساسية قيوداً صارمة: اذكرها أعلى خطة الوجبات وقائمة التسوق. " +
                "عند الشك في منتج معبأ، راجع الملصق في كل مرة—قد تتغير الوصفات.",
            nap = "Tratta 'e allergeni comme regole dure: mettille in cima ô piano e â lista 'e spesa. " +
                "Si dubite 'e nu produtto, guarda 'a etichetta ogne vota—'e ricette cagnano.",
        ),
        Category.Weight to localized(
            en = "Focus on balanced plates (half vegetables, quarter protein, quarter starch) and consistent meal timing. " +
                "Small sustainable changes beat strict short-term diets for household health.",
            es = "Prioriza platos equilibrados (mitad verduras, cuarto proteína, cuarto carbohidratos) y horarios regulares. " +
                "Pequeños cambios sostenibles superan dietas estrictas a corto plazo.",
            fr = "Privilégiez des assiettes équilibrées (moitié légumes, quart protéines, quart féculents) et des horaires réguliers. " +
                "De petits changements durables valent mieux que des régimes stricts.",
            de = "Setze auf ausgewogene Teller (halb Gemüse, Viertel Protein, Viertel Stärke) und regelmäßige Essenszeiten. " +
                "Kleine nachhaltige Schritte schlagen kurze Crash-Diäten.",
            it = "Punta su piatti equilibrati (metà verdure, un quarto proteine, un quarto carboidrati) e orari costanti. " +
                "Piccoli cambiamenti sostenibili battono diete rigide a breve termine.",
            ar = "ركّز على أطباق متوازنة (نصف خضار، ربع بروتين، ربع نشويات) ومواعيد ثابتة للوجبات. " +
                "التغييرات الصغيرة المستدامة أفضل من الحميات القاسية قصيرة المدى.",
            nap = "Punta ncopp' a portate equilibrate (mietà verdurielle, quarto proteine, quarto carboidrati) e orari fissi. " +
                "Picciule cagnamiente durature fan' megghio 'e diete rigide.",
        ),
        Category.Generic to localized(
            en = "For your household, anchor the week with two easy dinners, one batch lunch, and a grocery list tied to those meals. " +
                "Keep questions specific (age, allergies, time, budget) for sharper guidance. " +
                "You asked: \"{question}\"",
            es = "Para tu hogar, ancla la semana con dos cenas fáciles, un almuerzo en lote y una lista de la compra ligada a esas comidas. " +
                "Sé específico (edad, alergias, tiempo, presupuesto) para mejores consejos. " +
                "Preguntaste: \"{question}\"",
            fr = "Pour votre foyer, structurez la semaine avec deux dîners faciles, un déjeuner en batch et des courses liées à ces repas. " +
                "Soyez précis (âge, allergies, temps, budget) pour de meilleurs conseils. " +
                "Vous avez demandé : « {question} »",
            de = "Verankere die Woche mit zwei einfachen Abendessen, einem Vorkoch-Mittagessen und einer passenden Einkaufsliste. " +
                "Sei konkret (Alter, Allergien, Zeit, Budget) für bessere Tipps. " +
                "Deine Frage: \"{question}\"",
            it = "Per la tua famiglia, organizza la settimana con due cene facili, un pranzo in batch e una spesa collegata a quei pasti. " +
                "Sii specifico (età, allergie, tempo, budget) per consigli più utili. " +
                "Hai chiesto: \"{question}\"",
            ar = "لأسرتك، ثبّت الأسبوع بعشاءين سهلين وغداء للطبخ المسبق وقائمة تسوق مرتبطة بتلك الوجبات. " +
                "كن محدداً (العمر، الحساسية، الوقت، الميزانية) لنصائح أدق. " +
                "سؤالك: \"{question}\"",
            nap = "Pe' 'a famiglia, organizza 'a settimana cu doje cene facile, nu pranzo 'n batch e spesa ligata a sti pastate. " +
                "Sii specifico (età, allergie, tiempo, budget) pe' conseglie cchiù utili. " +
                "T'ê dumandato: \"{question}\"",
        ),
    )

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
