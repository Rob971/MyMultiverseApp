export function inviteTokenFromPayload(payload: Record<string, unknown>): string | null {
  const fromPayload = String(payload.invite_token ?? "").trim();
  return fromPayload || null;
}

export function buildInvitePushData(
  payload: Record<string, unknown>,
  inviteToken: string | null,
): Record<string, string> {
  const data: Record<string, string> = {
    type: "household_invite",
    invite_id: String(payload.invite_id ?? ""),
    household_id: String(payload.household_id ?? ""),
  };
  if (inviteToken) {
    data.invite_token = inviteToken;
  }
  return data;
}
