import { createClient } from "https://esm.sh/@supabase/supabase-js@2.49.1";
import { isDeliverableIosToken, sendIosInvitePush } from "./apns.ts";
import { isDeliverableAndroidToken, sendAndroidInvitePush } from "./fcm.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

type OutboxRow = {
  id: string;
  kind: string;
  payload: Record<string, unknown>;
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const resendApiKey = Deno.env.get("RESEND_API_KEY") ?? "";
    const fromEmail = Deno.env.get("INVITE_FROM_EMAIL") ?? "invites@mymultiverse.app";
    const fcmServiceAccountJson = Deno.env.get("FCM_SERVICE_ACCOUNT_JSON") ?? "";
    const apnsKeyId = Deno.env.get("APNS_KEY_ID") ?? "";
    const apnsTeamId = Deno.env.get("APNS_TEAM_ID") ?? "";
    const apnsPrivateKey = Deno.env.get("APNS_PRIVATE_KEY") ?? "";
    const apnsBundleId = Deno.env.get("APNS_BUNDLE_ID") ?? "app.mymultiverse.kmp";
    const apnsUseSandbox = (Deno.env.get("APNS_USE_SANDBOX") ?? "true").toLowerCase() !== "false";

    const admin = createClient(supabaseUrl, serviceRoleKey);

    const body = req.method === "POST" ? await req.json().catch(() => ({})) : {};
    const outboxId = body?.outbox_id as string | undefined;

    let query = admin
      .from("household_notification_outbox")
      .select("id, kind, payload")
      .is("processed_at", null)
      .eq("kind", "household_invite")
      .order("created_at", { ascending: true })
      .limit(25);

    if (outboxId) {
      query = admin
        .from("household_notification_outbox")
        .select("id, kind, payload")
        .eq("id", outboxId)
        .limit(1);
    }

    const { data: rows, error: fetchError } = await query;
    if (fetchError) {
      return new Response(JSON.stringify({ error: fetchError.message }), {
        status: 500,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      });
    }

    const processed: string[] = [];

    for (const row of (rows ?? []) as OutboxRow[]) {
      const payload = row.payload ?? {};
      const inviteeEmail = String(payload.invitee_email ?? "");
      const householdName = String(payload.household_name ?? "MyMultiverse");
      const inviterName = String(payload.inviter_name ?? "Someone");

      if (resendApiKey && inviteeEmail) {
        const emailResponse = await fetch("https://api.resend.com/emails", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${resendApiKey}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            from: fromEmail,
            to: [inviteeEmail],
            subject: `${inviterName} invited you to ${householdName}`,
            html: `<p>${inviterName} invited you to join <strong>${householdName}</strong> on MyMultiverse.</p><p>Open the app and sign in with <strong>${inviteeEmail}</strong> to accept.</p>`,
          }),
        });
        if (!emailResponse.ok) {
          console.error("resend_failed", await emailResponse.text());
        }
      } else {
        console.log("invite_notification_skipped", { inviteeEmail, householdName });
      }

      const inviteeUserId = payload.invitee_user_id as string | null | undefined;
      if (inviteeUserId) {
        const { data: tokens } = await admin
          .from("user_device_tokens")
          .select("token, platform")
          .eq("user_id", inviteeUserId);

        const pushTitle = `${inviterName} invited you`;
        const pushBody = `Join ${householdName} on MyMultiverse`;
        const pushData = {
          type: "household_invite",
          invite_id: String(payload.invite_id ?? ""),
          household_id: String(payload.household_id ?? ""),
        };

        let androidPushSent = 0;
        let iosPushSent = 0;
        const apnsConfigured = Boolean(apnsKeyId && apnsTeamId && apnsPrivateKey);

        for (const tokenRow of tokens ?? []) {
          const token = String(tokenRow.token ?? "");
          const platform = String(tokenRow.platform ?? "");

          if (fcmServiceAccountJson && isDeliverableAndroidToken(token, platform)) {
            const ok = await sendAndroidInvitePush(
              fcmServiceAccountJson,
              token,
              pushTitle,
              pushBody,
              pushData,
            );
            if (ok) androidPushSent += 1;
            continue;
          }

          if (apnsConfigured && isDeliverableIosToken(token, platform)) {
            const ok = await sendIosInvitePush(
              apnsTeamId,
              apnsKeyId,
              apnsPrivateKey,
              apnsBundleId,
              apnsUseSandbox,
              token,
              pushTitle,
              pushBody,
              pushData,
            );
            if (ok) iosPushSent += 1;
          }
        }

        console.log("push_delivery", {
          userId: inviteeUserId,
          tokenCount: tokens?.length ?? 0,
          androidPushSent,
          iosPushSent,
          fcmConfigured: Boolean(fcmServiceAccountJson),
          apnsConfigured,
        });
      }

      await admin
        .from("household_notification_outbox")
        .update({ processed_at: new Date().toISOString() })
        .eq("id", row.id);

      processed.push(row.id);
    }

    return new Response(JSON.stringify({ processed }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (error) {
    return new Response(JSON.stringify({ error: String(error) }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});
