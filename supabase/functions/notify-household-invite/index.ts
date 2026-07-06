import { createClient, type SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2.49.1";
import { isDeliverableIosToken, sendIosInvitePush } from "./apns.ts";
import { isDeliverableAndroidToken, sendAndroidInvitePush } from "./fcm.ts";
import {
  APP_BRAND_NAME,
  buildInviteDeepLink,
  buildInviteEmailFallbackHtml,
  buildInviteEmailHtml,
  buildInviteEmailSubject,
  buildInviteEmailText,
  buildInviteWebLink,
  DEFAULT_INVITE_FROM_EMAIL,
  resolveInviteOpenLinkBase,
} from "./invite-content.ts";
import { buildInvitePushData, inviteTokenFromPayload } from "./invite-token.ts";
import { buildGroceryListNudgePushData } from "./grocery-list-nudge-token.ts";
import { buildGroceryItemAddedPushData } from "./grocery-item-added-token.ts";
import { buildMealPlanNudgePushData } from "./meal-plan-nudge-token.ts";
import { buildMealPlanItemAddedPushData } from "./meal-plan-item-added-token.ts";
import { buildMemberJoinedPushData } from "./member-joined-token.ts";
import {
  groceryItemAddedPushText,
  groceryListNudgePushText,
  mealPlanItemAddedPushText,
  mealPlanNudgePushText,
  memberJoinedPushText,
  normalizePushLocale,
  type PushLocale,
} from "./notification-i18n.ts";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

type OutboxRow = {
  id: string;
  kind: string;
  payload: Record<string, unknown>;
};

async function resolveInviteToken(
  admin: SupabaseClient,
  payload: Record<string, unknown>,
): Promise<string | null> {
  const fromPayload = inviteTokenFromPayload(payload);
  if (fromPayload) return fromPayload;

  const inviteId = String(payload.invite_id ?? "").trim();
  if (!inviteId) return null;

  const { data, error } = await admin
    .from("household_invites")
    .select("token")
    .eq("id", inviteId)
    .maybeSingle();

  if (error) {
    console.error("invite_token_lookup_failed", error.message);
    return null;
  }

  const token = data?.token ? String(data.token).trim() : "";
  return token || null;
}

type LocalizedPushContent = {
  title: string;
  body: string;
};

async function sendPushToUserTokens(
  admin: SupabaseClient,
  userId: string,
  contentForLocale: (locale: PushLocale) => LocalizedPushContent,
  data: Record<string, string>,
  fcmServiceAccountJson: string,
  apnsTeamId: string,
  apnsKeyId: string,
  apnsPrivateKey: string,
  apnsBundleId: string,
  apnsUseSandbox: boolean,
): Promise<void> {
  const { data: tokens } = await admin
    .from("user_device_tokens")
    .select("token, platform, app_locale")
    .eq("user_id", userId);

  const apnsConfigured = Boolean(apnsKeyId && apnsTeamId && apnsPrivateKey);
  let androidPushSent = 0;
  let iosPushSent = 0;

  for (const tokenRow of tokens ?? []) {
    const token = String(tokenRow.token ?? "");
    const platform = String(tokenRow.platform ?? "");
    const locale = normalizePushLocale(tokenRow.app_locale as string | null | undefined);
    const { title, body } = contentForLocale(locale);

    if (fcmServiceAccountJson && isDeliverableAndroidToken(token, platform)) {
      const ok = await sendAndroidInvitePush(
        fcmServiceAccountJson,
        token,
        title,
        body,
        data,
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
        title,
        body,
        data,
      );
      if (ok) iosPushSent += 1;
    }
  }

  console.log("push_delivery", {
    userId,
    tokenCount: tokens?.length ?? 0,
    androidPushSent,
    iosPushSent,
    fcmConfigured: Boolean(fcmServiceAccountJson),
    apnsConfigured,
  });
}

async function processHouseholdInvite(
  admin: SupabaseClient,
  payload: Record<string, unknown>,
  resendApiKey: string,
  fromEmail: string,
  inviteOpenLinkBase: string,
  fcmServiceAccountJson: string,
  apnsKeyId: string,
  apnsTeamId: string,
  apnsPrivateKey: string,
  apnsBundleId: string,
  apnsUseSandbox: boolean,
): Promise<void> {
  const inviteeEmail = String(payload.invitee_email ?? "");
  const householdName = String(payload.household_name ?? "your household");
  const inviterName = String(payload.inviter_name ?? "Someone");
  const inviteToken = await resolveInviteToken(admin, payload);

  if (resendApiKey && inviteeEmail) {
    const emailContent = inviteToken
      ? {
        inviterName,
        householdName,
        inviteeEmail,
        inviteWebLink: buildInviteWebLink(inviteToken, inviteOpenLinkBase),
        inviteDeepLink: buildInviteDeepLink(inviteToken),
      }
      : null;

    const emailResponse = await fetch("https://api.resend.com/emails", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${resendApiKey}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(
        emailContent
          ? {
            from: fromEmail,
            to: [inviteeEmail],
            subject: buildInviteEmailSubject(emailContent),
            html: buildInviteEmailHtml(emailContent),
            text: buildInviteEmailText(emailContent),
          }
          : {
            from: fromEmail,
            to: [inviteeEmail],
            subject: `${inviterName} invited you to ${householdName}`,
            html: buildInviteEmailFallbackHtml(inviterName, householdName, inviteeEmail),
            text: [
              `${inviterName} invited you to join ${householdName} on ${APP_BRAND_NAME}.`,
              `Open the app and sign in with ${inviteeEmail} to accept.`,
            ].join("\n"),
          },
      ),
    });
    if (!emailResponse.ok) {
      console.error("resend_failed", await emailResponse.text());
    } else if (!inviteToken) {
      console.warn("invite_email_sent_without_token", { inviteeEmail, householdName });
    }
  } else {
    console.log("invite_notification_skipped", { inviteeEmail, householdName });
  }

  const inviteeUserId = payload.invitee_user_id as string | null | undefined;
  if (inviteeUserId) {
    const pushData = buildInvitePushData(payload, inviteToken);
    await sendPushToUserTokens(
      admin,
      inviteeUserId,
      () => ({
        title: `${inviterName} invited you`,
        body: `Join ${householdName} on ${APP_BRAND_NAME}`,
      }),
      pushData,
      fcmServiceAccountJson,
      apnsTeamId,
      apnsKeyId,
      apnsPrivateKey,
      apnsBundleId,
      apnsUseSandbox,
    );
  }
}

