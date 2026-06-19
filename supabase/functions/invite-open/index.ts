import {
  buildInviteDeepLink,
  buildInviteOpenPageHtml,
} from "../notify-household-invite/invite-content.ts";

const htmlHeaders = {
  "Content-Type": "text/html; charset=utf-8",
  "Cache-Control": "no-store",
};

function invalidInviteHtml(): string {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Invitation link invalid</title>
</head>
<body style="margin:0;padding:32px 16px;background:#f6f1ea;font-family:Helvetica,Arial,sans-serif;color:#2c241c;text-align:center;">
  <div style="max-width:420px;margin:0 auto;background:#ffffff;border-radius:16px;padding:32px 24px;">
    <h1 style="margin:0 0 12px;font-size:24px;">Invitation link invalid</h1>
    <p style="margin:0;font-size:16px;line-height:1.5;color:#5c5348;">
      This invite link is missing or expired. Ask the household owner to send a new invitation.
    </p>
  </div>
</body>
</html>`;
}

Deno.serve((req) => {
  if (req.method !== "GET") {
    return new Response("Method not allowed", { status: 405, headers: htmlHeaders });
  }

  const token = new URL(req.url).searchParams.get("token")?.trim();
  if (!token) {
    return new Response(invalidInviteHtml(), { status: 400, headers: htmlHeaders });
  }

  const inviteDeepLink = buildInviteDeepLink(token);
  return new Response(buildInviteOpenPageHtml(inviteDeepLink), { headers: htmlHeaders });
});
