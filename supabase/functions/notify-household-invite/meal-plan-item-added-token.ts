export function buildMealPlanItemAddedPushData(
  payload: Record<string, unknown>,
): Record<string, string> {
  return {
    type: "meal_plan_item_added",
    household_id: String(payload.household_id ?? ""),
    week_key: String(payload.week_key ?? ""),
    actor_name: String(payload.actor_name ?? ""),
    item_label: String(payload.item_label ?? ""),
    added_count: String(payload.added_count ?? "1"),
    day_index: String(payload.day_index ?? "0"),
    meal_slot: String(payload.meal_slot ?? ""),
  };
}
