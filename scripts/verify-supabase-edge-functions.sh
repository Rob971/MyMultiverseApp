#!/usr/bin/env bash
# Smoke-test deployed Supabase edge functions (notify-household-invite, delete-account).
# Requires: curl, jq
# Env: SUPABASE_URL, SUPABASE_ANON_KEY (or local.properties with supabase.url / supabase.anonKey)

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS_FILE="${ROOT_DIR}/local.properties"

if [[ -z "${SUPABASE_URL:-}" || -z "${SUPABASE_ANON_KEY:-}" ]]; then
  if [[ ! -f "${PROPS_FILE}" ]]; then
    echo "ERROR: set SUPABASE_URL + SUPABASE_ANON_KEY or provide local.properties" >&2
    exit 1
  fi
  SUPABASE_URL="$(grep -E '^supabase\.url=' "${PROPS_FILE}" | cut -d= -f2- | tr -d '[:space:]')"
  SUPABASE_ANON_KEY="$(grep -E '^supabase\.anonKey=' "${PROPS_FILE}" | cut -d= -f2- | tr -d '[:space:]')"
fi

if [[ -z "${SUPABASE_URL}" || -z "${SUPABASE_ANON_KEY}" ]]; then
  echo "ERROR: supabase.url and supabase.anonKey must be set" >&2
  exit 1
fi

FUNCTIONS_URL="${SUPABASE_URL%/}/functions/v1"

echo "==> Probing notify-household-invite (expect 200 + processed array)"
NOTIFY_BODY="$(mktemp)"
NOTIFY_STATUS="$(curl -s -o "${NOTIFY_BODY}" -w '%{http_code}' -X POST "${FUNCTIONS_URL}/notify-household-invite" \
  -H "apikey: ${SUPABASE_ANON_KEY}" \
  -H "Authorization: Bearer ${SUPABASE_ANON_KEY}" \
  -H "Content-Type: application/json" \
  -d '{}')"

if [[ "${NOTIFY_STATUS}" != "200" ]]; then
  echo "ERROR: notify-household-invite returned ${NOTIFY_STATUS}" >&2
  cat "${NOTIFY_BODY}" >&2
  rm -f "${NOTIFY_BODY}"
  exit 1
fi

if ! jq -e 'has("processed") and (.processed | type == "array")' "${NOTIFY_BODY}" >/dev/null 2>&1; then
  echo "ERROR: notify-household-invite response missing processed[]" >&2
  cat "${NOTIFY_BODY}" >&2
  rm -f "${NOTIFY_BODY}"
  exit 1
fi
rm -f "${NOTIFY_BODY}"
echo "OK: notify-household-invite reachable"

echo "==> Probing delete-account without user session (expect 401 auth_required)"
DELETE_BODY="$(mktemp)"
DELETE_STATUS="$(curl -s -o "${DELETE_BODY}" -w '%{http_code}' -X POST "${FUNCTIONS_URL}/delete-account" \
  -H "apikey: ${SUPABASE_ANON_KEY}" \
  -H "Authorization: Bearer ${SUPABASE_ANON_KEY}" \
  -H "Content-Type: application/json" \
  -d '{}')"

if [[ "${DELETE_STATUS}" != "401" ]]; then
  echo "ERROR: delete-account without user JWT expected 401, got ${DELETE_STATUS}" >&2
  cat "${DELETE_BODY}" >&2
  rm -f "${DELETE_BODY}"
  exit 1
fi

ERROR_CODE="$(jq -r '.error // empty' "${DELETE_BODY}")"
if [[ "${ERROR_CODE}" != "auth_invalid" && "${ERROR_CODE}" != "auth_required" ]]; then
  echo "ERROR: delete-account expected auth_invalid or auth_required, got: ${ERROR_CODE}" >&2
  cat "${DELETE_BODY}" >&2
  rm -f "${DELETE_BODY}"
  exit 1
fi
rm -f "${DELETE_BODY}"
echo "OK: delete-account deployed and enforces auth"

echo "All edge function smoke probes passed."
