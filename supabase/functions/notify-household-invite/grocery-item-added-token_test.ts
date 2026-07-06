import { buildGroceryItemAddedPushData } from "./grocery-item-added-token.ts";
import { assertEquals } from "https://deno.land/std@0.224.0/assert/mod.ts";

Deno.test("buildGroceryItemAddedPushData maps payload fields", () => {
  const data = buildGroceryItemAddedPushData({
    household_id: "household-1",
    week_key: "2026-06-30",
    actor_name: "Alex",
    item_label: "Milk",
    added_count: 2,
  });

  assertEquals(data.type, "grocery_item_added");
  assertEquals(data.household_id, "household-1");
  assertEquals(data.added_count, "2");
});
