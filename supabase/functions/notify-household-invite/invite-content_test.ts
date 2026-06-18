import {
  buildInviteDeepLink,
  buildInviteEmailHtml,
  buildInviteEmailSubject,
  buildInviteEmailText,
  escapeHtml,
} from "./invite-content.ts";

function assertEquals(actual: unknown, expected: unknown, message: string) {
  if (actual !== expected) {
    throw new Error(`${message}: expected ${JSON.stringify(expected)}, got ${JSON.stringify(actual)}`);
  }
}

function assertIncludes(haystack: string, needle: string, message: string) {
  if (!haystack.includes(needle)) {
    throw new Error(`${message}: expected to include ${JSON.stringify(needle)}`);
  }
}

Deno.test("buildInviteDeepLink encodes token query param", () => {
  assertEquals(
    buildInviteDeepLink("abc 123"),
    "app.mymultiverse.kmp://invite?token=abc%20123",
    "deep link",
  );
});

Deno.test("escapeHtml escapes user-controlled copy", () => {
  assertEquals(
    escapeHtml(`Rossi <3 & "family"`),
    "Rossi &lt;3 &amp; &quot;family&quot;",
    "html escape",
  );
});

Deno.test("buildInviteEmailSubject uses inviter and household", () => {
  const subject = buildInviteEmailSubject({
    inviterName: "Marco",
    householdName: "Rossi family",
    inviteeEmail: "maria@example.com",
    inviteDeepLink: "app.mymultiverse.kmp://invite?token=t",
  });
  assertEquals(subject, "Marco invited you to Rossi family", "subject");
});

Deno.test("buildInviteEmailHtml includes CTA and deep link", () => {
  const html = buildInviteEmailHtml({
    inviterName: "Marco",
    householdName: "Rossi family",
    inviteeEmail: "maria@example.com",
    inviteDeepLink: "app.mymultiverse.kmp://invite?token=secret-token",
  });
  assertIncludes(html, "Accept invitation", "cta label");
  assertIncludes(html, "app.mymultiverse.kmp://invite?token=secret-token", "deep link");
  assertIncludes(html, "maria@example.com", "invitee email");
});

Deno.test("buildInviteEmailText includes deep link", () => {
  const text = buildInviteEmailText({
    inviterName: "Marco",
    householdName: "Rossi family",
    inviteeEmail: "maria@example.com",
    inviteDeepLink: "app.mymultiverse.kmp://invite?token=secret-token",
  });
  assertIncludes(text, "Accept invitation: app.mymultiverse.kmp://invite?token=secret-token", "plain text link");
});
