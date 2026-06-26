export const INVITE_DEEP_LINK_SCHEME = "app.mymultiverse.kmp";
export const INVITE_DEEP_LINK_HOST = "invite";
export const ANDROID_APP_PACKAGE = "app.mymultiverse.kmp";
export const DEFAULT_INVITE_OPEN_PATH = "/functions/v1/invite-open";
export const APP_BRAND_NAME = "Ammò";
export const DEFAULT_INVITE_FROM_EMAIL = `Ammò <invites@mymultiverse.app>`;
/** Hosted on mymultiverse.app — keep in sync with website `public/brand/`. */
export const BRAND_LOGO_URL = "https://mymultiverse.app/brand/ammo-round-logo-256.png";

export function buildBrandLogoImgHtml(sizePx = 96): string {
  const url = escapeHtml(BRAND_LOGO_URL);
  const alt = escapeHtml(`${APP_BRAND_NAME} logo`);
  return `<img src="${url}" width="${sizePx}" height="${sizePx}" alt="${alt}" style="display:block;margin:0 auto 16px;border-radius:50%;" />`;
}

export type InviteEmailContent = {
  inviterName: string;
  householdName: string;
  inviteeEmail: string;
  /** HTTPS link for email CTAs (clickable in Gmail/Outlook). */
  inviteWebLink: string;
  /** Custom-scheme link used by the invite-open landing page and push payloads. */
  inviteDeepLink: string;
};

export function buildInviteDeepLink(inviteToken: string): string {
  const token = inviteToken.trim();
  return `${INVITE_DEEP_LINK_SCHEME}://${INVITE_DEEP_LINK_HOST}?token=${encodeURIComponent(token)}`;
}

export function resolveInviteOpenLinkBase(
  configuredBase?: string | null,
  supabaseUrl?: string | null,
): string {
  const configured = configuredBase?.trim();
  if (configured) return configured.replace(/\/$/, "");

  const projectUrl = supabaseUrl?.trim();
  if (projectUrl) {
    return `${projectUrl.replace(/\/$/, "")}${DEFAULT_INVITE_OPEN_PATH}`;
  }

  return "https://mymultiverse.app/invite";
}

export function buildInviteWebLink(inviteToken: string, openLinkBase: string): string {
  const token = inviteToken.trim();
  const base = openLinkBase.replace(/\/$/, "");
  return `${base}?token=${encodeURIComponent(token)}`;
}

export function buildAndroidIntentLink(inviteDeepLink: string): string {
  const withoutScheme = inviteDeepLink.replace(`${INVITE_DEEP_LINK_SCHEME}://`, "");
  return `intent://${withoutScheme}#Intent;scheme=${INVITE_DEEP_LINK_SCHEME};package=${ANDROID_APP_PACKAGE};end`;
}

export function buildInviteOpenPageHtml(inviteDeepLink: string): string {
  const deepLink = escapeHtml(inviteDeepLink);
  const androidIntent = escapeHtml(buildAndroidIntentLink(inviteDeepLink));

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Open ${APP_BRAND_NAME}</title>
  <meta http-equiv="refresh" content="0;url=${deepLink}" />
</head>
<body style="margin:0;padding:32px 16px;background:#f6f1ea;font-family:Helvetica,Arial,sans-serif;color:#2c241c;text-align:center;">
  <div style="max-width:420px;margin:0 auto;background:#ffffff;border-radius:16px;padding:32px 24px;">
    ${buildBrandLogoImgHtml(96)}
    <p style="margin:0 0 8px;font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#c45c26;">${APP_BRAND_NAME}</p>
    <h1 style="margin:0 0 12px;font-size:24px;line-height:1.25;">Open your invitation</h1>
    <p style="margin:0 0 24px;font-size:16px;line-height:1.5;color:#5c5348;">
      Continue in the ${APP_BRAND_NAME} app to review and accept your household invite.
    </p>
    <p style="margin:0 0 16px;">
      <a href="${deepLink}" style="display:inline-block;background:#1f6f78;color:#ffffff;text-decoration:none;font-size:16px;font-weight:600;padding:14px 28px;border-radius:999px;">
        Open ${APP_BRAND_NAME}
      </a>
    </p>
    <p style="margin:0;font-size:14px;line-height:1.5;color:#8a8178;">
      On Android, <a href="${androidIntent}" style="color:#1f6f78;">tap here if the app did not open</a>.
    </p>
  </div>
  <script>
    (function () {
      var deepLink = ${JSON.stringify(inviteDeepLink)};
      try { window.location.replace(deepLink); } catch (_) {}
    })();
  </script>
</body>
</html>`;
}

export function escapeHtml(value: string): string {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

export function buildInviteEmailSubject(content: InviteEmailContent): string {
  return `${content.inviterName} invited you to ${content.householdName}`;
}

export function buildInviteEmailHtml(content: InviteEmailContent): string {
  const inviter = escapeHtml(content.inviterName);
  const household = escapeHtml(content.householdName);
  const email = escapeHtml(content.inviteeEmail);
  const link = escapeHtml(content.inviteWebLink);

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Join ${household} on ${APP_BRAND_NAME}</title>
</head>
<body style="margin:0;padding:0;background:#f6f1ea;font-family:Helvetica,Arial,sans-serif;color:#2c241c;">
  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f6f1ea;padding:32px 16px;">
    <tr>
      <td align="center">
        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:520px;background:#ffffff;border-radius:16px;padding:32px 28px;">
          <tr>
            <td>
              ${buildBrandLogoImgHtml(96)}
              <p style="margin:0 0 8px;font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#c45c26;">${APP_BRAND_NAME}</p>
              <h1 style="margin:0 0 12px;font-size:28px;line-height:1.2;color:#2c241c;">Join ${household}</h1>
              <p style="margin:0 0 20px;font-size:16px;line-height:1.5;color:#5c5348;">
                <strong>${inviter}</strong> invited you to share groceries, meal plans, and family logistics together.
              </p>
              <p style="margin:0 0 24px;text-align:center;">
                <a href="${link}" style="display:inline-block;background:#1f6f78;color:#ffffff;text-decoration:none;font-size:16px;font-weight:600;padding:14px 28px;border-radius:999px;">
                  Accept invitation
                </a>
              </p>
              <p style="margin:0 0 12px;font-size:14px;line-height:1.5;color:#5c5348;">
                Sign in with <strong>${email}</strong> to accept this invitation.
              </p>
              <p style="margin:0;font-size:13px;line-height:1.5;color:#8a8178;">
                If the button does not work, copy this link into your mobile browser:<br />
                <a href="${link}" style="color:#1f6f78;word-break:break-all;text-decoration:underline;">${link}</a>
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>`;
}

export function buildInviteEmailText(content: InviteEmailContent): string {
  return [
    `Join ${content.householdName} on ${APP_BRAND_NAME}`,
    "",
    `${content.inviterName} invited you to share groceries, meal plans, and family logistics together.`,
    "",
    `Accept invitation: ${content.inviteWebLink}`,
    "",
    `Sign in with ${content.inviteeEmail} to accept this invitation.`,
  ].join("\n");
}
