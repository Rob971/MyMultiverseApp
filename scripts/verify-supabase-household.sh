#!/usr/bin/env bash
# Smoke-test Supabase household bootstrap + nutrition persistence against a configured project.
# Requires: curl, jq, local.properties with supabase.url and supabase.anonKey
# Optional: SUPABASE_TEST_EMAIL and SUPABASE_TEST_PASSWORD for authenticated RPC checks.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROPS_FILE="${ROOT_DIR}/local.properties"

if [[ ! -f "${PROPS_FILE}" ]]; then
  echo "ERROR: missing ${PROPS_FILE}" >&2
  exit 1
fi

SUPABASE_URL="$(grep -E '^supabase\.url=' "${PROPS_FILE}" | cut -d= -f2- | tr -d '[:space:]')"
ANON_KEY="$(grep -E '^supabase\.anonKey=' "${PROPS_FILE}" | cut -d= -f2- | tr -d '[:space:]')"

if [[ -z "${SUPABASE_URL}" || -z "${ANON_KEY}" ]]; then
  echo "ERROR: supabase.url and supabase.anonKey must be set in local.properties" >&2
  exit 1
fi

REST_URL="${SUPABASE_URL%/}/rest/v1"
AUTH_URL="${SUPABASE_URL%/}/auth/v1"

echo "==> Checking PostgREST reachability"
HEALTH_STATUS="$(curl -s -o /dev/null -w '%{http_code}' -H "apikey: ${ANON_KEY}" "${REST_URL}/")"
if [[ "${HEALTH_STATUS}" != "200" && "${HEALTH_STATUS}" != "401" ]]; then
  echo "ERROR: Supabase REST not reachable (status ${HEALTH_STATUS})" >&2
  exit 1
fi
echo "OK: Supabase REST reachable (status ${HEALTH_STATUS})"

echo "==> Checking ensure_household RPC is deployed"
OPENAPI_STATUS="$(curl -s -o /tmp/supabase-openapi.json -w '%{http_code}' "${REST_URL}/" \
  -H "apikey: ${ANON_KEY}" \
  -H "Authorization: Bearer ${ANON_KEY}" \
  -H "Accept: application/openapi+json")"
OPENAPI_BODY="$(cat /tmp/supabase-openapi.json)"
rm -f /tmp/supabase-openapi.json

if [[ "${OPENAPI_STATUS}" != "200" ]]; then
  echo "ERROR: could not fetch PostgREST OpenAPI schema (status ${OPENAPI_STATUS})" >&2
  exit 1
fi

if ! echo "${OPENAPI_BODY}" | jq -e '.paths["/rpc/ensure_household"].post' >/dev/null 2>&1; then
  echo "ERROR: ensure_household RPC not found in PostgREST schema. Run: supabase db push" >&2
  exit 1
fi
echo "OK: ensure_household RPC exposed in PostgREST schema"

if [[ -n "${SUPABASE_TEST_EMAIL:-}" && -n "${SUPABASE_TEST_PASSWORD:-}" ]]; then
  echo "==> Signing in test user ${SUPABASE_TEST_EMAIL}"
  TOKEN_RESPONSE="$(curl -fsS -X POST "${AUTH_URL}/token?grant_type=password" \
    -H "apikey: ${ANON_KEY}" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"${SUPABASE_TEST_EMAIL}\",\"password\":\"${SUPABASE_TEST_PASSWORD}\"}")"
  ACCESS_TOKEN="$(echo "${TOKEN_RESPONSE}" | jq -r '.access_token')"

  if [[ -z "${ACCESS_TOKEN}" || "${ACCESS_TOKEN}" == "null" ]]; then
    echo "ERROR: could not obtain access token" >&2
    echo "${TOKEN_RESPONSE}" >&2
    exit 1
  fi

  echo "==> Calling ensure_household as authenticated user"
  HOUSEHOLD_JSON="$(curl -fsS -X POST "${REST_URL}/rpc/ensure_household" \
    -H "apikey: ${ANON_KEY}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{}')"

  HOUSEHOLD_ID="$(echo "${HOUSEHOLD_JSON}" | jq -r '.household_id')"
  FEATURE_COUNT="$(echo "${HOUSEHOLD_JSON}" | jq -r '.features | length')"

  if [[ -z "${HOUSEHOLD_ID}" || "${HOUSEHOLD_ID}" == "null" ]]; then
    echo "ERROR: ensure_household did not return household_id" >&2
    echo "${HOUSEHOLD_JSON}" >&2
    exit 1
  fi

  echo "OK: household_id=${HOUSEHOLD_ID} features=${FEATURE_COUNT}"

  WEEK_KEY="$(date -u +%Y-%m-%d)"
  PAYLOAD='[{"id":"verify-1","label":"Supabase verify rice","isChecked":false}]'

  echo "==> Upserting nutrition week grocery payload"
  UPSERT_STATUS="$(curl -s -o /dev/null -w '%{http_code}' \
    -X POST "${REST_URL}/nutrition_household_week_data" \
    -H "apikey: ${ANON_KEY}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}" \
    -H "Content-Type: application/json" \
    -H "Prefer: resolution=merge-duplicates" \
    -d "[{\"household_id\":\"${HOUSEHOLD_ID}\",\"week_key\":\"${WEEK_KEY}\",\"data_kind\":\"grocery\",\"payload\":$(echo "${PAYLOAD}" | jq -c .)}]")"

  if [[ "${UPSERT_STATUS}" != "201" && "${UPSERT_STATUS}" != "200" ]]; then
    echo "ERROR: nutrition upsert failed with status ${UPSERT_STATUS}" >&2
    exit 1
  fi

  echo "==> Reading nutrition week grocery payload back"
  FETCHED="$(curl -fsS \
    "${REST_URL}/nutrition_household_week_data?household_id=eq.${HOUSEHOLD_ID}&week_key=eq.${WEEK_KEY}&data_kind=eq.grocery&select=payload" \
    -H "apikey: ${ANON_KEY}" \
    -H "Authorization: Bearer ${ACCESS_TOKEN}")"

  if ! echo "${FETCHED}" | jq -e '.[0].payload | contains("verify rice")' >/dev/null; then
    echo "ERROR: fetched payload does not contain test item" >&2
    echo "${FETCHED}" >&2
    exit 1
  fi

  echo "OK: nutrition persistence round-trip succeeded for week ${WEEK_KEY}"
else
  echo "SKIP: set SUPABASE_TEST_EMAIL and SUPABASE_TEST_PASSWORD to run authenticated persistence round-trip"
fi

echo "All Supabase household checks passed."
