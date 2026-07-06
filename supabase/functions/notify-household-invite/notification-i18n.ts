export type PushLocale =
  | "en"
  | "fr"
  | "es"
  | "de"
  | "it"
  | "ar"
  | "ar-rSA"
  | "nap";

const SUPPORTED_LOCALES: PushLocale[] = [
  "en",
  "fr",
  "es",
  "de",
  "it",
  "ar",
  "ar-rSA",
  "nap",
];

export function normalizePushLocale(raw: string | null | undefined): PushLocale {
  const trimmed = String(raw ?? "").trim();
  if (SUPPORTED_LOCALES.includes(trimmed as PushLocale)) {
    return trimmed as PushLocale;
  }
  if (trimmed === "ar-SA" || trimmed.startsWith("ar")) {
    return trimmed === "ar" ? "ar" : "ar-rSA";
  }
  return "en";
}

type TemplateSet = {
  someone: string;
  memberJoinedTitle: string;
  memberJoinedBody: string;
  groceryNudgeTitle: string;
  groceryNudgeBody: string;
  mealPlanNudgeTitle: string;
  mealPlanNudgeBody: string;
  groceryItemAddedTitle: string;
  groceryItemAddedBodySingle: string;
  groceryItemAddedBodyMultiple: string;
  mealPlanItemAddedTitle: string;
  mealPlanItemAddedBodySingle: string;
  mealPlanItemAddedBodyMultiple: string;
  mealSlotLunch: string;
  mealSlotDinner: string;
  days: [string, string, string, string, string, string, string];
};

