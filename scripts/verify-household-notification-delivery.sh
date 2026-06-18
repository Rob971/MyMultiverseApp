#!/usr/bin/env bash
# Verify outbox dispatch trigger + delivery config on a linked Supabase project.
#
# Requires: SUPABASE_PROJECT_REF, SUPABASE_DB_PASSWORD
# CI: SUPABASE_ACCESS_TOKEN (uses supabase db query --linked)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/supabase-remote-psql.sh
source "${SCRIPT_DIR}/lib/supabase-remote-psql.sh"

for name in SUPABASE_PROJECT_REF SUPABASE_DB_PASSWORD; do
  if [[ -z "${!name:-}" ]]; then
    echo "ERROR: ${name} must be set" >&2
    exit 1
  fi
done

echo "==> Checking household_notification_outbox_dispatch trigger"
TRIGGER_COUNT="$(supabase_remote_query_scalar "
select count(*)::text
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
CONFIG_OK="$(supabase_remote_query_scalar "
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
