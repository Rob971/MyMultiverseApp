import {
  buildAndroidIntentLink,
  buildBrandLogoImgHtml,
  buildInvalidInvitePageHtml,
  buildInviteDeepLink,
  buildInviteEmailFallbackHtml,
  buildInviteEmailHtml,
  buildInviteEmailSubject,
  buildInviteEmailText,
  buildInviteOpenPageHtml,
  buildInviteWebLink,
  BRAND_LOGO_URL,
  escapeHtml,
  resolveInviteOpenLinkBase,
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
    "app.mymultiverse.ammo://invite?token=abc%20123",
    "deep link",
  );
});

Deno.test("buildInviteWebLink uses HTTPS open base", () => {
  assertEquals(
    buildInviteWebLink("abc 123", "https://example.supabase.co/functions/v1/invite-open"),
    "https://example.supabase.co/functions/v1/invite-open?token=abc%20123",
    "web link",
  );
});

Deno.test("resolveInviteOpenLinkBase prefers configured base", () => {
  assertEquals(
    resolveInviteOpenLinkBase("https://mymultiverse.app/invite/", "https://x.supabase.co"),
    "https://mymultiverse.app/invite",
    "configured base",
  );
});

Deno.test("resolveInviteOpenLinkBase falls back to Supabase function URL", () => {
  assertEquals(
    resolveInviteOpenLinkBase("", "https://x.supabase.co/"),
    "https://x.supabase.co/functions/v1/invite-open",
    "supabase fallback",
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
    inviteWebLink: "https://example.com/invite-open?token=t",
    inviteDeepLink: "app.mymultiverse.ammo://invite?token=t",
  });
  assertEquals(subject, "Marco invited you to Rossi family", "subject");
});

Deno.test("buildInviteEmailHtml uses HTTPS web link for CTA", () => {
  const html = buildInviteEmailHtml({
    inviterName: "Marco",
    householdName: "Rossi family",
    inviteeEmail: "maria@example.com",
    inviteWebLink: "https://example.com/invite-open?token=secret-token",
    inviteDeepLink: "app.mymultiverse.ammo://invite?token=secret-token",
  });
  assertIncludes(html, "Accept invitation", "cta label");
  assertIncludes(html, 'href="https://example.com/invite-open?token=secret-token"', "https cta href");
  assertIncludes(html, "https://example.com/invite-open?token=secret-token", "https fallback link");
  assertIncludes(html, "maria@example.com", "invitee email");
  assertIncludes(html, BRAND_LOGO_URL, "brand logo url");
  assertIncludes(html, 'alt="Ammò logo"', "brand logo alt");
});

Deno.test("buildInviteEmailText includes HTTPS web link", () => {
  const text = buildInviteEmailText({
    inviterName: "Marco",
    householdName: "Rossi family",
    inviteeEmail: "maria@example.com",
    inviteWebLink: "https://example.com/invite-open?token=secret-token",
    inviteDeepLink: "app.mymultiverse.ammo://invite?token=secret-token",
  });
  assertIncludes(text, "Accept invitation: https://example.com/invite-open?token=secret-token", "plain text link");
});

Deno.test("buildInviteEmailFallbackHtml includes brand logo", () => {
  const html = buildInviteEmailFallbackHtml("Marco", "Rossi family", "maria@example.com");
  assertIncludes(html, BRAND_LOGO_URL, "brand logo url");
  assertIncludes(html, "Open the Ammò app", "fallback instructions");
});

Deno.test("buildInvalidInvitePageHtml includes brand logo", () => {
  const html = buildInvalidInvitePageHtml();
  assertIncludes(html, BRAND_LOGO_URL, "brand logo url");
  assertIncludes(html, "Invitation link invalid", "invalid title");
});

Deno.test("buildInviteOpenPageHtml redirects to custom scheme", () => {
  const html = buildInviteOpenPageHtml("app.mymultiverse.ammo://invite?token=secret-token");
  assertIncludes(html, "app.mymultiverse.ammo://invite?token=secret-token", "deep link");
  assertIncludes(html, "Open Ammò", "open button");
  assertIncludes(html, BRAND_LOGO_URL, "brand logo url");
});

Deno.test("buildAndroidIntentLink targets app package", () => {
  assertIncludes(
    buildAndroidIntentLink("app.mymultiverse.ammo://invite?token=abc"),
    "package=app.mymultiverse.ammo",
    "android package",
  );
});
