import {
  groceryItemAddedPushText,
  groceryListNudgePushText,
  mealPlanItemAddedPushText,
  normalizePushLocale,
} from "./notification-i18n.ts";
import { assertEquals } from "https://deno.land/std@0.224.0/assert/mod.ts";

Deno.test("normalizePushLocale maps supported and fallback locales", () => {
  assertEquals(normalizePushLocale("fr"), "fr");
  assertEquals(normalizePushLocale("ar-rSA"), "ar-rSA");
  assertEquals(normalizePushLocale("ar"), "ar");
  assertEquals(normalizePushLocale("xx"), "en");
});

Deno.test("groceryItemAddedPushText localizes single and multiple items", () => {
  const single = groceryItemAddedPushText("fr", "Alex", "Lait", 1);
  assertEquals(single.title, "Liste de courses mise à jour");
  assertEquals(single.body, "Alex a ajouté Lait à la liste de courses");

  const multiple = groceryItemAddedPushText("en", "Alex", "Milk", 3);
  assertEquals(multiple.body, "Alex added 3 items to the grocery list");
});

Deno.test("mealPlanItemAddedPushText localizes meal slot copy", () => {
  const single = mealPlanItemAddedPushText("it", "Alex", "Pasta", 1, 1, "dinner");
  assertEquals(single.title, "Piano pasti aggiornato");
  assertEquals(single.body, "Alex ha aggiunto Pasta per martedì (cena)");
});

Deno.test("groceryListNudgePushText uses localized templates", () => {
  const text = groceryListNudgePushText("de", "Alex", "Familie");
  assertEquals(text.title, "Alex geht einkaufen");
  assertEquals(text.body, "Füge Fehlendes zur Einkaufsliste in Familie hinzu");
});
