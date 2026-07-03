import { buildMealPlanNudgePushData } from "./meal-plan-nudge-token.ts";
import { assertEquals } from "https://deno.land/std@0.224.0/assert/mod.ts";

Deno.test("buildMealPlanNudgePushData maps payload fields", () => {
  const data = buildMealPlanNudgePushData({
    household_id: "household-1",
    week_key: "2026-06-30",
    nudger_name: "Alex",
  });

  assertEquals(data.type, "meal_plan_nudge");
  assertEquals(data.household_id, "household-1");
  assertEquals(data.week_key, "2026-06-30");
  assertEquals(data.nudger_name, "Alex");
});
