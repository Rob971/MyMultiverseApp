import { buildInvitePushData, inviteTokenFromPayload } from "./invite-token.ts";

function assertEquals(actual: unknown, expected: unknown, message: string) {
  if (actual !== expected) {
    throw new Error(`${message}: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}

function assertDeepEquals(actual: unknown, expected: unknown, message: string) {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    throw new Error(`${message}: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}

Deno.test("inviteTokenFromPayload prefers invite_token field", () => {
  assertEquals(
    inviteTokenFromPayload({ invite_token: "  token-abc  ", invite_id: "id" }),
    "token-abc",
    "payload token",
  );
});

Deno.test("inviteTokenFromPayload returns null when missing", () => {
  assertEquals(inviteTokenFromPayload({ invite_id: "id" }), null, "missing token");
  assertEquals(inviteTokenFromPayload({ invite_token: "   " }), null, "blank token");
});

Deno.test("buildInvitePushData includes invite_token when present", () => {
  assertDeepEquals(
    buildInvitePushData(
      { invite_id: "invite-1", household_id: "house-1" },
      "token-abc",
    ),
    {
      type: "household_invite",
      invite_id: "invite-1",
      household_id: "house-1",
      invite_token: "token-abc",
    },
    "push data with token",
  );
});

Deno.test("buildInvitePushData omits invite_token when absent", () => {
  assertDeepEquals(
    buildInvitePushData(
      { invite_id: "invite-1", household_id: "house-1" },
      null,
    ),
    {
      type: "household_invite",
      invite_id: "invite-1",
      household_id: "house-1",
    },
    "push data without token",
  );
});
