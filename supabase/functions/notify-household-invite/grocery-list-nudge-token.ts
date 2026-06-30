export function buildGroceryListNudgePushData(
  payload: Record<string, unknown>,
): Record<string, string> {
  return {
    type: "grocery_list_nudge",
    household_id: String(payload.household_id ?? ""),
    week_key: String(payload.week_key ?? ""),
    nudger_name: String(payload.nudger_name ?? ""),
  };
}
