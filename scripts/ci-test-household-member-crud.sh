#!/usr/bin/env bash
# CI: exercise owner/admin household member CRUD against the local Supabase stack.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for tool in supabase jq psql; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "ERROR: ${tool} is required" >&2
    exit 1
  fi
done

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

echo "==> Testing owner/admin household member CRUD"

psql "${DB_URL}" -v ON_ERROR_STOP=1 <<'SQL'
begin;

insert into auth.users (
    id,
    aud,
    role,
    email,
    encrypted_password,
    email_confirmed_at,
    raw_app_meta_data,
    raw_user_meta_data,
    created_at,
    updated_at
)
values
    ('10000000-0000-0000-0000-000000000001', 'authenticated', 'authenticated', 'crud-owner@example.com', '', now(), '{"provider":"email","providers":["email"]}', '{"name":"CRUD Owner"}', now(), now()),
    ('10000000-0000-0000-0000-000000000002', 'authenticated', 'authenticated', 'crud-admin@example.com', '', now(), '{"provider":"email","providers":["email"]}', '{"name":"CRUD Admin"}', now(), now()),
    ('10000000-0000-0000-0000-000000000003', 'authenticated', 'authenticated', 'crud-editor@example.com', '', now(), '{"provider":"email","providers":["email"]}', '{"name":"CRUD Editor"}', now(), now()),
    ('10000000-0000-0000-0000-000000000004', 'authenticated', 'authenticated', 'crud-invitee@example.com', '', now(), '{"provider":"email","providers":["email"]}', '{"name":"CRUD Invitee"}', now(), now()),
    ('10000000-0000-0000-0000-000000000005', 'authenticated', 'authenticated', 'crud-owner-remove@example.com', '', now(), '{"provider":"email","providers":["email"]}', '{"name":"CRUD Owner Remove"}', now(), now());

insert into public.households (id, topic, name, owner_id)
values (
    '20000000-0000-0000-0000-000000000001',
    'nutrition',
    'CRUD Integration Household',
    '10000000-0000-0000-0000-000000000001'
);

insert into public.household_members (id, household_id, user_id, role)
values
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'owner'),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'admin'),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', 'editor'),
    ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', 'viewer');

-- CREATE + READ as owner.
set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000001', true);
select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000001","email":"crud-owner@example.com","role":"authenticated"}',
    true
);
select public.invite_household_member(
    '20000000-0000-0000-0000-000000000001',
    'crud-invitee@example.com',
    'viewer'
);
do $$
begin
    if (
        select count(*)
        from public.household_invites
        where household_id = '20000000-0000-0000-0000-000000000001'
          and email = 'crud-invitee@example.com'
          and accepted_at is null
          and declined_at is null
    ) <> 1 then
        raise exception 'owner_cannot_read_outbound_invite';
    end if;
end;
$$;
select public.update_household_member_role(
    '30000000-0000-0000-0000-000000000003',
    'viewer'
);
do $$
begin
    if not exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000003'
          and role = 'viewer'
          and left_at is null
    ) then
        raise exception 'owner_member_update_did_not_persist';
    end if;
end;
$$;
select public.remove_household_member('30000000-0000-0000-0000-000000000004');
reset role;

-- READ + UPDATE + DELETE as admin.
set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000002', true);
select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000002","email":"crud-admin@example.com","role":"authenticated"}',
    true
);
do $$
begin
    if (
        select count(*)
        from public.household_members
        where household_id = '20000000-0000-0000-0000-000000000001'
          and left_at is null
    ) <> 3 then
        raise exception 'admin_cannot_read_member_list';
    end if;
end;
$$;
do $$
begin
    perform public.update_household_member_role(
        '30000000-0000-0000-0000-000000000003',
        'admin'
    );
    raise exception 'admin_promotion_was_allowed';
exception
    when raise_exception then
        if sqlerrm <> 'insufficient_role' then
            raise;
        end if;
end;
$$;
select public.invite_household_member(
    '20000000-0000-0000-0000-000000000001',
    'crud-admin-invitee@example.com',
    'viewer'
);
do $$
begin
    if (
        select count(*)
        from public.household_invites
        where household_id = '20000000-0000-0000-0000-000000000001'
          and email = 'crud-admin-invitee@example.com'
          and role = 'viewer'
          and accepted_at is null
          and declined_at is null
    ) <> 1 then
        raise exception 'admin_cannot_create_or_read_outbound_invite';
    end if;

    begin
        update public.household_invites
        set role = 'admin'
        where household_id = '20000000-0000-0000-0000-000000000001'
          and email = 'crud-admin-invitee@example.com';
        raise exception 'direct_invite_update_was_allowed';
    exception
        when insufficient_privilege then null;
    end;
end;
$$;
select public.update_household_member_role(
    '30000000-0000-0000-0000-000000000003',
    'editor'
);
do $$
begin
    if not exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000003'
          and role = 'editor'
          and left_at is null
    ) then
        raise exception 'admin_member_update_did_not_persist';
    end if;
