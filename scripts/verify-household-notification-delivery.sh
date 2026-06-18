#!/usr/bin/env bash
# Verify outbox dispatch trigger + delivery config on a linked Supabase project.
#
# Requires: psql, SUPABASE_PROJECT_REF, SUPABASE_DB_PASSWORD

set -euo pipefail

for name in SUPABASE_PROJECT_REF SUPABASE_DB_PASSWORD; do
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

PSQL=(psql "host=${PGHOST} port=${PGPORT} dbname=${PGDATABASE} user=${PGUSER} sslmode=require" -v ON_ERROR_STOP=1 -tA)

echo "==> Checking household_notification_outbox_dispatch trigger"
TRIGGER_COUNT="$("${PSQL[@]}" -c "
select count(*)
from pg_trigger t
join pg_class c on c.oid = t.tgrelid
join pg_namespace n on n.oid = c.relnamespace
where n.nspname = 'public'
  and c.relname = 'household_notification_outbox'
  and t.tgname = 'household_notification_outbox_dispatch'
  and not t.tgisinternal;
")"

if [[ "${TRIGGER_COUNT}" != "1" ]]; then
  echo "ERROR: expected dispatch trigger on household_notification_outbox, found ${TRIGGER_COUNT}" >&2
  exit 1
fi
echo "OK: dispatch trigger present"

echo "==> Checking delivery config row"
CONFIG_OK="$("${PSQL[@]}" -c "
select case
    when count(*) = 1
     and char_length(trim(project_url)) > 0
     and char_length(trim(invoke_bearer_token)) > 0
    then 'yes'
    else 'no'
end
from private.household_notification_delivery_config
where id = 1;
")"

if [[ "${CONFIG_OK}" != "yes" ]]; then
  echo "ERROR: private.household_notification_delivery_config is missing or incomplete" >&2
  echo "Run: ./scripts/configure-household-notification-delivery.sh" >&2
  exit 1
fi
echo "OK: delivery config populated"

echo "All household notification delivery checks passed."
