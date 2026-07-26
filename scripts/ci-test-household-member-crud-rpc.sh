#!/usr/bin/env bash
# CI: exercise household member CRUD RPC authorization against fully applied local migrations.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for command_name in supabase jq psql; do
  if ! command -v "$command_name" >/dev/null 2>&1; then
    echo "ERROR: ${command_name} required" >&2
    exit 1
  fi
done

STATUS_JSON="$(supabase status -o json 2>/dev/null || true)"
DB_URL="$(jq -r '.DB_URL // empty' <<<"${STATUS_JSON}")"
if [[ -z "${DB_URL}" ]]; then
  echo "ERROR: local Supabase is not running — run supabase start first" >&2
  exit 1
fi

echo "==> Testing household member CRUD RPC authorization"
psql "${DB_URL}" -v ON_ERROR_STOP=1 <<'SQL'
begin;

insert into auth.users (id, email, raw_app_meta_data, raw_user_meta_data, created_at, updated_at)
values
    ('10000000-0000-0000-0000-000000000001', 'crud-owner@example.invalid', '{"provider":"email","providers":["email"]}', '{}', now(), now()),
    ('10000000-0000-0000-0000-000000000002', 'crud-admin@example.invalid', '{"provider":"email","providers":["email"]}', '{}', now(), now()),
    ('10000000-0000-0000-0000-000000000003', 'crud-editor@example.invalid', '{"provider":"email","providers":["email"]}', '{}', now(), now()),
    ('10000000-0000-0000-0000-000000000004', 'crud-owner-target@example.invalid', '{"provider":"email","providers":["email"]}', '{}', now(), now()),
    ('10000000-0000-0000-0000-000000000005', 'crud-admin-target@example.invalid', '{"provider":"email","providers":["email"]}', '{}', now(), now()),
    ('10000000-0000-0000-0000-000000000006', 'crud-editor-target@example.invalid', '{"provider":"email","providers":["email"]}', '{}', now(), now());

insert into public.profiles (id, display_name, email)
values
    ('10000000-0000-0000-0000-000000000001', 'CRUD Owner', 'crud-owner@example.invalid'),
    ('10000000-0000-0000-0000-000000000002', 'CRUD Admin', 'crud-admin@example.invalid'),
    ('10000000-0000-0000-0000-000000000003', 'CRUD Editor', 'crud-editor@example.invalid'),
    ('10000000-0000-0000-0000-000000000004', 'Owner Target', 'crud-owner-target@example.invalid'),
    ('10000000-0000-0000-0000-000000000005', 'Admin Target', 'crud-admin-target@example.invalid'),
    ('10000000-0000-0000-0000-000000000006', 'Editor Target', 'crud-editor-target@example.invalid')
on conflict (id) do update
set display_name = excluded.display_name,
    email = excluded.email;

insert into public.households (id, topic, name, owner_id)
values (
    '20000000-0000-0000-0000-000000000001',
    'nutrition',
    'CRUD RPC authorization test',
    '10000000-0000-0000-0000-000000000001'
);

