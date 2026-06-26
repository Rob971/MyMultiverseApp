import {
  buildInviteDeepLink,
  buildInvalidInvitePageHtml,
  buildInviteOpenPageHtml,
} from "../notify-household-invite/invite-content.ts";

const htmlHeaders = {
  "Content-Type": "text/html; charset=utf-8",
  "Cache-Control": "no-store",
};

Deno.serve((req) => {
  if (req.method !== "GET") {
    return new Response("Method not allowed", { status: 405, headers: htmlHeaders });
  }

  const token = new URL(req.url).searchParams.get("token")?.trim();
  if (!token) {
    return new Response(buildInvalidInvitePageHtml(), { status: 400, headers: htmlHeaders });
  }

  const inviteDeepLink = buildInviteDeepLink(token);
  return new Response(buildInviteOpenPageHtml(inviteDeepLink), { headers: htmlHeaders });
});
