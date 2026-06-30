import { buildGroceryListNudgePushData } from "./grocery-list-nudge-token.ts";
import { assertEquals } from "https://deno.land/std@0.224.0/assert/mod.ts";

Deno.test("buildGroceryListNudgePushData maps payload fields", () => {
  const data = buildGroceryListNudgePushData({
    household_id: "household-1",
    week_key: "2026-06-30",
    nudger_name: "Alex",
  });

  assertEquals(data.type, "grocery_list_nudge");
  assertEquals(data.household_id, "household-1");
  assertEquals(data.week_key, "2026-06-30");
  assertEquals(data.nudger_name, "Alex");
});
