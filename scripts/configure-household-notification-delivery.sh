#!/usr/bin/env bash
# Upsert private.household_notification_delivery_config so outbox INSERT triggers
# invoke notify-household-invite via pg_net.
#
# Requires: SUPABASE_PROJECT_REF, SUPABASE_DB_PASSWORD, SUPABASE_URL, SUPABASE_ANON_KEY
# CI: SUPABASE_ACCESS_TOKEN (uses supabase db query --linked)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/supabase-remote-psql.sh
source "${SCRIPT_DIR}/lib/supabase-remote-psql.sh"

for name in SUPABASE_PROJECT_REF SUPABASE_URL SUPABASE_ANON_KEY; do
  if [[ -z "${!name:-}" ]]; then
    echo "ERROR: ${name} must be set" >&2
    exit 1
  fi
done

if [[ -z "${SUPABASE_ACCESS_TOKEN:-}" && -z "${SUPABASE_DB_PASSWORD:-}" ]]; then
  echo "ERROR: SUPABASE_ACCESS_TOKEN or SUPABASE_DB_PASSWORD must be set" >&2
  exit 1
fi

project_url="$(escape_sql_literal "${SUPABASE_URL}")"
bearer_token="$(escape_sql_literal "${SUPABASE_ANON_KEY}")"

echo "==> Configuring household notification outbox delivery"
supabase_remote_query "
insert into private.household_notification_delivery_config (
    id,
    project_url,
    invoke_bearer_token,
    updated_at
)
values (
    1,
    '${project_url}',
    '${bearer_token}',
    now()
)
on conflict (id) do update
set project_url = excluded.project_url,
    invoke_bearer_token = excluded.invoke_bearer_token,
    updated_at = now();
"

echo "OK: household notification delivery config updated"
