export function buildMealPlanNudgePushData(
  payload: Record<string, unknown>,
): Record<string, string> {
  return {
    type: "meal_plan_nudge",
    household_id: String(payload.household_id ?? ""),
    week_key: String(payload.week_key ?? ""),
    nudger_name: String(payload.nudger_name ?? ""),
  };
}