const TEMPLATES: Record<PushLocale, TemplateSet> = {
  en: {
    someone: "Someone",
    memberJoinedTitle: "%s joined",
    memberJoinedBody: "Now collaborating in %s",
    groceryNudgeTitle: "%s is heading to the store",
    groceryNudgeBody: "Add anything missing to the grocery list in %s",
    mealPlanNudgeTitle: "%s is planning meals",
    mealPlanNudgeBody: "Add lunches and dinners to the meal plan in %s",
    groceryItemAddedTitle: "Grocery list updated",
    groceryItemAddedBodySingle: "%s added %s to the grocery list",
    groceryItemAddedBodyMultiple: "%s added %d items to the grocery list",
    mealPlanItemAddedTitle: "Meal plan updated",
    mealPlanItemAddedBodySingle: "%s added %s for %s %s",
    mealPlanItemAddedBodyMultiple: "%s added %d meals to the plan",
    mealSlotLunch: "lunch",
    mealSlotDinner: "dinner",
    days: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
  },
  fr: {
    someone: "Quelqu'un",
    memberJoinedTitle: "%s a rejoint le foyer",
    memberJoinedBody: "Collaboration dans %s",
    groceryNudgeTitle: "%s part faire les courses",
    groceryNudgeBody: "Ajoutez ce qui manque à la liste de courses dans %s",
    mealPlanNudgeTitle: "%s planifie les repas",
    mealPlanNudgeBody: "Ajoutez déjeuners et dîners au plan de repas dans %s",
    groceryItemAddedTitle: "Liste de courses mise à jour",
    groceryItemAddedBodySingle: "%s a ajouté %s à la liste de courses",
    groceryItemAddedBodyMultiple: "%s a ajouté %d articles à la liste de courses",
    mealPlanItemAddedTitle: "Plan de repas mis à jour",
    mealPlanItemAddedBodySingle: "%s a ajouté %s pour %s (%s)",
    mealPlanItemAddedBodyMultiple: "%s a ajouté %d repas au plan",
    mealSlotLunch: "déjeuner",
    mealSlotDinner: "dîner",
    days: ["lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"],
  },
  es: {
    someone: "Alguien",
    memberJoinedTitle: "%s se unió",
    memberJoinedBody: "Ahora colaboran en %s",
    groceryNudgeTitle: "%s va al supermercado",
    groceryNudgeBody: "Añade lo que falte a la lista de la compra en %s",
    mealPlanNudgeTitle: "%s está planificando comidas",
    mealPlanNudgeBody: "Añade almuerzos y cenas al plan de comidas en %s",
    groceryItemAddedTitle: "Lista de la compra actualizada",
    groceryItemAddedBodySingle: "%s añadió %s a la lista de la compra",
    groceryItemAddedBodyMultiple: "%s añadió %d artículos a la lista de la compra",
    mealPlanItemAddedTitle: "Plan de comidas actualizado",
    mealPlanItemAddedBodySingle: "%s añadió %s para el %s (%s)",
    mealPlanItemAddedBodyMultiple: "%s añadió %d comidas al plan",
    mealSlotLunch: "almuerzo",
    mealSlotDinner: "cena",
    days: ["lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"],
  },
  de: {
    someone: "Jemand",
    memberJoinedTitle: "%s ist beigetreten",
    memberJoinedBody: "Jetzt in %s zusammenarbeiten",
    groceryNudgeTitle: "%s geht einkaufen",
    groceryNudgeBody: "Füge Fehlendes zur Einkaufsliste in %s hinzu",
    mealPlanNudgeTitle: "%s plant Mahlzeiten",
    mealPlanNudgeBody: "Füge Mittag- und Abendessen zum Essensplan in %s hinzu",
    groceryItemAddedTitle: "Einkaufsliste aktualisiert",
    groceryItemAddedBodySingle: "%s hat %s zur Einkaufsliste hinzugefügt",
    groceryItemAddedBodyMultiple: "%s hat %d Artikel zur Einkaufsliste hinzugefügt",
    mealPlanItemAddedTitle: "Essensplan aktualisiert",
    mealPlanItemAddedBodySingle: "%s hat %s für %s (%s) hinzugefügt",
    mealPlanItemAddedBodyMultiple: "%s hat %d Mahlzeiten zum Plan hinzugefügt",
    mealSlotLunch: "Mittagessen",
    mealSlotDinner: "Abendessen",
    days: ["Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"],
  },
  it: {
    someone: "Qualcuno",
    memberJoinedTitle: "%s si è unito",
    memberJoinedBody: "Ora collaborate in %s",
    groceryNudgeTitle: "%s va al supermercato",
    groceryNudgeBody: "Aggiungi ciò che manca alla lista della spesa in %s",
    mealPlanNudgeTitle: "%s sta pianificando i pasti",
    mealPlanNudgeBody: "Aggiungi pranzi e cene al piano pasti in %s",
    groceryItemAddedTitle: "Lista della spesa aggiornata",
    groceryItemAddedBodySingle: "%s ha aggiunto %s alla lista della spesa",
    groceryItemAddedBodyMultiple: "%s ha aggiunto %d articoli alla lista della spesa",
    mealPlanItemAddedTitle: "Piano pasti aggiornato",
    mealPlanItemAddedBodySingle: "%s ha aggiunto %s per %s (%s)",
    mealPlanItemAddedBodyMultiple: "%s ha aggiunto %d pasti al piano",
    mealSlotLunch: "pranzo",
    mealSlotDinner: "cena",
    days: ["lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato", "domenica"],
  },
  ar: {
    someone: "شخص ما",
    memberJoinedTitle: "انضم %s",
    memberJoinedBody: "يتعاونون الآن في %s",
    groceryNudgeTitle: "%s متجه إلى المتجر",
    groceryNudgeBody: "أضف ما ينقص إلى قائمة التسوق في %s",
    mealPlanNudgeTitle: "%s يخطط للوجبات",
    mealPlanNudgeBody: "أضف الغداء والعشاء إلى خطة الوجبات في %s",
    groceryItemAddedTitle: "تم تحديث قائمة التسوق",
    groceryItemAddedBodySingle: "أضاف %s %s إلى قائمة التسوق",
    groceryItemAddedBodyMultiple: "أضاف %s %d عناصر إلى قائمة التسوق",
    mealPlanItemAddedTitle: "تم تحديث خطة الوجبات",
    mealPlanItemAddedBodySingle: "أضاف %s %s ليوم %s (%s)",
    mealPlanItemAddedBodyMultiple: "أضاف %s %d وجبات إلى الخطة",
    mealSlotLunch: "غداء",
    mealSlotDinner: "عشاء",
    days: ["الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد"],
  },
  "ar-rSA": {
    someone: "شخص ما",
    memberJoinedTitle: "انضم %s",
    memberJoinedBody: "يتعاونون الآن في %s",
    groceryNudgeTitle: "%s متجه إلى المتجر",
    groceryNudgeBody: "أضف ما ينقص إلى قائمة التسوق في %s",
    mealPlanNudgeTitle: "%s يخطط للوجبات",
    mealPlanNudgeBody: "أضف الغداء والعشاء إلى خطة الوجبات في %s",
    groceryItemAddedTitle: "تم تحديث قائمة التسوق",
    groceryItemAddedBodySingle: "أضاف %s %s إلى قائمة التسوق",
    groceryItemAddedBodyMultiple: "أضاف %s %d عناصر إلى قائمة التسوق",
    mealPlanItemAddedTitle: "تم تحديث خطة الوجبات",
    mealPlanItemAddedBodySingle: "أضاف %s %s ليوم %s (%s)",
    mealPlanItemAddedBodyMultiple: "أضاف %s %d وجبات إلى الخطة",
    mealSlotLunch: "غداء",
    mealSlotDinner: "عشاء",
    days: ["الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد"],
  },
  nap: {
    someone: "Qualcuno",
    memberJoinedTitle: "%s s'è unito",
    memberJoinedBody: "Mo' collaborano in %s",
    groceryNudgeTitle: "%s va 'o supermercato",
    groceryNudgeBody: "Miettete tutto quello ca manca 'n lista primma 'e fa' 'a spesa in %s",
    mealPlanNudgeTitle: "%s sta pianificanno 'e pasti",
    mealPlanNudgeBody: "Miettete pranzo e cena 'o piano pasti in %s",
    groceryItemAddedTitle: "Lista aggiornata",
    groceryItemAddedBodySingle: "%s ha mietto %s 'n lista",
    groceryItemAddedBodyMultiple: "%s ha mietto %d cose 'n lista",
    mealPlanItemAddedTitle: "Piano pasti aggiornato",
    mealPlanItemAddedBodySingle: "%s ha mietto %s pe' %s (%s)",
    mealPlanItemAddedBodyMultiple: "%s ha mietto %d pasti 'o piano",
    mealSlotLunch: "pranzo",
    mealSlotDinner: "cena",
    days: ["lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato", "domenica"],
  },
};

