#!/usr/bin/env bash
# CI: verify household member/dependant removal RPC authorization and invite RLS.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for dependency in supabase jq psql; do
  if ! command -v "${dependency}" >/dev/null 2>&1; then
    echo "ERROR: ${dependency} is required" >&2
    exit 1
  fi
done

STATUS_JSON="$(supabase status -o json 2>/dev/null || true)"
DB_URL="$(echo "${STATUS_JSON}" | jq -r '.DB_URL // empty')"
if [[ -z "${DB_URL}" ]]; then
  echo "ERROR: local Supabase DB_URL is unavailable" >&2
  exit 1
fi

echo "==> Testing household member CRUD RPCs and invite RLS"

psql "${DB_URL}" -v ON_ERROR_STOP=1 -q <<'SQL'
begin;

create function pg_temp.assert_true(p_condition boolean, p_message text)
returns void
language plpgsql
as $$
begin
    if not coalesce(p_condition, false) then
        raise exception 'assertion_failed: %', p_message;
    end if;
end;
$$;

create function pg_temp.expect_error(p_statement text, p_expected text)
returns void
language plpgsql
as $$
declare
    v_actual text;
begin
    begin
        execute p_statement;
    exception
        when others then v_actual := sqlerrm;
    end;

    if v_actual is distinct from p_expected then
        raise exception 'expected_error %, got %', p_expected, coalesce(v_actual, '<none>');
    end if;
end;
$$;

create function pg_temp.expect_any_error(p_statement text)
returns void
language plpgsql
as $$
declare
    v_failed boolean := false;
begin
    begin
        execute p_statement;
    exception
        when others then v_failed := true;
    end;

    if not v_failed then
        raise exception 'expected statement to fail: %', p_statement;
    end if;
end;
$$;

insert into auth.users (
    instance_id,
    id,
    aud,
    role,
    email,
    encrypted_password,
    email_confirmed_at,
    raw_app_meta_data,
    raw_user_meta_data,
    created_at,
    updated_at,
    confirmation_token,
    recovery_token,
    email_change,
    email_change_token_new
)
select
    '00000000-0000-0000-0000-000000000000',
    seed.id::uuid,
    'authenticated',
    'authenticated',
    seed.email,
    '',
    now(),
    '{"provider":"email","providers":["email"]}'::jsonb,
    '{}'::jsonb,
    now(),
    now(),
    '',
    '',
    '',
    ''
from (
    values
        ('10000000-0000-4000-8000-000000000001', 'crud-owner@example.test'),
        ('10000000-0000-4000-8000-000000000002', 'crud-admin@example.test'),
        ('10000000-0000-4000-8000-000000000003', 'crud-editor@example.test'),
        ('10000000-0000-4000-8000-000000000004', 'crud-target-one@example.test'),
        ('10000000-0000-4000-8000-000000000005', 'crud-target-two@example.test')
) as seed(id, email);

insert into public.households (id, topic, name, owner_id)
values (
    '20000000-0000-4000-8000-000000000001',
    'nutrition',
    'Household CRUD regression 20000000',
    '10000000-0000-4000-8000-000000000001'
);

