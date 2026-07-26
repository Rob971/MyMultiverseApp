#!/usr/bin/env bash
# Verify final-schema household member/invite authorization on local Supabase.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v jq >/dev/null 2>&1; then
  echo "ERROR: jq required" >&2
  exit 1
fi

if ! command -v psql >/dev/null 2>&1; then
  echo "ERROR: psql required (install postgresql-client)" >&2
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "ERROR: python3 required" >&2
  exit 1
fi

DB_URL="${DB_URL:-}"
if [[ -z "${DB_URL}" ]]; then
  if ! command -v supabase >/dev/null 2>&1; then
    echo "ERROR: supabase CLI required when DB_URL is unset" >&2
    exit 1
  fi
  STATUS_JSON="$(supabase status -o json 2>/dev/null || true)"
  DB_URL="$(echo "${STATUS_JSON}" | jq -r '.DB_URL // empty')"
fi
if [[ -z "${DB_URL}" ]]; then
  echo "ERROR: local Supabase stack is not running" >&2
  exit 1
fi

echo "==> Validating household collaboration authorization"

python3 <<'PY'
from pathlib import Path
import re

source = Path(
    "composeApp/src/commonMain/kotlin/app/mymultiverse/ammo/data/supabase/"
    "SupabaseHouseholdCollaborationRepository.kt"
).read_text(encoding="utf-8")

for rpc in ("remove_household_member", "decline_household_invite", "remove_household_dependant"):
    if f'"{rpc}"' not in source:
        raise SystemExit(f"ERROR: client does not call {rpc}")

if re.search(r'client\.postgrest\["household_members"\]\s*\.delete', source):
    raise SystemExit("ERROR: client directly deletes household members")

if re.search(r'client\.postgrest\["household_invites"\]\s*\.(?:insert|update|delete)', source):
    raise SystemExit("ERROR: client directly mutates household invites")
PY

psql "${DB_URL}" -v ON_ERROR_STOP=1 <<'SQL'
begin;

do $$
declare
    v_dependant_definition text;
begin
    if to_regprocedure('public.remove_household_member(uuid)') is null
        or to_regprocedure('public.remove_household_dependant(uuid)') is null
        or to_regprocedure('public.decline_household_invite(uuid)') is null then
        raise exception 'required collaboration RPC is missing';
    end if;

    if exists (
        select 1
        from pg_proc
        where oid in (
            to_regprocedure('public.remove_household_member(uuid)'),
            to_regprocedure('public.remove_household_dependant(uuid)'),
            to_regprocedure('public.decline_household_invite(uuid)')
        )
          and (
              not prosecdef
              or not ('search_path=""' = any(coalesce(proconfig, array[]::text[])))
          )
    ) then
        raise exception 'collaboration RPC security configuration is unsafe';
    end if;

    select pg_get_functiondef(to_regprocedure('public.remove_household_dependant(uuid)'))
    into v_dependant_definition;
    if position('household_member_can_write_nutrition' in v_dependant_definition) = 0
        or position('space_member_can_write_nutrition' in v_dependant_definition) > 0 then
        raise exception 'dependant removal does not use the current write helper';
    end if;

    if not has_function_privilege(
        'authenticated',
        'public.remove_household_member(uuid)',
        'EXECUTE'
    ) or has_function_privilege(
        'anon',
        'public.remove_household_member(uuid)',
        'EXECUTE'
    ) then
        raise exception 'remove_household_member execute grants are unsafe';
    end if;

    if not has_function_privilege(
        'authenticated',
        'public.remove_household_dependant(uuid)',
        'EXECUTE'
    ) or has_function_privilege(
        'anon',
        'public.remove_household_dependant(uuid)',
        'EXECUTE'
    ) then
        raise exception 'remove_household_dependant execute grants are unsafe';
    end if;

    if not has_function_privilege(
        'authenticated',
        'public.decline_household_invite(uuid)',
        'EXECUTE'
    ) or has_function_privilege(
        'anon',
        'public.decline_household_invite(uuid)',
        'EXECUTE'
    ) then
        raise exception 'decline_household_invite execute grants are unsafe';
    end if;

    if not has_table_privilege('authenticated', 'public.household_members', 'SELECT')
        or has_table_privilege('authenticated', 'public.household_members', 'INSERT,UPDATE,DELETE') then
        raise exception 'household_members direct mutation grants are unsafe';
    end if;

    if not has_table_privilege('authenticated', 'public.household_invites', 'SELECT')
        or has_table_privilege('authenticated', 'public.household_invites', 'INSERT,UPDATE,DELETE') then
        raise exception 'household_invites direct mutation grants are unsafe';
    end if;

    if exists (
        select 1
        from pg_class c
        join pg_namespace n on n.oid = c.relnamespace
        where n.nspname = 'public'
          and c.relname in ('household_members', 'household_invites')
          and not c.relrowsecurity
    ) then
        raise exception 'household member or invite RLS is disabled';
    end if;

    if not exists (
        select 1
        from pg_policies
        where schemaname = 'public'
          and tablename = 'household_invites'
          and policyname = 'household_invites_select'
          and cmd = 'SELECT'
          and 'authenticated' = any(roles)
    ) then
        raise exception 'household invite SELECT policy is missing';
    end if;

    if exists (
        select 1
        from pg_policies
        where schemaname = 'public'
          and tablename in ('household_members', 'household_invites')
          and cmd in ('INSERT', 'UPDATE', 'DELETE', 'ALL')
    ) then
        raise exception 'direct member or invite mutation policy exists';
    end if;