function format(template: string, ...values: (string | number)[]): string {
  let index = 0;
  return template.replace(/%[sd]/g, (match) => {
    const value = values[index++];
    if (match === "%d") {
      return String(value ?? "");
    }
    return String(value ?? "");
  });
}

function templatesFor(locale: PushLocale): TemplateSet {
  return TEMPLATES[locale] ?? TEMPLATES.en;
}

function actorName(raw: string, locale: PushLocale): string {
  const trimmed = raw.trim();
  return trimmed || templatesFor(locale).someone;
}

function householdName(raw: string): string {
  const trimmed = raw.trim();
  return trimmed || "your household";
}

function dayName(locale: PushLocale, dayIndex: number): string {
  const days = templatesFor(locale).days;
  return days[Math.max(0, Math.min(6, dayIndex))] ?? days[0];
}

function mealSlotLabel(locale: PushLocale, mealSlot: string): string {
  const t = templatesFor(locale);
  return mealSlot === "lunch" ? t.mealSlotLunch : t.mealSlotDinner;
}

export function memberJoinedPushText(
  locale: PushLocale,
  memberName: string,
  householdNameRaw: string,
): { title: string; body: string } {
  const t = templatesFor(locale);
  const name = actorName(memberName, locale);
  const household = householdName(householdNameRaw);
  return {
    title: format(t.memberJoinedTitle, name),
    body: format(t.memberJoinedBody, household),
  };
}

export function groceryListNudgePushText(
  locale: PushLocale,
  nudgerName: string,
  householdNameRaw: string,
): { title: string; body: string } {
  const t = templatesFor(locale);
  const name = actorName(nudgerName, locale);
  const household = householdName(householdNameRaw);
  return {
    title: format(t.groceryNudgeTitle, name),
    body: format(t.groceryNudgeBody, household),
  };
}

export function mealPlanNudgePushText(
  locale: PushLocale,
  nudgerName: string,
  householdNameRaw: string,
): { title: string; body: string } {
  const t = templatesFor(locale);
  const name = actorName(nudgerName, locale);
  const household = householdName(householdNameRaw);
  return {
    title: format(t.mealPlanNudgeTitle, name),
    body: format(t.mealPlanNudgeBody, household),
  };
}

export function groceryItemAddedPushText(
  locale: PushLocale,
  actorNameRaw: string,
  itemLabel: string,
  addedCount: number,
): { title: string; body: string } {
  const t = templatesFor(locale);
  const name = actorName(actorNameRaw, locale);
  const count = Math.max(1, addedCount);
  if (count === 1) {
    return {
      title: t.groceryItemAddedTitle,
      body: format(t.groceryItemAddedBodySingle, name, itemLabel.trim() || "—"),
    };
  }
  return {
    title: t.groceryItemAddedTitle,
    body: format(t.groceryItemAddedBodyMultiple, name, count),
  };
}

export function mealPlanItemAddedPushText(
  locale: PushLocale,
  actorNameRaw: string,
  itemLabel: string,
  addedCount: number,
  dayIndex: number,
  mealSlot: string,
): { title: string; body: string } {
  const t = templatesFor(locale);
  const name = actorName(actorNameRaw, locale);
  const count = Math.max(1, addedCount);
  if (count === 1) {
    return {
      title: t.mealPlanItemAddedTitle,
      body: format(
        t.mealPlanItemAddedBodySingle,
        name,
        itemLabel.trim() || "—",
        dayName(locale, dayIndex),
        mealSlotLabel(locale, mealSlot),
      ),
    };
  }
  return {
    title: t.mealPlanItemAddedTitle,
    body: format(t.mealPlanItemAddedBodyMultiple, name, count),
  };
}
