#!/usr/bin/env bash
# Smoke-check ammo rebrand readiness (local + remote).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

fail=0
warn() { echo "WARN: $*"; }
ok() { echo "OK: $*"; }
bad() { echo "FAIL: $*"; fail=1; }

echo "==> Source tree"
kmp_count="$(find composeApp/src -path '*/mymultiverse/kmp/*.kt' 2>/dev/null | wc -l | tr -d ' ')"
if [[ "$kmp_count" == "0" ]]; then
  ok "no Kotlin files under app.mymultiverse.kmp"
else
  bad "found ${kmp_count} Kotlin files still under app.mymultiverse.kmp"
fi

echo "==> Android package / Firebase"
if [[ -f androidApp/google-services.json ]]; then
  pkg="$(python3 -c "import json; d=json.load(open('androidApp/google-services.json')); print(next(c['client_info']['android_client_info']['package_name'] for c in d['client']))")"
  if [[ "$pkg" == "app.mymultiverse.ammo" ]]; then
    ok "google-services.json package_name=${pkg}"
  else
    bad "google-services.json package_name=${pkg} (expected app.mymultiverse.ammo)"
  fi
else
  warn "androidApp/google-services.json missing — run ./scripts/setup-firebase-ammo-android.sh"
fi

if grep -q 'applicationId = "app.mymultiverse.ammo"' androidApp/build.gradle.kts; then
  ok "Gradle applicationId is app.mymultiverse.ammo"
else
  bad "Gradle applicationId not app.mymultiverse.ammo"
fi

echo "==> Supabase custom domain"
if curl -fsS "https://ammo.mymultiverse.app/auth/v1/health" -H "apikey: dummy" >/dev/null 2>&1; then
  ok "ammo.mymultiverse.app responds (auth API reachable)"
else
  code="$(curl -sS -o /dev/null -w '%{http_code}' "https://ammo.mymultiverse.app/auth/v1/health" || true)"
  if [[ "$code" == "401" ]]; then
    ok "ammo.mymultiverse.app TLS + routing OK (401 without apikey is expected)"
  else
    bad "ammo.mymultiverse.app health check returned HTTP ${code}"
  fi
fi

echo "==> Supabase Auth redirect allow-list"
if [[ -f "${HOME}/.supabase/access-token" ]] || [[ -n "${SUPABASE_ACCESS_TOKEN:-}" ]]; then
  token="${SUPABASE_ACCESS_TOKEN:-$(<"${HOME}/.supabase/access-token")}"
  redirects="$(curl -sS -H "Authorization: Bearer ${token}" \
    "https://api.supabase.com/v1/projects/${SUPABASE_PROJECT_REF:-ivjdzreazvkrrirecznk}/config/auth" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin).get("uri_allow_list",""))')"
  if [[ "$redirects" == *"app.mymultiverse.ammo://auth"* ]]; then
    ok "Supabase redirect URLs include app.mymultiverse.ammo"
  else
    bad "Supabase redirect URLs missing ammo scheme: ${redirects}"
  fi
  if [[ "$redirects" == *"mymultiverse.kmp"* ]]; then
    warn "legacy kmp redirects still present: ${redirects}"
  fi
else
  warn "skip Supabase redirect check (no access token)"
fi

echo "==> App Links hosting (mymultiverse.app)"
assetlinks="$(curl -fsS "https://mymultiverse.app/.well-known/assetlinks.json" 2>/dev/null || true)"
if [[ -z "$assetlinks" ]]; then
  bad "could not fetch assetlinks.json"
elif echo "$assetlinks" | grep -q '"package_name": "app.mymultiverse.ammo"'; then
  ok "assetlinks.json targets app.mymultiverse.ammo"
elif echo "$assetlinks" | grep -q '"package_name": "app.mymultiverse.kmp"'; then
  bad "assetlinks.json still targets app.mymultiverse.kmp — update mymultiverse-website ANDROID_SHA256_FINGERPRINT + redeploy hosting"
else
  warn "assetlinks.json package_name not recognized"
fi

echo "==> CI Firebase app id"
if grep -q 'FIREBASE_APP_ID: "1:37917280954:android:a0c28d6a257baf50f91083"' .github/workflows/kmp-ci.yml; then
  ok "kmp-ci.yml FIREBASE_APP_ID is ammo Android app"
else
  warn "kmp-ci.yml FIREBASE_APP_ID may still point at legacy kmp app"
fi

if [[ "$fail" -ne 0 ]]; then
  echo ""
  echo "One or more checks failed."
  exit 1
fi

echo ""
echo "All critical checks passed."
