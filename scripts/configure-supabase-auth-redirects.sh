#!/usr/bin/env bash
# Set Supabase Auth redirect allow-list for app.mymultiverse.ammo (removes legacy kmp URLs).
#
# Requires: SUPABASE_PROJECT_REF (default ivjdzreazvkrrirecznk)
# Auth: SUPABASE_ACCESS_TOKEN or ~/.supabase/access-token (from `supabase login`)

set -euo pipefail

PROJECT_REF="${SUPABASE_PROJECT_REF:-ivjdzreazvkrrirecznk}"
SITE_URL="${SUPABASE_SITE_URL:-https://mymultiverse.app}"
ALLOW_LIST="${SUPABASE_AUTH_REDIRECT_URLS:-app.mymultiverse.ammo://auth,app.mymultiverse.ammo://auth/callback}"

if [[ -n "${SUPABASE_ACCESS_TOKEN:-}" ]]; then
  token="$SUPABASE_ACCESS_TOKEN"
elif [[ -f "${HOME}/.supabase/access-token" ]]; then
  token="$(<"${HOME}/.supabase/access-token")"
else
  echo "ERROR: Set SUPABASE_ACCESS_TOKEN or run: supabase login" >&2
  exit 1
fi

payload="$(python3 - <<PY
import json
print(json.dumps({
    "site_url": "${SITE_URL}",
    "uri_allow_list": "${ALLOW_LIST}",
}))
PY
)"

echo "==> Updating Supabase Auth redirect URLs for ${PROJECT_REF}"
current="$(curl -sS \
  -H "Authorization: Bearer ${token}" \
  "https://api.supabase.com/v1/projects/${PROJECT_REF}/config/auth")"

echo "Current uri_allow_list: $(echo "$current" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("uri_allow_list",""))')"

response="$(curl -sS -X PATCH \
  -H "Authorization: Bearer ${token}" \
  -H "Content-Type: application/json" \
  -d "$payload" \
  "https://api.supabase.com/v1/projects/${PROJECT_REF}/config/auth")"

echo "$response" | python3 -c '
import json, sys
data = json.load(sys.stdin)
if "message" in data and "uri_allow_list" not in data:
    print("ERROR:", data, file=sys.stderr)
    sys.exit(1)
print("OK: uri_allow_list =", data.get("uri_allow_list", ""))
'