insert into public.household_members (id, household_id, user_id, role)
values
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'owner'),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'admin'),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 'editor'),
    ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004', 'viewer'),
    ('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'admin'),
    ('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000006', 'viewer');

insert into public.household_dependants (id, household_id, display_name, created_by)
values (
    '40000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    'Unauthorized target',
    '10000000-0000-0000-0000-000000000001'
);

do $test$
begin
    if has_function_privilege('anon', 'public.add_household_dependant(uuid,text)', 'execute')
        or has_function_privilege('anon', 'public.remove_household_dependant(uuid)', 'execute')
        or has_function_privilege('anon', 'public.remove_household_member(uuid)', 'execute')
    then
        raise exception 'anon_must_not_execute_household_crud_rpcs';
    end if;

    if not has_function_privilege('authenticated', 'public.add_household_dependant(uuid,text)', 'execute')
        or not has_function_privilege('authenticated', 'public.remove_household_dependant(uuid)', 'execute')
        or not has_function_privilege('authenticated', 'public.remove_household_member(uuid)', 'execute')
    then
        raise exception 'authenticated_must_execute_household_crud_rpcs';
    end if;

    if has_table_privilege('authenticated', 'public.household_dependants', 'insert')
        or has_column_privilege('authenticated', 'public.household_dependants', 'removed_at', 'update')
        or has_column_privilege('authenticated', 'public.household_dependants', 'display_name', 'update')
    then
        raise exception 'dependant_crud_columns_must_require_manager_rpcs';
    end if;

    if not has_column_privilege('authenticated', 'public.household_dependants', 'avatar_url', 'update') then
        raise exception 'dependant_avatar_update_privilege_missing';
    end if;
end;
$test$;

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000001', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
select set_config('request.jwt.claims', '{"sub":"10000000-0000-0000-0000-000000000001","role":"authenticated"}', true);
select public.add_household_dependant(
    '20000000-0000-0000-0000-000000000001',
    'Owner dependant'
)::text as owner_dependant_id \gset
select public.remove_household_dependant(:'owner_dependant_id'::uuid);
select public.remove_household_member('30000000-0000-0000-0000-000000000004');
reset role;

do $test$
begin
    if not exists (
        select 1
        from public.household_dependants
        where display_name = 'Owner dependant'
          and removed_at is not null
    ) then
        raise exception 'owner_dependant_removal_not_persisted';
    end if;

    if exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000004'
    ) then
        raise exception 'owner_person_removal_not_persisted';
    end if;
end;
$test$;

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000002', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
select set_config('request.jwt.claims', '{"sub":"10000000-0000-0000-0000-000000000002","role":"authenticated"}', true);
select public.add_household_dependant(
    '20000000-0000-0000-0000-000000000001',
    'Admin dependant'
)::text as admin_dependant_id \gset
select public.remove_household_dependant(:'admin_dependant_id'::uuid);
select public.remove_household_member('30000000-0000-0000-0000-000000000005');

do $test$
begin
    begin
        perform public.remove_household_member('30000000-0000-0000-0000-000000000001');
        raise exception 'expected_cannot_remove_owner';
    exception
        when raise_exception then
            if sqlerrm <> 'cannot_remove_owner' then
                raise;
            end if;
    end;
end;
$test$;
reset role;

do $test$
begin
    if not exists (
        select 1
        from public.household_dependants
        where display_name = 'Admin dependant'
          and removed_at is not null
    ) then
        raise exception 'admin_dependant_removal_not_persisted';
    end if;

    if exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000005'
    ) then
        raise exception 'admin_person_removal_not_persisted';
    end if;
end;
$test$;

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000003', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
select set_config('request.jwt.claims', '{"sub":"10000000-0000-0000-0000-000000000003","role":"authenticated"}', true);
do $test$
begin
    begin
        perform public.add_household_dependant(
            '20000000-0000-0000-0000-000000000001',
            'Editor must not add'
        );
        raise exception 'expected_add_insufficient_role';
    exception
        when raise_exception then
            if sqlerrm <> 'insufficient_role' then
                raise;
            end if;
    end;

    begin
        perform public.remove_household_dependant('40000000-0000-0000-0000-000000000001');
        raise exception 'expected_dependant_remove_insufficient_role';
    exception
        when raise_exception then
            if sqlerrm <> 'insufficient_role' then
                raise;
            end if;
    end;

    begin
        perform public.remove_household_member('30000000-0000-0000-0000-000000000006');
        raise exception 'expected_member_remove_insufficient_role';
    exception
        when raise_exception then
            if sqlerrm <> 'insufficient_role' then
                raise;
            end if;
    end;
end;
$test$;
reset role;

do $test$
begin
    if exists (
        select 1
        from public.household_dependants
        where display_name = 'Editor must not add'
    ) then
        raise exception 'unauthorized_dependant_add_persisted';
    end if;

    if not exists (
        select 1
        from public.household_dependants
        where id = '40000000-0000-0000-0000-000000000001'
          and removed_at is null
    ) then
        raise exception 'unauthorized_dependant_remove_persisted';
    end if;

    if not exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000006'
          and left_at is null
    ) then
        raise exception 'unauthorized_member_remove_persisted';
    end if;
end;
$test$;

rollback;
SQL

echo "All household member CRUD RPC checks passed."