async function processGroceryListNudge(
  admin: SupabaseClient,
  payload: Record<string, unknown>,
  fcmServiceAccountJson: string,
  apnsKeyId: string,
  apnsTeamId: string,
  apnsPrivateKey: string,
  apnsBundleId: string,
  apnsUseSandbox: boolean,
): Promise<void> {
  const householdId = String(payload.household_id ?? "").trim();
  const nudgerUserId = String(payload.nudger_user_id ?? "").trim();
  const nudgerName = String(payload.nudger_name ?? "Someone");
  const householdName = String(payload.household_name ?? "your household");

  if (!householdId || !nudgerUserId) {
    console.warn("grocery_list_nudge_missing_ids", { householdId, nudgerUserId });
    return;
  }

  const { data: members, error } = await admin
    .from("household_members")
    .select("user_id")
    .eq("household_id", householdId)
    .neq("user_id", nudgerUserId);

  if (error) {
    console.error("grocery_list_nudge_recipients_failed", error.message);
    return;
  }

  const pushData = buildGroceryListNudgePushData(payload);

  for (const member of members ?? []) {
    const userId = String(member.user_id ?? "").trim();
    if (!userId) continue;
    await sendPushToUserTokens(
      admin,
      userId,
      (locale) => groceryListNudgePushText(locale, nudgerName, householdName),
      pushData,
      fcmServiceAccountJson,
      apnsKeyId,
      apnsTeamId,
      apnsPrivateKey,
      apnsBundleId,
      apnsUseSandbox,
    );
  }
}

async function processMealPlanNudge(
  admin: SupabaseClient,
  payload: Record<string, unknown>,
  fcmServiceAccountJson: string,
  apnsKeyId: string,
  apnsTeamId: string,
  apnsPrivateKey: string,
  apnsBundleId: string,
  apnsUseSandbox: boolean,
): Promise<void> {
  const householdId = String(payload.household_id ?? "").trim();
  const nudgerUserId = String(payload.nudger_user_id ?? "").trim();
  const nudgerName = String(payload.nudger_name ?? "Someone");
  const householdName = String(payload.household_name ?? "your household");

  if (!householdId || !nudgerUserId) {
    console.warn("meal_plan_nudge_missing_ids", { householdId, nudgerUserId });
    return;
  }

  const { data: members, error } = await admin
    .from("household_members")
    .select("user_id")
    .eq("household_id", householdId)
    .neq("user_id", nudgerUserId);

  if (error) {
    console.error("meal_plan_nudge_recipients_failed", error.message);
    return;
  }

  const pushData = buildMealPlanNudgePushData(payload);

  for (const member of members ?? []) {
    const userId = String(member.user_id ?? "").trim();
    if (!userId) continue;
    await sendPushToUserTokens(
      admin,
      userId,
      (locale) => mealPlanNudgePushText(locale, nudgerName, householdName),
      pushData,
      fcmServiceAccountJson,
      apnsKeyId,
      apnsTeamId,
      apnsPrivateKey,
      apnsBundleId,
      apnsUseSandbox,
    );
  }
}

