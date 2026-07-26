-- Repair household member CRUD authorization after the household terminology
-- migration dropped invite policies and left legacy dependant/member write paths.

-- Managers can inspect pending outbound invites. Invitees may read only invites
-- addressed to their authenticated email. Role and identity mutations stay
-- behind RPCs; legacy clients retain column-scoped access to decline an invite.
drop policy if exists space_invites_select on public.household_invites;
drop policy if exists space_invites_insert on public.household_invites;
drop policy if exists space_invites_update on public.household_invites;
drop policy if exists household_invites_select on public.household_invites;
drop policy if exists household_invites_insert on public.household_invites;
drop policy if exists household_invites_update on public.household_invites;
drop policy if exists household_invites_update_manager on public.household_invites;
drop policy if exists household_invites_update_invitee_decline on public.household_invites;

create policy household_invites_select
    on public.household_invites
    for select
    to authenticated
    using (
        public.is_household_manager(household_id)
        or lower(trim(email)) = lower(trim(coalesce(auth.jwt() ->> 'email', '')))
    );

create policy household_invites_update_invitee_decline
    on public.household_invites
    for update
    to authenticated
    using (
        accepted_at is null
        and declined_at is null
        and expires_at > now()
        and lower(trim(email)) = lower(trim(coalesce(auth.jwt() ->> 'email', '')))
    )
    with check (
        accepted_at is null
        and declined_at is not null
        and expires_at > now()
        and lower(trim(email)) = lower(trim(coalesce(auth.jwt() ->> 'email', '')))
    );

-- A broad UPDATE grant would let an admin bypass invite_household_member() and
-- assign an admin/owner role. Only the legacy decline column remains writable.
revoke insert, update, delete on public.household_invites from authenticated;
grant select on public.household_invites to authenticated;
grant update (declined_at) on public.household_invites to authenticated;

-- Invite identity must come from the signed Auth token, never the editable
-- public profile row.
create or replace function public.list_my_pending_household_invites()
returns json
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_auth_email text := lower(trim(coalesce(auth.jwt() ->> 'email', '')));
    v_result json;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_auth_email) = 0 then
        return '[]'::json;
    end if;

    select coalesce(
        json_agg(
            json_build_object(
                'id', i.id,
                'household_id', i.household_id,
                'household_name', h.name,
                'email', i.email,
                'role', i.role::text,
                'expires_at', i.expires_at
            )
            order by h.name
        ),
        '[]'::json
    )
    into v_result
    from public.household_invites i
    join public.households h on h.id = i.household_id
    where lower(trim(i.email)) = v_auth_email
      and i.accepted_at is null
      and i.declined_at is null
      and i.expires_at > now();

    return v_result;
end;
$$;

revoke all on function public.list_my_pending_household_invites() from public;
grant execute on function public.list_my_pending_household_invites() to authenticated;

-- Member removal follows the same owner/admin authorization matrix as role
-- changes. Soft removal preserves audit history and permits a later re-invite.
create or replace function public.remove_household_member(p_member_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
    v_target_user_id uuid;
    v_target_role public.household_member_role;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select sm.household_id, sm.user_id, sm.role
    into v_household_id, v_target_user_id, v_target_role
    from public.household_members sm
    where sm.id = p_member_id
      and sm.user_id is not null
      and sm.left_at is null
    for update;

    if not found then
        raise exception 'member_not_found';
    end if;

    if v_target_role = 'owner' or exists (
        select 1
        from public.households h
        where h.id = v_household_id
          and h.owner_id = v_target_user_id
    ) then
        raise exception 'insufficient_role';
    end if;

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_members
    set left_at = now()
    where id = p_member_id;
end;
$$;

revoke all on function public.remove_household_member(uuid) from public;
grant execute on function public.remove_household_member(uuid) to authenticated;

-- The previous function still referenced space_member_can_write_nutrition(),
-- which was removed during the household terminology migration.
create or replace function public.remove_household_dependant(p_dependant_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select hd.household_id
    into v_household_id
    from public.household_dependants hd
    where hd.id = p_dependant_id
      and hd.removed_at is null
    for update;

    if not found then
        raise exception 'dependant_not_found';
    end if;

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_dependants
    set removed_at = now()
    where id = p_dependant_id;
end;
$$;

revoke all on function public.remove_household_dependant(uuid) from public;
grant execute on function public.remove_household_dependant(uuid) to authenticated;

-- Invitees decline through a narrow RPC rather than a broad table UPDATE policy.
create or replace function public.decline_household_invite(p_invite_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_auth_email text := lower(trim(coalesce(auth.jwt() ->> 'email', '')));
    v_invite_email text;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select lower(trim(i.email))
    into v_invite_email
    from public.household_invites i
    where i.id = p_invite_id
      and i.accepted_at is null
      and i.declined_at is null
      and i.expires_at > now()
    for update;

    if not found then
        raise exception 'invite_not_found';
    end if;

    if char_length(v_auth_email) = 0
        or v_auth_email <> v_invite_email
    then
        raise exception 'invite_email_mismatch';
    end if;

    update public.household_invites
    set declined_at = now()
    where id = p_invite_id;
end;
$$;

revoke all on function public.decline_household_invite(uuid) from public;
grant execute on function public.decline_household_invite(uuid) to authenticated;

-- Soft-removed people retain their unique member row. Re-accepting an invite
-- reactivates that row instead of failing the unique (household_id, user_id)
-- constraint.
create or replace function public.accept_household_invite(p_invite_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_invite public.household_invites%rowtype;
    v_profile_id uuid := auth.uid();
    v_auth_email text := lower(trim(coalesce(auth.jwt() ->> 'email', '')));
    v_member_name text;
    v_household_name text;
begin
    if v_profile_id is null then
        raise exception 'auth_required';
    end if;

    perform public.ensure_current_profile();

    if public.user_has_active_nutrition_household(v_profile_id, null) then
        raise exception 'household_already_active';
    end if;

    if char_length(v_auth_email) = 0 then
        raise exception 'profile_email_required';
    end if;

    select *
    into v_invite
    from public.household_invites i
    where i.id = p_invite_id
      and i.accepted_at is null
      and i.declined_at is null
      and i.expires_at > now()
    for update;

    if not found then
        raise exception 'invite_not_found';
    end if;

    if lower(trim(v_invite.email)) <> v_auth_email then
        raise exception 'invite_email_mismatch';
    end if;

    if public.household_active_member_count(v_invite.household_id) >= 20 then
        raise exception 'household_member_limit_reached';
    end if;

    insert into public.household_members (household_id, user_id, role)
    values (v_invite.household_id, v_profile_id, v_invite.role)
    on conflict (household_id, user_id) where user_id is not null
    do update
    set role = excluded.role,
        left_at = null;

    update public.household_invites
    set accepted_at = now()
    where id = p_invite_id;

    select coalesce(nullif(trim(p.display_name), ''), nullif(trim(p.email), ''), 'Member')
    into v_member_name
    from public.profiles p
    where p.id = v_profile_id;

    select h.name
    into v_household_name
    from public.households h
    where h.id = v_invite.household_id;

    insert into public.household_notification_outbox (kind, payload)
    values (
        'household_member_joined',
        jsonb_build_object(
            'household_id', v_invite.household_id,
            'household_name', v_household_name,
            'member_user_id', v_profile_id,
            'member_name', v_member_name
        )
    );
end;
$$;

revoke all on function public.accept_household_invite(uuid) from public;
grant execute on function public.accept_household_invite(uuid) to authenticated;
