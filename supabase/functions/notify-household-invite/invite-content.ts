export const INVITE_DEEP_LINK_SCHEME = "app.mymultiverse.kmp";
export const INVITE_DEEP_LINK_HOST = "invite";

export type InviteEmailContent = {
  inviterName: string;
  householdName: string;
  inviteeEmail: string;
  inviteDeepLink: string;
};

export function buildInviteDeepLink(inviteToken: string): string {
  const token = inviteToken.trim();
  return `${INVITE_DEEP_LINK_SCHEME}://${INVITE_DEEP_LINK_HOST}?token=${encodeURIComponent(token)}`;
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
  const link = escapeHtml(content.inviteDeepLink);

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Join ${household} on MyMultiverse</title>
</head>
<body style="margin:0;padding:0;background:#f6f1ea;font-family:Helvetica,Arial,sans-serif;color:#2c241c;">
  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#f6f1ea;padding:32px 16px;">
    <tr>
      <td align="center">
        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:520px;background:#ffffff;border-radius:16px;padding:32px 28px;">
          <tr>
            <td>
              <p style="margin:0 0 8px;font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#c45c26;">MyMultiverse</p>
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
                If the button does not open the app, copy this link into your browser:<br />
                <a href="${link}" style="color:#1f6f78;word-break:break-all;">${link}</a>
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
    `Join ${content.householdName} on MyMultiverse`,
    "",
    `${content.inviterName} invited you to share groceries, meal plans, and family logistics together.`,
    "",
    `Accept invitation: ${content.inviteDeepLink}`,
    "",
    `Sign in with ${content.inviteeEmail} to accept this invitation.`,
  ].join("\n");
}
