export function buildGroceryItemAddedPushData(
  payload: Record<string, unknown>,
): Record<string, string> {
  return {
    type: "grocery_item_added",
    household_id: String(payload.household_id ?? ""),
    week_key: String(payload.week_key ?? ""),
    actor_name: String(payload.actor_name ?? ""),
    item_label: String(payload.item_label ?? ""),
    added_count: String(payload.added_count ?? "1"),
  };
}