end;
$$;

-- Profiles reference auth.users. These transaction-local fixtures deliberately
-- bypass only FK triggers; authorization is tested under the authenticated role.
set local session_replication_role = replica;
insert into public.profiles (id, email, display_name)
values
    ('10000000-0000-0000-0000-000000000001', 'owner@example.test', 'Owner'),
    ('10000000-0000-0000-0000-000000000002', 'admin@example.test', 'Admin'),
    ('10000000-0000-0000-0000-000000000003', 'editor@example.test', 'Editor'),
    ('10000000-0000-0000-0000-000000000004', 'viewer@example.test', 'Viewer'),
    ('10000000-0000-0000-0000-000000000005', 'invitee@example.test', 'Invitee'),
    ('10000000-0000-0000-0000-000000000006', 'other@example.test', 'Other');
set local session_replication_role = origin;

insert into public.households (id, topic, name, owner_id)
values (
    '20000000-0000-0000-0000-000000000001',
    'nutrition',
    'Security Test Household',
    '10000000-0000-0000-0000-000000000001'
);

insert into public.household_members (id, household_id, user_id, role)
values
    (
        '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001',
        'owner'
    ),
    (
        '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000002',
        'admin'
    ),
    (
        '30000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000003',
        'editor'
    ),
    (
        '30000000-0000-0000-0000-000000000004',
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000004',
        'viewer'
    );

insert into public.household_dependants (
    id,
    household_id,
    display_name,
    created_by
)
values (
    '40000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'Dependant',
    '10000000-0000-0000-0000-000000000001'
);

insert into public.household_invites (
    id,
    household_id,
    email,
    role,
    invited_by
)
values
    (
        '50000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        'invitee@example.test',
        'editor',
        '10000000-0000-0000-0000-000000000001'
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001',
        'other@example.test',
        'viewer',
        '10000000-0000-0000-0000-000000000001'
    );

-- Profile email is client-writable metadata. Invite authorization must use the
-- verified JWT email claim rather than trusting this spoofable column.
update public.profiles
set email = 'other@example.test'
where id = '10000000-0000-0000-0000-000000000005';

select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000001","email":"owner@example.test","role":"authenticated"}',
    true
);
set local role authenticated;
do $$
begin
    if (
        select count(*)
        from public.household_invites
        where household_id = '20000000-0000-0000-0000-000000000001'
    ) <> 2 then
        raise exception 'owner cannot read outbound invites';
    end if;

    perform public.remove_household_member(
        '30000000-0000-0000-0000-000000000004'
    );
end;
$$;
reset role;

select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000002","email":"admin@example.test","role":"authenticated"}',
    true
);
set local role authenticated;
do $$
declare
    v_owner_rejected boolean := false;
