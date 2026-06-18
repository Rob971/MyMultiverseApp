#!/usr/bin/env bash
# Upsert private.household_notification_delivery_config so outbox INSERT triggers
# invoke notify-household-invite via pg_net.
#
# Requires: psql, SUPABASE_PROJECT_REF, SUPABASE_DB_PASSWORD, SUPABASE_URL, SUPABASE_ANON_KEY

set -euo pipefail

for name in SUPABASE_PROJECT_REF SUPABASE_DB_PASSWORD SUPABASE_URL SUPABASE_ANON_KEY; do
  if [[ -z "${!name:-}" ]]; then
    echo "ERROR: ${name} must be set" >&2
    exit 1
  fi
done

if ! command -v psql >/dev/null 2>&1; then
  echo "ERROR: psql is required (install postgresql-client)" >&2
  exit 1
fi

export PGPASSWORD="${SUPABASE_DB_PASSWORD}"
PGHOST="db.${SUPABASE_PROJECT_REF}.supabase.co"
PGPORT=5432
PGUSER=postgres
PGDATABASE=postgres

echo "==> Configuring household notification outbox delivery"
psql "host=${PGHOST} port=${PGPORT} dbname=${PGDATABASE} user=${PGUSER} sslmode=require" \
  -v ON_ERROR_STOP=1 \
  -v project_url="${SUPABASE_URL}" \
  -v bearer_token="${SUPABASE_ANON_KEY}" \
  <<'SQL'
insert into private.household_notification_delivery_config (
    id,
    project_url,
    invoke_bearer_token,
    updated_at
)
values (
    1,
    :'project_url',
    :'bearer_token',
    now()
)
on conflict (id) do update
set project_url = excluded.project_url,
    invoke_bearer_token = excluded.invoke_bearer_token,
    updated_at = now();
SQL

echo "OK: household notification delivery config updated"
