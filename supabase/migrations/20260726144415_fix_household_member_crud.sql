-- Repair member-removal paths broken by the household terminology migration.
-- Keep removal behind manager authorization so owner/admin behavior matches the app.

create or replace function public.remove_household_member(p_member_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_household_id uuid;
    v_target_user_id uuid;
    v_target_role public.household_member_role;
    v_owner_id uuid;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    select sm.household_id, sm.user_id, sm.role, h.owner_id
    into v_household_id, v_target_user_id, v_target_role, v_owner_id
    from public.household_members sm
    join public.households h on h.id = sm.household_id
    where sm.id = p_member_id
      and sm.left_at is null
      and sm.user_id is not null
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

    if v_target_user_id = v_user_id then
        raise exception 'cannot_remove_self';
    end if;

    delete from public.household_members
    where id = p_member_id;
end;
$$;

revoke all on function public.remove_household_member(uuid) from public;
grant execute on function public.remove_household_member(uuid) to authenticated;

create or replace function public.remove_household_dependant(p_dependant_id uuid)
returns void
language plpgsql
security definer
set search_path = public
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

create or replace function public.decline_household_invite(p_invite_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_profile_email text;
    v_invite_email text;
begin
    if v_user_id is null then
        raise exception 'auth_required';
    end if;

    perform public.ensure_current_profile();

    select lower(trim(coalesce(p.email, auth.jwt() ->> 'email', '')))
    into v_profile_email
    from public.profiles p
    where p.id = v_user_id;

    if v_profile_email is null or char_length(v_profile_email) = 0 then
        raise exception 'profile_email_required';
    end if;

    select lower(trim(i.email))
    into v_invite_email
    from public.household_invites i
    where i.id = p_invite_id
      and i.accepted_at is null
      and i.declined_at is null
    for update;

    if not found then
        raise exception 'invite_not_found';
    end if;

    if v_invite_email <> v_profile_email then
        raise exception 'invite_email_mismatch';
    end if;

    update public.household_invites
    set declined_at = now()
    where id = p_invite_id;
end;
$$;

revoke all on function public.decline_household_invite(uuid) from public;
grant execute on function public.decline_household_invite(uuid) to authenticated;

-- Dropping is_space_owner(uuid) CASCADE removed the renamed invite policies in
-- 20250620000000_complete_household_terminology.sql. Restore their intended
-- manager/invitee access with current table and helper names.

alter table public.household_invites enable row level security;

drop policy if exists space_invites_select on public.household_invites;
drop policy if exists household_invites_select on public.household_invites;
create policy household_invites_select
    on public.household_invites
    for select
    to authenticated
    using (
        public.is_household_manager(household_id)
        or lower(trim(email)) = lower(trim(coalesce(
            (select p.email from public.profiles p where p.id = (select auth.uid())),
            auth.jwt() ->> 'email',
            ''
        )))
    );

drop policy if exists space_invites_insert on public.household_invites;
drop policy if exists household_invites_insert on public.household_invites;
create policy household_invites_insert
    on public.household_invites
    for insert
    to authenticated
    with check (
        role <> 'owner'
        and (
            public.is_household_owner(household_id)
            or (
                public.is_household_manager(household_id)
                and role <> 'admin'
            )
        )
    );

drop policy if exists space_invites_update on public.household_invites;
drop policy if exists household_invites_update on public.household_invites;

grant select, insert on public.household_invites to authenticated;
revoke update on public.household_invites from authenticated;