async function processMemberJoined(
  admin: SupabaseClient,
  payload: Record<string, unknown>,
  fcmServiceAccountJson: string,
  apnsKeyId: string,
  apnsTeamId: string,
  apnsPrivateKey: string,
  apnsBundleId: string,
  apnsUseSandbox: boolean,
): Promise<void> {
  const householdId = String(payload.household_id ?? "").trim();
  const memberUserId = String(payload.member_user_id ?? "").trim();
  const memberName = String(payload.member_name ?? "Someone");
  const householdName = String(payload.household_name ?? "your household");

  if (!householdId || !memberUserId) {
    console.warn("member_joined_missing_ids", { householdId, memberUserId });
    return;
  }

  const { data: members, error } = await admin
    .from("household_members")
    .select("user_id")
    .eq("household_id", householdId)
    .neq("user_id", memberUserId);

  if (error) {
    console.error("member_joined_recipients_failed", error.message);
    return;
  }

  const pushData = buildMemberJoinedPushData(payload);

  for (const member of members ?? []) {
    const userId = String(member.user_id ?? "").trim();
    if (!userId) continue;
    await sendPushToUserTokens(
      admin,
      userId,
      (locale) => memberJoinedPushText(locale, memberName, householdName),
      pushData,
      fcmServiceAccountJson,
      apnsTeamId,
      apnsKeyId,
      apnsPrivateKey,
      apnsBundleId,
      apnsUseSandbox,
    );
  }
}

async function processGroceryItemAdded(
  admin: SupabaseClient,
  payload: Record<string, unknown>,
  fcmServiceAccountJson: string,
  apnsKeyId: string,
  apnsTeamId: string,
  apnsPrivateKey: string,
  apnsBundleId: string,
  apnsUseSandbox: boolean,
): Promise<void> {
  const householdId = String(payload.household_id ?? "").trim();
  const actorUserId = String(payload.actor_user_id ?? "").trim();
  const actorName = String(payload.actor_name ?? "Someone");
  const itemLabel = String(payload.item_label ?? "");
  const addedCount = Number(payload.added_count ?? 1);

  if (!householdId || !actorUserId) {
    console.warn("grocery_item_added_missing_ids", { householdId, actorUserId });
    return;
  }

  const { data: members, error } = await admin
    .from("household_members")
    .select("user_id")
    .eq("household_id", householdId)
    .neq("user_id", actorUserId);

  if (error) {
    console.error("grocery_item_added_recipients_failed", error.message);
    return;
  }

  const pushData = buildGroceryItemAddedPushData(payload);

  for (const member of members ?? []) {
    const userId = String(member.user_id ?? "").trim();
    if (!userId) continue;
    await sendPushToUserTokens(
      admin,
      userId,
      (locale) => groceryItemAddedPushText(locale, actorName, itemLabel, addedCount),
      pushData,
      fcmServiceAccountJson,
      apnsKeyId,
      apnsTeamId,
      apnsPrivateKey,
      apnsBundleId,
      apnsUseSandbox,
    );
  }
}