insert into public.household_members (id, household_id, user_id, role)
values
    ('30000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 'owner'),
    ('30000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000002', 'admin'),
    ('30000000-0000-4000-8000-000000000003', '20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000003', 'editor'),
    ('30000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000004', 'viewer'),
    ('30000000-0000-4000-8000-000000000005', '20000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000005', 'viewer');

insert into public.household_dependants (id, household_id, display_name, created_by)
values
    ('40000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', 'CRUD dependant one', '10000000-0000-4000-8000-000000000001'),
    ('40000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000001', 'CRUD dependant two', '10000000-0000-4000-8000-000000000001');

select pg_temp.assert_true(
    has_function_privilege('authenticated', 'public.remove_household_member(uuid)', 'EXECUTE'),
    'authenticated must execute remove_household_member'
);
select pg_temp.assert_true(
    not has_function_privilege('anon', 'public.remove_household_member(uuid)', 'EXECUTE'),
    'anon must not execute remove_household_member'
);
select pg_temp.assert_true(
    has_function_privilege('authenticated', 'public.remove_household_dependant(uuid)', 'EXECUTE'),
    'authenticated must execute remove_household_dependant'
);
select pg_temp.assert_true(
    not has_function_privilege('anon', 'public.remove_household_dependant(uuid)', 'EXECUTE'),
    'anon must not execute remove_household_dependant'
);
select pg_temp.assert_true(
    has_function_privilege('authenticated', 'public.decline_household_invite(uuid)', 'EXECUTE'),
    'authenticated must execute decline_household_invite'
);
select pg_temp.assert_true(
    not has_function_privilege('anon', 'public.decline_household_invite(uuid)', 'EXECUTE'),
    'anon must not execute decline_household_invite'
);

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000001', true);
select public.remove_household_member('30000000-0000-4000-8000-000000000004');
reset role;
select pg_temp.assert_true(
    not exists (select 1 from public.household_members where id = '30000000-0000-4000-8000-000000000004'),
    'owner removal must delete the target member'
);

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000002', true);
select public.remove_household_member('30000000-0000-4000-8000-000000000005');
select pg_temp.expect_error(
    $statement$select public.remove_household_member('30000000-0000-4000-8000-000000000002')$statement$,
    'cannot_remove_self'
);
select pg_temp.expect_error(
    $statement$select public.remove_household_member('30000000-0000-4000-8000-000000000001')$statement$,
    'cannot_remove_owner'
);
reset role;
select pg_temp.assert_true(
    not exists (select 1 from public.household_members where id = '30000000-0000-4000-8000-000000000005'),
    'admin removal must delete the target member'
);
select pg_temp.assert_true(
    exists (select 1 from public.household_members where id = '30000000-0000-4000-8000-000000000001'),
    'owner row must remain active'
);
select pg_temp.assert_true(
    exists (select 1 from public.household_members where id = '30000000-0000-4000-8000-000000000002'),
    'admin self row must remain active'
);

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000003', true);
select pg_temp.expect_error(
    $statement$select public.remove_household_member('30000000-0000-4000-8000-000000000002')$statement$,
    'insufficient_role'
);
select pg_temp.expect_error(
    $statement$select public.remove_household_dependant('40000000-0000-4000-8000-000000000002')$statement$,
    'insufficient_role'
);
reset role;

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000002', true);
select public.remove_household_dependant('40000000-0000-4000-8000-000000000001');
reset role;
select pg_temp.assert_true(
    (select removed_at is not null from public.household_dependants where id = '40000000-0000-4000-8000-000000000001'),
    'admin dependant removal must persist removed_at'
);
select pg_temp.assert_true(
    (select removed_at is null from public.household_dependants where id = '40000000-0000-4000-8000-000000000002'),
    'failed editor removal must leave dependant active'
);

select pg_temp.assert_true(
    (
        select count(*) = 2
        from pg_policies
        where schemaname = 'public'
          and tablename = 'household_invites'
          and policyname in (
              'household_invites_select',
              'household_invites_insert'
          )
    ),
    'household invite select/insert policies must exist'
);
select pg_temp.assert_true(
    not exists (
        select 1
        from pg_policies
        where schemaname = 'public'
          and tablename = 'household_invites'
          and cmd = 'UPDATE'
    ),
    'household invites must not expose direct UPDATE policies'
);
select pg_temp.assert_true(
    not has_table_privilege('authenticated', 'public.household_invites', 'UPDATE'),
    'authenticated must decline invites through the secured RPC'
);

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000002', true);
insert into public.household_invites (id, household_id, email, role, invited_by)
values (
    '50000000-0000-4000-8000-000000000001',
    '20000000-0000-4000-8000-000000000001',
    'crud-target-two@example.test',
    'viewer',
    '10000000-0000-4000-8000-000000000002'
);
select pg_temp.assert_true(
    (select count(*) = 1 from public.household_invites where household_id = '20000000-0000-4000-8000-000000000001'),
    'admin must read outbound household invites'
);
select pg_temp.expect_any_error(
    $statement$update public.household_invites set role = 'admin' where id = '50000000-0000-4000-8000-000000000001'$statement$
);
select pg_temp.expect_any_error(
    $statement$update public.household_invites set accepted_at = now() where id = '50000000-0000-4000-8000-000000000001'$statement$
);
select pg_temp.assert_true(
    (select role = 'viewer' from public.household_invites where id = '50000000-0000-4000-8000-000000000001'),
    'admin must not mutate an outbound invite through direct UPDATE'
);

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000003', true);
select pg_temp.assert_true(
    (select count(*) = 0 from public.household_invites where household_id = '20000000-0000-4000-8000-000000000001'),
    'editor must not read outbound household invites'
);

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000005', true);
select pg_temp.assert_true(
    (select count(*) = 1 from public.household_invites where household_id = '20000000-0000-4000-8000-000000000001'),
    'invitee must read their pending invite'
);
select pg_temp.expect_any_error(
    $statement$update public.household_invites set declined_at = now() where id = '50000000-0000-4000-8000-000000000001'$statement$
);
select pg_temp.assert_true(
    (select declined_at is null from public.household_invites where id = '50000000-0000-4000-8000-000000000001'),
    'invitee direct UPDATE must not mutate the invitation'
);
select public.decline_household_invite('50000000-0000-4000-8000-000000000001');
reset role;

select pg_temp.assert_true(
    (
        select declined_at is not null
        from public.household_invites
        where id = '50000000-0000-4000-8000-000000000001'
    ),
    'invitee must be able to decline through the secured RPC'
);

rollback;
SQL

echo "Household member CRUD RPC and invite RLS tests passed."
