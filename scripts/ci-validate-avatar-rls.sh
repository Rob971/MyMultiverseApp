#!/usr/bin/env bash
# CI: after `supabase start`, verify avatar-related RLS policies exist on Postgres tables
# used by PostgREST direct UPDATE (households, profiles, household_dependants).
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

echo "==> Validating avatar RLS policies on local Supabase stack"

STATUS_JSON="$(supabase status -o json 2>/dev/null || true)"
if [[ -z "${STATUS_JSON}" || "${STATUS_JSON}" == "null" ]]; then
  echo "ERROR: supabase stack is not running — run supabase start first" >&2
  exit 1
fi

DB_URL="$(echo "${STATUS_JSON}" | jq -r '.DB_URL // empty')"
if [[ -z "${DB_URL}" ]]; then
  echo "ERROR: could not resolve DB_URL from supabase status" >&2
  exit 1
fi

if ! command -v psql >/dev/null 2>&1; then
  echo "ERROR: psql required (install postgresql-client)" >&2
  exit 1
fi

POLICY_SQL="
SELECT tablename, policyname, cmd
FROM pg_policies
WHERE schemaname = 'public'
  AND tablename IN ('households', 'profiles', 'household_dependants')
  AND cmd IN ('SELECT', 'UPDATE')
ORDER BY tablename, cmd, policyname;
"

POLICIES="$(psql "${DB_URL}" -v ON_ERROR_STOP=1 -tA -F '|' -c "${POLICY_SQL}")"

require_policy() {
  local table="$1"
  local policy="$2"
  local cmd="$3"
  if ! echo "${POLICIES}" | grep -q "^${table}|${policy}|${cmd}$"; then
    echo "ERROR: missing RLS policy ${policy} (${cmd}) on public.${table}" >&2
    echo "Current policies:" >&2
    echo "${POLICIES}" >&2
    exit 1
  fi
  echo "OK: ${table}.${policy} (${cmd})"
}

# PostgREST UPDATE requires SELECT + UPDATE on the target table.
require_policy households households_select_member SELECT
require_policy households households_update_manager UPDATE
require_policy profiles profiles_select_own SELECT
require_policy profiles profiles_update_own UPDATE
require_policy household_dependants household_dependants_select SELECT
require_policy household_dependants household_dependants_update UPDATE

echo "All avatar RLS policy checks passed."
