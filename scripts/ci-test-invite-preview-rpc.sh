#!/usr/bin/env bash
# CI: smoke-test preview_household_invite on the local Supabase stack (after supabase start).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v supabase >/dev/null 2>&1; then
  echo "ERROR: supabase CLI required" >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "ERROR: jq required" >&2
  exit 1
fi

load_local_supabase_credentials() {
  local status_json line key value
  status_json="$(supabase status -o json 2>/dev/null || true)"

  if [[ -n "$status_json" && "$status_json" != "null" ]]; then
    API_URL="$(echo "$status_json" | jq -r '.API_URL // empty')"
    ANON_KEY="$(echo "$status_json" | jq -r '
      .ANON_KEY //
      .PUBLISHABLE_KEY //
      .SUPABASE_PUBLISHABLE_KEY //
      .publishable_key //
      empty
    ')"
  fi

  while IFS='=' read -r key value; do
    value="${value%\"}"
    value="${value#\"}"
    case "$key" in
      API_URL)
        [[ -z "${API_URL:-}" ]] && API_URL="$value"
        ;;
      ANON_KEY|PUBLISHABLE_KEY|SUPABASE_PUBLISHABLE_KEY)
        [[ -z "${ANON_KEY:-}" ]] && ANON_KEY="$value"
        ;;
    esac
  done < <(supabase status -o env 2>/dev/null || true)
}

load_local_supabase_credentials

if [[ -z "${API_URL:-}" || -z "${ANON_KEY:-}" ]]; then
  echo "ERROR: local Supabase is not running. Run supabase start first." >&2
  exit 1
fi

REST_URL="${API_URL%/}/rest/v1"

rpc_auth_headers() {
  printf '%s\n' "-H" "apikey: ${ANON_KEY}"
  if [[ "$ANON_KEY" == eyJ* ]]; then
    printf '%s\n' "-H" "Authorization: Bearer ${ANON_KEY}"
  fi
}

echo "==> preview_household_invite rejects blank token"
PREVIEW_BODY="$(mktemp)"
mapfile -t AUTH_HEADERS < <(rpc_auth_headers)
PREVIEW_STATUS="$(curl -s -o "${PREVIEW_BODY}" -w '%{http_code}' -X POST "${REST_URL}/rpc/preview_household_invite" \
  "${AUTH_HEADERS[@]}" \
  -H "Content-Type: application/json" \
  -d '{"p_token":""}')"

if [[ "${PREVIEW_STATUS}" != "400" ]]; then
  echo "ERROR: expected HTTP 400 for blank token, got ${PREVIEW_STATUS}" >&2
  cat "${PREVIEW_BODY}" >&2
  rm -f "${PREVIEW_BODY}"
  exit 1
fi

if ! grep -qi 'invite_token_required' "${PREVIEW_BODY}"; then
  echo "ERROR: expected invite_token_required in response body" >&2
  cat "${PREVIEW_BODY}" >&2
  rm -f "${PREVIEW_BODY}"
  exit 1
fi
rm -f "${PREVIEW_BODY}"
echo "OK: preview_household_invite invite_token_required"

echo "==> preview_household_invite rejects unknown token"
UNKNOWN_BODY="$(mktemp)"
UNKNOWN_STATUS="$(curl -s -o "${UNKNOWN_BODY}" -w '%{http_code}' -X POST "${REST_URL}/rpc/preview_household_invite" \
  "${AUTH_HEADERS[@]}" \
  -H "Content-Type: application/json" \
  -d '{"p_token":"nonexistent-token-for-ci-smoke"}')"

if [[ "${UNKNOWN_STATUS}" != "400" ]]; then
  echo "ERROR: expected HTTP 400 for unknown token, got ${UNKNOWN_STATUS}" >&2
  cat "${UNKNOWN_BODY}" >&2
  rm -f "${UNKNOWN_BODY}"
  exit 1
fi

if ! grep -qi 'invite_not_found' "${UNKNOWN_BODY}"; then
  echo "ERROR: expected invite_not_found in response body" >&2
  cat "${UNKNOWN_BODY}" >&2
  rm -f "${UNKNOWN_BODY}"
  exit 1
fi
rm -f "${UNKNOWN_BODY}"
echo "OK: preview_household_invite invite_not_found"

echo "All invite preview RPC checks passed."
