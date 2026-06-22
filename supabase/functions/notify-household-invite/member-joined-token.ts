export function buildMemberJoinedPushData(
  payload: Record<string, unknown>,
): Record<string, string> {
  return {
    type: "household_member_joined",
    household_id: String(payload.household_id ?? ""),
    member_name: String(payload.member_name ?? ""),
  };
}
