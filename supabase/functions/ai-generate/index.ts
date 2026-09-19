// ai-generate: the app's only path to Gemini. The Gemini key lives in this function's
// secrets (GEMINI_API_KEY), never in the app. Every request must come from a signed-in user
// and is counted against per-user and project-wide daily limits before it reaches Gemini.
// Prompts and responses are never logged.
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.49.1";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

// The model is fixed here: a client cannot pick a more expensive one.
const MODEL_ID = "gemini-3.1-flash-lite";
const MAX_BODY_CHARS = 32_000;
const MAX_OUTPUT_TOKENS = 4_096;

function envInt(name: string, fallback: number): number {
  const value = Number.parseInt(Deno.env.get(name) ?? "", 10);
  return Number.isFinite(value) && value > 0 ? value : fallback;
}

function json(status: number, body: Record<string, unknown>): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

/** Keeps only what the app sends; anything else in the request is dropped. */
function sanitize(body: unknown): Record<string, unknown> | null {
  if (typeof body !== "object" || body === null) return null;
  const { contents, generationConfig } = body as Record<string, unknown>;
  if (!Array.isArray(contents) || contents.length === 0) return null;
  const config = (typeof generationConfig === "object" && generationConfig !== null)
    ? generationConfig as Record<string, unknown>
    : {};
  const temperature = typeof config.temperature === "number"
    ? Math.min(Math.max(config.temperature, 0), 2)
    : 0.7;
  const requestedTokens = typeof config.maxOutputTokens === "number" ? config.maxOutputTokens : 1_024;
  const maxOutputTokens = Math.min(Math.max(Math.floor(requestedTokens), 1), MAX_OUTPUT_TOKENS);
  return { contents, generationConfig: { temperature, maxOutputTokens } };
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }
  if (req.method !== "POST") {
    return json(405, { error: "method_not_allowed" });
  }

  const geminiKey = Deno.env.get("GEMINI_API_KEY") ?? "";
  if (!geminiKey) {
    return json(503, { error: "ai_not_configured" });
  }

  const authHeader = req.headers.get("Authorization");
  if (!authHeader) {
    return json(401, { error: "auth_required" });
  }
  const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
  const userClient = createClient(supabaseUrl, Deno.env.get("SUPABASE_ANON_KEY") ?? "", {
    global: { headers: { Authorization: authHeader } },
  });
  const { data: userData, error: userError } = await userClient.auth.getUser();
  if (userError || !userData.user) {
    return json(401, { error: "auth_invalid" });
  }

  const raw = await req.text();
  if (raw.length > MAX_BODY_CHARS) {
    return json(413, { error: "request_too_large" });
  }
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch {
    return json(400, { error: "invalid_json" });
  }
  const forward = sanitize(parsed);
  if (!forward) {
    return json(400, { error: "invalid_request" });
  }

  // Counted before calling Gemini, so failed or slow upstream calls still count as usage.
  const admin = createClient(supabaseUrl, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "");
  const { error: quotaError } = await admin.rpc("consume_ai_quota", {
    p_user_id: userData.user.id,
    p_user_limit: envInt("AI_DAILY_USER_LIMIT", 50),
    p_global_limit: envInt("AI_DAILY_GLOBAL_LIMIT", 2_000),
  });
  if (quotaError) {
    const message = quotaError.message ?? "";
    if (message.includes("ai_user_limit") || message.includes("ai_global_limit")) {
      return json(429, { error: message.includes("ai_user_limit") ? "ai_user_limit" : "ai_global_limit" });
    }
    console.error("consume_ai_quota failed");
    return json(500, { error: "quota_unavailable" });
  }

  const baseUrl = Deno.env.get("GEMINI_BASE_URL") ?? "https://generativelanguage.googleapis.com";
  let upstream: Response;
  try {
    upstream = await fetch(`${baseUrl}/v1beta/models/${MODEL_ID}:generateContent`, {
      method: "POST",
      headers: { "Content-Type": "application/json", "x-goog-api-key": geminiKey },
      body: JSON.stringify(forward),
      // Under the 30 s the app waits for a seasonal week; shorter calls time out in the app first (10-15 s).
      signal: AbortSignal.timeout(envInt("AI_UPSTREAM_TIMEOUT_MS", 25_000)),
    });
  } catch (e) {
    const timedOut = e instanceof DOMException && (e.name === "TimeoutError" || e.name === "AbortError");
    console.error(timedOut ? "gemini timeout" : "gemini unreachable");
    return json(timedOut ? 504 : 502, { error: timedOut ? "upstream_timeout" : "upstream_unreachable" });
  }

  const text = await upstream.text();
  if (!upstream.ok) {
    // Upstream 4xx (including an invalid server key) is our fault, not the user's: report 502.
    console.error(`gemini status ${upstream.status}`);
    return json(502, { error: "upstream_error", upstreamStatus: upstream.status });
  }
  return new Response(text, {
    status: 200,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
});