async function processMealPlanItemAdded(
  admin: SupabaseClient,
  payload: Record<string, unknown>,
  fcmServiceAccountJson: string,
  apnsKeyId: string,
  apnsTeamId: string,
  apnsPrivateKey: string,
  apnsBundleId: string,
  apnsUseSandbox: boolean,
): Promise<void> {
  const householdId = String(payload.household_id ?? "").trim();
  const actorUserId = String(payload.actor_user_id ?? "").trim();
  const actorName = String(payload.actor_name ?? "Someone");
  const itemLabel = String(payload.item_label ?? "");
  const addedCount = Number(payload.added_count ?? 1);
  const dayIndex = Number(payload.day_index ?? 0);
  const mealSlot = String(payload.meal_slot ?? "");

  if (!householdId || !actorUserId) {
    console.warn("meal_plan_item_added_missing_ids", { householdId, actorUserId });
    return;
  }

  const { data: members, error } = await admin
    .from("household_members")
    .select("user_id")
    .eq("household_id", householdId)
    .neq("user_id", actorUserId);

  if (error) {
    console.error("meal_plan_item_added_recipients_failed", error.message);
    return;
  }

  const pushData = buildMealPlanItemAddedPushData(payload);

  for (const member of members ?? []) {
    const userId = String(member.user_id ?? "").trim();
    if (!userId) continue;
    await sendPushToUserTokens(
      admin,
      userId,
      (locale) => mealPlanItemAddedPushText(
        locale,
        actorName,
        itemLabel,
        addedCount,
        dayIndex,
        mealSlot,
      ),
      pushData,
      fcmServiceAccountJson,
      apnsKeyId,
      apnsTeamId,
      apnsPrivateKey,
      apnsBundleId,
      apnsUseSandbox,
    );
  }
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    const resendApiKey = Deno.env.get("RESEND_API_KEY") ?? "";
    const fromEmail = Deno.env.get("INVITE_FROM_EMAIL") ?? DEFAULT_INVITE_FROM_EMAIL;
    const fcmServiceAccountJson = Deno.env.get("FCM_SERVICE_ACCOUNT_JSON") ?? "";
    const apnsKeyId = Deno.env.get("APNS_KEY_ID") ?? "";
    const apnsTeamId = Deno.env.get("APNS_TEAM_ID") ?? "";
    const apnsPrivateKey = Deno.env.get("APNS_PRIVATE_KEY") ?? "";
    const apnsBundleId = Deno.env.get("APNS_BUNDLE_ID") ?? "app.mymultiverse.ammo";
    const apnsUseSandbox = (Deno.env.get("APNS_USE_SANDBOX") ?? "true").toLowerCase() !== "false";
    const inviteOpenLinkBase = resolveInviteOpenLinkBase(
      Deno.env.get("INVITE_OPEN_LINK_BASE"),
      supabaseUrl,
    );

    const admin = createClient(supabaseUrl, serviceRoleKey);

    const body = req.method === "POST" ? await req.json().catch(() => ({})) : {};
    const outboxId = body?.outbox_id as string | undefined;

    let query = admin
      .from("household_notification_outbox")
      .select("id, kind, payload")
      .is("processed_at", null)
      .in("kind", [
        "household_invite",
        "household_member_joined",
        "grocery_list_nudge",
        "meal_plan_nudge",
        "grocery_item_added",
        "meal_plan_item_added",
      ])
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

      if (row.kind === "household_invite") {
        await processHouseholdInvite(
          admin,
          payload,
          resendApiKey,
          fromEmail,
          inviteOpenLinkBase,
          fcmServiceAccountJson,
          apnsKeyId,
          apnsTeamId,
          apnsPrivateKey,
          apnsBundleId,
          apnsUseSandbox,
        );
      } else if (row.kind === "household_member_joined") {
        await processMemberJoined(
          admin,
          payload,
          fcmServiceAccountJson,
          apnsKeyId,
          apnsTeamId,
          apnsPrivateKey,
          apnsBundleId,
          apnsUseSandbox,
        );
      } else if (row.kind === "grocery_list_nudge") {
        await processGroceryListNudge(
          admin,
          payload,
          fcmServiceAccountJson,
          apnsKeyId,
          apnsTeamId,
          apnsPrivateKey,
          apnsBundleId,
          apnsUseSandbox,
        );
      } else if (row.kind === "meal_plan_nudge") {
        await processMealPlanNudge(
          admin,
          payload,
          fcmServiceAccountJson,
          apnsKeyId,
          apnsTeamId,
          apnsPrivateKey,
          apnsBundleId,
          apnsUseSandbox,
        );
      } else if (row.kind === "grocery_item_added") {
        await processGroceryItemAdded(
          admin,
          payload,
          fcmServiceAccountJson,
          apnsKeyId,
          apnsTeamId,
          apnsPrivateKey,
          apnsBundleId,
          apnsUseSandbox,
        );
      } else if (row.kind === "meal_plan_item_added") {
        await processMealPlanItemAdded(
          admin,
          payload,
          fcmServiceAccountJson,
          apnsKeyId,
          apnsTeamId,
          apnsPrivateKey,
          apnsBundleId,
          apnsUseSandbox,
        );
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