end;
$$;
select public.add_household_dependant(
    '20000000-0000-0000-0000-000000000001',
    'CRUD Dependant'
);
select public.remove_household_member('30000000-0000-0000-0000-000000000003');
select public.remove_household_dependant(
    (
        select id
        from public.household_dependants
        where household_id = '20000000-0000-0000-0000-000000000001'
          and display_name = 'CRUD Dependant'
          and removed_at is null
    )
);
do $$
begin
    perform public.remove_household_member('30000000-0000-0000-0000-000000000001');
    raise exception 'owner_removal_was_allowed';
exception
    when raise_exception then
        if sqlerrm <> 'insufficient_role' then
            raise;
        end if;
end;
$$;
reset role;

do $$
begin
    if not exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000003'
          and role = 'editor'
          and left_at is not null
    ) then
        raise exception 'admin_member_update_or_remove_did_not_persist';
    end if;

    if not exists (
        select 1
        from public.household_members
        where id = '30000000-0000-0000-0000-000000000004'
          and left_at is not null
    ) then
        raise exception 'owner_member_remove_did_not_persist';
    end if;

    if not exists (
        select 1
        from public.household_dependants
        where household_id = '20000000-0000-0000-0000-000000000001'
          and display_name = 'CRUD Dependant'
          and removed_at is not null
    ) then
        raise exception 'admin_dependant_create_or_remove_did_not_persist';
    end if;
end;
$$;

-- A removed person can be invited again and reactivated.
set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000001', true);
select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000001","email":"crud-owner@example.com","role":"authenticated"}',
    true
);
select public.invite_household_member(
    '20000000-0000-0000-0000-000000000001',
    'crud-editor@example.com',
    'editor'
);
reset role;

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000003', true);
select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000003","email":"crud-editor@example.com","role":"authenticated"}',
    true
);
select public.accept_household_invite(
    (
        select id
        from public.household_invites
        where email = 'crud-editor@example.com'
          and accepted_at is null
          and declined_at is null
    )
);
do $$
begin
    if (
        select count(*)
        from public.household_members
        where household_id = '20000000-0000-0000-0000-000000000001'
          and user_id = '10000000-0000-0000-0000-000000000003'
          and role = 'editor'
          and left_at is null
    ) <> 1 then
        raise exception 'removed_member_was_not_reactivated';
    end if;

    begin
        perform public.remove_household_member('30000000-0000-0000-0000-000000000002');
        raise exception 'editor_removal_was_allowed';
    exception
        when raise_exception then
            if sqlerrm <> 'insufficient_role' then
                raise;
            end if;
    end;
end;
$$;
reset role;

-- Invitees can read and decline only their own pending invitation.
update public.profiles
set email = 'crud-admin-invitee@example.com'
where id = '10000000-0000-0000-0000-000000000004';

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000004', true);
select set_config(
    'request.jwt.claims',
    '{"sub":"10000000-0000-0000-0000-000000000004","email":"crud-invitee@example.com","role":"authenticated"}',
    true
);
do $$
begin
    if (
        select count(*)
        from public.household_invites
        where email = 'crud-invitee@example.com'
          and accepted_at is null
          and declined_at is null
    ) <> 1 then
        raise exception 'invitee_cannot_read_own_invite';
    end if;

    if exists (
        select 1
        from public.household_invites
        where email = 'crud-admin-invitee@example.com'
          and accepted_at is null
          and declined_at is null
    ) then
        raise exception 'editable_profile_email_exposed_another_invite';
    end if;

    if jsonb_array_length(public.list_my_pending_household_invites()::jsonb) <> 1
        or public.list_my_pending_household_invites()::jsonb -> 0 ->> 'email'
            <> 'crud-invitee@example.com'
    then
        raise exception 'pending_invite_rpc_trusted_editable_profile_email';
    end if;
end;
$$;
savepoint legacy_invite_decline;
update public.household_invites
set declined_at = now()
where email = 'crud-invitee@example.com'
  and accepted_at is null
  and declined_at is null;
do $$
begin
    if not exists (
        select 1
        from public.household_invites
        where email = 'crud-invitee@example.com'
          and declined_at is not null
    ) then
        raise exception 'legacy_invite_decline_was_blocked';
    end if;

    update public.household_invites
    set declined_at = null
    where email = 'crud-invitee@example.com';

    if not exists (
        select 1
        from public.household_invites
        where email = 'crud-invitee@example.com'
          and declined_at is not null
    ) then
        raise exception 'declined_invite_was_reactivated';
    end if;
end;
$$;
rollback to savepoint legacy_invite_decline;
select public.decline_household_invite(
    (
        select id
        from public.household_invites
        where email = 'crud-invitee@example.com'
          and accepted_at is null
          and declined_at is null
    )
);
reset role;

do $$
begin
    if not exists (
        select 1
        from public.household_invites
        where email = 'crud-invitee@example.com'
          and declined_at is not null
    ) then
        raise exception 'invite_decline_did_not_persist';
    end if;
end;
$$;

rollback;
SQL

echo "Household member CRUD checks passed."