begin
    if (
        select count(*)
        from public.household_invites
        where household_id = '20000000-0000-0000-0000-000000000001'
    ) <> 2 then
        raise exception 'admin cannot read outbound invites';
    end if;

    begin
        perform public.remove_household_member(
            '30000000-0000-0000-0000-000000000001'
        );
    exception
        when others then
            if sqlerrm = 'cannot_remove_owner' then
                v_owner_rejected := true;
            else
                raise;
            end if;
    end;
    if not v_owner_rejected then
        raise exception 'admin removed the household owner';
    end if;
end;
$$;
reset role;

select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000003","email":"editor@example.test","role":"authenticated"}',
    true
);
set local role authenticated;
do $$
declare
    v_member_rejected boolean := false;
begin
    if (select count(*) from public.household_invites) <> 0 then
        raise exception 'editor can read outbound invites';
    end if;

    begin
        perform public.remove_household_member(
            '30000000-0000-0000-0000-000000000002'
        );
    exception
        when others then
            if sqlerrm = 'insufficient_role' then
                v_member_rejected := true;
            else
                raise;
            end if;
    end;
    if not v_member_rejected then
        raise exception 'editor removed a household member';
    end if;

    perform public.remove_household_dependant(
        '40000000-0000-0000-0000-000000000001'
    );
end;
$$;
reset role;

select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000002","email":"admin@example.test","role":"authenticated"}',
    true
);
set local role authenticated;
select public.remove_household_member(
    '30000000-0000-0000-0000-000000000003'
);
reset role;

select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000005","email":"invitee@example.test","role":"authenticated"}',
    true
);
set local role authenticated;
do $$
declare
    v_mismatch_rejected boolean := false;
begin
    if (
        select array_agg(id order by id)
        from public.household_invites
    ) <> array['50000000-0000-0000-0000-000000000001'::uuid] then
        raise exception 'invitee can read an invite for another email';
    end if;

    begin
        perform public.decline_household_invite(
            '50000000-0000-0000-0000-000000000002'
        );
    exception
        when others then
            if sqlerrm = 'invite_not_found' then
                v_mismatch_rejected := true;
            else
                raise;
            end if;
    end;
    if not v_mismatch_rejected then
        raise exception 'invitee declined an invite for another email';
    end if;

    perform public.decline_household_invite(
        '50000000-0000-0000-0000-000000000001'
    );
end;
$$;
reset role;

do $$
begin
    if exists (
        select 1
        from public.household_members
        where id in (
            '30000000-0000-0000-0000-000000000003',
            '30000000-0000-0000-0000-000000000004'
        )
    ) then
        raise exception 'manager member removal did not delete both targets';
    end if;

    if not exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000001'
          and left_at is null
          and role = 'owner'
    ) then
        raise exception 'owner member row changed';
    end if;

    insert into public.household_members (id, household_id, user_id, role)
    values (
        '30000000-0000-0000-0000-000000000005',
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000003',
        'editor'
    );

    if not exists (
        select 1
        from public.household_members
        where household_id = '20000000-0000-0000-0000-000000000001'
          and user_id = '10000000-0000-0000-0000-000000000003'
          and left_at is null
    ) then
        raise exception 'removed member cannot rejoin the household';
    end if;

    if not exists (
        select 1
        from public.household_dependants
        where id = '40000000-0000-0000-0000-000000000001'
          and removed_at is not null
    ) then
        raise exception 'dependant removal did not use the active write contract';
    end if;

    if not exists (
        select 1
        from public.household_invites
        where id = '50000000-0000-0000-0000-000000000001'
          and declined_at is not null
          and role = 'editor'
          and invited_by = '10000000-0000-0000-0000-000000000001'
    ) then
        raise exception 'decline mutated protected invite fields';
    end if;

    if not exists (
        select 1
        from public.household_invites
        where id = '50000000-0000-0000-0000-000000000002'
          and declined_at is null
    ) then
        raise exception 'mismatched invite was changed';
    end if;
end;
$$;

rollback;
SQL

echo "All household collaboration security checks passed."
