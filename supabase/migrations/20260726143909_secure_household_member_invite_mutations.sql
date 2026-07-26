-- Restore collaboration contracts lost when legacy space helpers were dropped
-- with CASCADE, and route privileged mutations through narrowly granted RPCs.

create or replace function public.remove_household_member(p_member_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_household_id uuid;
    v_target_user_id uuid;
    v_target_role public.household_member_role;
    v_owner_id uuid;
begin
    if (select auth.uid()) is null then
        raise exception 'auth_required';
    end if;

    select sm.household_id, sm.user_id, sm.role, h.owner_id
    into v_household_id, v_target_user_id, v_target_role, v_owner_id
    from public.household_members sm
    join public.households h on h.id = sm.household_id
    where sm.id = p_member_id
      and sm.user_id is not null
      and sm.left_at is null
    for update of sm;

    if not found then
        raise exception 'member_not_found';
    end if;

    if not public.is_household_manager(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    if v_target_role = 'owner' or v_target_user_id = v_owner_id then
        raise exception 'cannot_remove_owner';
    end if;

    delete from public.household_members
    where id = p_member_id
      and left_at is null;
end;
$$;

create or replace function public.remove_household_dependant(p_dependant_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_household_id uuid;
begin
    if (select auth.uid()) is null then
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

    if not public.household_member_can_write_nutrition(v_household_id) then
        raise exception 'insufficient_role';
    end if;

    update public.household_dependants
    set removed_at = now()
    where id = p_dependant_id
      and removed_at is null;
end;
$$;

create or replace function public.decline_household_invite(p_invite_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := (select auth.uid());
    v_verified_email text := lower(trim(coalesce(auth.jwt() ->> 'email', '')));
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    if char_length(v_verified_email) = 0 then
        raise exception 'profile_email_required';
    end if;

    perform 1
    from public.household_invites i
    where i.id = p_invite_id
      and lower(trim(i.email)) = v_verified_email
      and i.accepted_at is null
      and i.declined_at is null
    for update;

    if not found then
        raise exception 'invite_not_found';
    end if;

    update public.household_invites
    set declined_at = now()
    where id = p_invite_id
      and accepted_at is null
      and declined_at is null;
end;
$$;

-- The legacy invite policies were removed by DROP ... CASCADE. Restore only
-- the read contract: managers see outbound invites and invitees see their own.
drop policy if exists space_invites_select on public.household_invites;
drop policy if exists space_invites_insert on public.household_invites;
drop policy if exists space_invites_update on public.household_invites;
drop policy if exists household_invites_select on public.household_invites;
drop policy if exists household_invites_insert on public.household_invites;
drop policy if exists household_invites_update on public.household_invites;
drop policy if exists household_invites_delete on public.household_invites;

create policy household_invites_select
    on public.household_invites
    for select
    to authenticated
    using (
        public.is_household_manager(household_id)
        or lower(trim(email)) = lower(trim(coalesce(
            (select auth.jwt() ->> 'email'),
            ''
        )))
    );

-- All member and invite mutations now pass through RPC authorization checks.
drop policy if exists space_members_insert on public.household_members;
drop policy if exists space_members_update on public.household_members;
drop policy if exists space_members_delete on public.household_members;
drop policy if exists household_members_insert on public.household_members;
drop policy if exists household_members_update on public.household_members;
drop policy if exists household_members_delete on public.household_members;

revoke insert, update, delete on public.household_members from public, anon, authenticated;
grant select on public.household_members to authenticated;

revoke insert, update, delete on public.household_invites from public, anon, authenticated;
grant select on public.household_invites to authenticated;

revoke all on function public.remove_household_member(uuid) from public, anon, authenticated;
grant execute on function public.remove_household_member(uuid) to authenticated;

revoke all on function public.remove_household_dependant(uuid) from public, anon, authenticated;
grant execute on function public.remove_household_dependant(uuid) to authenticated;

revoke all on function public.decline_household_invite(uuid) from public, anon, authenticated;
grant execute on function public.decline_household_invite(uuid) to authenticated;
